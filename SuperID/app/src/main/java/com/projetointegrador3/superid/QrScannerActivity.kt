package com.projetointegrador3.superid

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.projetointegrador3.superid.permissions.WithPermission
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class QrScannerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WithPermission(
                permission = Manifest.permission.CAMERA,
            ) {
                TakePhotoScreen()
            }
        }
    }
}

class BarcodeAnalyzer(
    private val onResult: (loginToken: String, status: String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()
    private var isProcessing = false

    // Set para guardar tokens já verificados e evitar chamadas repetidas
    private val tokensVerificados = mutableSetOf<String>()

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (isProcessing) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            isProcessing = true

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val value = barcode.rawValue
                        if (value != null) {
                            // Verifica se token já foi processado
                            if (tokensVerificados.contains(value)) {
                                // Já processado, ignora
                                continue
                            } else {
                                tokensVerificados.add(value)
                            }

                            verificarLoginStatus(value) { response ->
                                if (response.trim().startsWith("{")) {
                                    try {
                                        val json = JSONObject(response)
                                        val status = json.optString("status", "")

                                        val userUid = FirebaseAuth.getInstance().currentUser?.uid
                                        if (userUid == null) {
                                            Log.e("SuperID", "Usuário não está logado")
                                            onResult(value, "erro")
                                            return@verificarLoginStatus
                                        }

                                        if (status != "confirmado") {
                                            confirmarLoginToken(value, userUid)
                                            onResult(value, "confirmado")
                                        } else {
                                            onResult(value, "confirmado")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("SuperID", "Erro no JSON ou status: ${e.message}")
                                        onResult(value, "erro")
                                    }
                                } else {
                                    Log.e("SuperID", "Resposta inesperada (não é JSON): $response")
                                    onResult(value, "erro")
                                }
                            }
                            break
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("QRCode", "Erro ao processar: ${it.message}")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                    isProcessing = false
                }
        } else {
            imageProxy.close()
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    lensFacing: Int,
    zoomLevel: Float,
    imageCaptureUseCase: ImageCapture,
    analyzer: ImageAnalysis.Analyzer
) {
    val previewUseCase = remember {
        androidx.camera.core.Preview.Builder().build()
    }

    var cameraProvider by remember {
        mutableStateOf<ProcessCameraProvider?>(null)
    }

    var cameraControl by remember {
        mutableStateOf<CameraControl?>(null)
    }

    val localContext = LocalContext.current

    val imageAnalyzer = remember(analyzer) {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(ContextCompat.getMainExecutor(localContext), analyzer)
            }
    }

    fun rebindCameraProvider() {
        cameraProvider?.let { provider ->
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()
            provider.unbindAll()
            val camera = provider.bindToLifecycle(
                localContext as LifecycleOwner,
                cameraSelector,
                previewUseCase,
                imageCaptureUseCase,
                imageAnalyzer
            )
            cameraControl = camera.cameraControl
        }
    }

    LaunchedEffect(Unit) {
        cameraProvider = ProcessCameraProvider.awaitInstance(localContext)
        rebindCameraProvider()
    }

    LaunchedEffect(lensFacing) {
        rebindCameraProvider()
    }

    LaunchedEffect(zoomLevel) {
        cameraControl?.setLinearZoom(zoomLevel)
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            PreviewView(context).also {
                previewUseCase.setSurfaceProvider(it.surfaceProvider)
            }
        }
    )
}

@Composable
fun TakePhotoScreen() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    val imageCaptureUseCase = remember { ImageCapture.Builder().build() }

    val analyzer = remember {
        BarcodeAnalyzer { _, status ->
            activity?.runOnUiThread {
                Toast.makeText(
                    context,
                    when (status.lowercase()) {
                        "confirmado" -> "Login concluído"
                        "aguardando confirmação" -> "Aguardando confirmação"
                        else -> "Falha no login"
                    },
                    Toast.LENGTH_LONG
                ).show()
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    activity.finish()
                }, 150)
            }
        }
    }

    var lensFacing by remember {
        mutableIntStateOf(CameraSelector.LENS_FACING_BACK)
    }
    var zoomLevel by remember {
        mutableFloatStateOf(0.0f)
    }

    Box {
        CameraPreview(
            lensFacing = lensFacing,
            zoomLevel = zoomLevel,
            imageCaptureUseCase = imageCaptureUseCase,
            analyzer = analyzer
        )

        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            Row {
                Button(
                    onClick = { lensFacing = CameraSelector.LENS_FACING_FRONT }
                ) { Text("Frontal") }
                Button(
                    onClick = { lensFacing = CameraSelector.LENS_FACING_BACK }
                ) { Text("Traseira") }
            }
            Row {
                Button(onClick = { zoomLevel = 0.0f }) { Text("Zoom 0") }
                Button(onClick = { zoomLevel = 0.5f }) { Text("Zoom 0.5") }
                Button(onClick = { zoomLevel = 1.0f }) { Text("Zoom 1.0") }
            }
        }
    }
}

fun verificarLoginStatus(loginToken: String, onResult: (String) -> Unit) {
    Thread {
        try {
            val url = URL("https://getloginstatus-nl2bfugfma-uc.a.run.app")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val jsonBody = """{"loginToken": "$loginToken"}"""
            conn.outputStream.write(jsonBody.toByteArray())

            val encoding = conn.getHeaderField("Content-Encoding")
            val inputStream = if (encoding != null && encoding.equals("gzip", ignoreCase = true)) {
                java.util.zip.GZIPInputStream(conn.inputStream)
            } else {
                conn.inputStream
            }

            val response = inputStream.bufferedReader().use { it.readText() }
            onResult(response)
        } catch (e: Exception) {
            val errorMsg = "Erro ao conectar: ${e.javaClass.simpleName} - ${e.message}"
            Log.e("verificarLoginStatus", errorMsg, e)
            onResult(errorMsg)
        }
    }.start()
}

fun confirmarLoginToken(token: String, userUid: String) {
    val db = FirebaseFirestore.getInstance()
    val docRef = db.collection("login").document(token)

    val data = hashMapOf(
        "uid" to userUid,
        "status" to "confirmado",
        "timestamp" to FieldValue.serverTimestamp(),
        "attempts" to 1
        // atualizar attempts para 0
    )

    docRef.set(data, SetOptions.merge())
        .addOnSuccessListener {
            Log.d("SuperID", "Login confirmado e salvo no Firestore")
        }
        .addOnFailureListener { e ->
            Log.e("SuperID", "Erro ao atualizar Firestore: ${e.message}")
        }
}