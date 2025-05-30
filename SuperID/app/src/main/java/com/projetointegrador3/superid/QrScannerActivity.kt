

package com.projetointegrador3.superid

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.projetointegrador3.superid.permissions.WithPermission
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom

class QrScannerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WithPermission(permission = Manifest.permission.CAMERA) {
                QrScannerScreen(onBack = { finish() })
            }
        }
    }
}
@Composable
fun QrScannerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val imageCapture = remember { ImageCapture.Builder().build() }
    val colors = MaterialTheme.colorScheme

    val analyzer = remember {
        BarcodeAnalyzer { _, status ->
            activity?.runOnUiThread {
                Toast.makeText(
                    context,
                    when (status.lowercase()) {
                        "confirmado" -> "Login autorizado"
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

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            lensFacing = lensFacing,
            zoomLevel = 0.0f,
            imageCaptureUseCase = imageCapture,
            analyzer = analyzer
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            IconButton(onClick = { onBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = colors.onBackground,
                    modifier = Modifier.size(32.dp)
                )
            }
        }


        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        ) {
            Button(
                onClick = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                        CameraSelector.LENS_FACING_FRONT
                    else
                        CameraSelector.LENS_FACING_BACK
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.surface,
                    contentColor = colors.onSurface
                )
            ) {
                Text("Trocar câmera")
            }
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
    val preview = remember { Preview.Builder().build() }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    val context = LocalContext.current

    val imageAnalyzer = remember(analyzer) {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().apply {
                setAnalyzer(ContextCompat.getMainExecutor(context), analyzer)
            }
    }

    fun bindCamera() {
        cameraProvider?.let { provider ->
            val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            provider.unbindAll()
            val camera = provider.bindToLifecycle(
                context as LifecycleOwner,
                selector,
                preview,
                imageCaptureUseCase,
                imageAnalyzer
            )
            cameraControl = camera.cameraControl
        }
    }

    LaunchedEffect(Unit) {
        cameraProvider = ProcessCameraProvider.awaitInstance(context)
        bindCamera()
    }

    LaunchedEffect(lensFacing) { bindCamera() }
    LaunchedEffect(zoomLevel) { cameraControl?.setLinearZoom(zoomLevel) }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { PreviewView(it).apply { preview.setSurfaceProvider(surfaceProvider) } }
    )
}

// Analisa o QRCode
class BarcodeAnalyzer(
    private val onResult: (String, String) -> Unit
) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient()
    private var isProcessing = false
    private val tokensProcessados = mutableSetOf<String>()

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (isProcessing) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            isProcessing = true
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val token = barcode.rawValue ?: continue
                        if (tokensProcessados.contains(token)) continue
                        tokensProcessados.add(token)

                        verificarLoginStatus(token) { response ->
                            if (response.trim().startsWith("{")) {
                                val json = JSONObject(response)
                                val status = json.optString("status", "")
                                val user = FirebaseAuth.getInstance().currentUser

                                if (user != null && status != "confirmado") {
                                    searchLoginDocument(token)
                                }

                                onResult(token, status)
                            } else {
                                onResult(token, "erro")
                            }
                        }

                        break
                    }
                }
                .addOnFailureListener {
                    Log.e("QRCode", "Erro: ${it.message}")
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

// Verifica status de login
fun verificarLoginStatus(loginToken: String, onResult: (String) -> Unit) {
    Thread {
        try {
            val url = URL("https://getloginstatus-nl2bfugfma-uc.a.run.app")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val json = """{"loginToken": "$loginToken"}"""
            conn.outputStream.write(json.toByteArray())

            val response = conn.inputStream.bufferedReader().use { it.readText() }
            onResult(response)
        } catch (e: Exception) {
            Log.e("LoginStatus", "Erro: ${e.message}", e)
            onResult("erro")
        }
    }.start()
}

fun searchLoginDocument(loginToken: String) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser ?: return

    db.collection("login")
        .whereEqualTo("loginToken", loginToken)
        .get()
        .addOnSuccessListener { result ->
            if (result.isEmpty) return@addOnSuccessListener

            val loginDoc = result.documents.first()
            val siteUrl = loginDoc.getString("site")?.trim() ?: return@addOnSuccessListener

            db.collection("usuarios")
                .document(user.uid)
                .collection("categorias")
                .document("Sites Web")
                .collection("Sites Web")
                .whereEqualTo("url", siteUrl)
                .get()
                .addOnSuccessListener { senhas ->
                    if (senhas.isEmpty) {
                        Log.e("LOGINSENHA", "URL não encontrada: $siteUrl")
                        return@addOnSuccessListener
                    }

                    val senhaDoc = senhas.documents.first()
                    val oldAccessToken = senhaDoc.getString("accessToken") ?: generateAccessToken()
                    confirmarLoginTokenComToken(
                        token = loginDoc.id,
                        userUid = user.uid,
                        oldAccessToken = oldAccessToken,
                        senhaDocId = senhaDoc.id
                    )
                }
        }
        .addOnFailureListener {
            Log.e("LOGINSENHA", "Erro ao buscar loginToken", it)
        }
}

fun confirmarLoginTokenComToken(
    token: String,
    userUid: String,
    oldAccessToken: String,
    senhaDocId: String
) {
    val db = FirebaseFirestore.getInstance()
    val loginRef = db.collection("login").document(token)
    val newAccessToken = generateAccessToken()

    val loginData = hashMapOf(
        "uid" to userUid,
        "accessToken" to oldAccessToken,
        "status" to "confirmado",
        "timestamp" to FieldValue.serverTimestamp(),
        "attempts" to 1
    )

    loginRef.set(loginData, SetOptions.merge())
        .addOnSuccessListener {
            db.collection("usuarios")
                .document(userUid)
                .collection("categorias")
                .document("Sites Web")
                .collection("Sites Web")
                .document(senhaDocId)
                .update("accessToken", newAccessToken)
                .addOnSuccessListener {
                    Log.d("SuperID", "Novo accessToken atualizado com sucesso")
                }
                .addOnFailureListener {
                    Log.e("SuperID", "Erro ao atualizar novo accessToken", it)
                }
        }
        .addOnFailureListener {
            Log.e("SuperID", "Erro ao confirmar login", it)
        }
}

fun generateAccessToken(): String {
    val randomBytes = ByteArray(192)
    SecureRandom().nextBytes(randomBytes)
    return Base64.encodeToString(randomBytes, Base64.NO_WRAP)
}