package com.projetointegrador3.superid

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.projetointegrador3.superid.permissions.WithPermission
import com.projetointegrador3.superid.ui.theme.SuperIDTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        super.onCreate(savedInstanceState)

        // Verifica se o usuário já aceitou os termos
        if (!primeiraVez(this)) {
            // Vai direto para a tela de login
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish() // Fecha a MainActivity para não voltar
        } else {
            // Exibe a tela de boas-vindas com termos
            enableEdgeToEdge()
            setContent {
                SuperIDTheme {
                    PermissionAndWelcomeFlow()
                }
            }
        }
    }
}

@Composable
fun PermissionAndWelcomeFlow() {
    WithPermission(permission = Manifest.permission.CAMERA) {
        WelcomeScreen()
    }
}

fun primeiraVez(context: Context): Boolean {
    val sharedPref = context.getSharedPreferences("SuperIDPrefs", Context.MODE_PRIVATE)
    return sharedPref.getBoolean("PrimeiraVez", true)
}

fun setPrimeiraVez(context: Context, primeiraVez: Boolean) {
    val sharedPref = context.getSharedPreferences("SuperIDPrefs", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putBoolean("PrimeiraVez", primeiraVez)
        apply()
    }
}


@Composable
fun WelcomeScreen() {
    val context = LocalContext.current
    var introTermos by remember { mutableStateOf(primeiraVez(context)) }
    var isChecked by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundImage()

        if (introTermos) {
            AlertDialog(
                onDismissRequest = { },
                properties = DialogProperties(
                    dismissOnClickOutside = false,
                    dismissOnBackPress = false
                ),
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.superid_removebg),
                            contentDescription = "Logo SuperID",
                            modifier = Modifier
                                .height(100.dp)
                                .padding(bottom = 8.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Bem-Vindo ao SuperID",
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Aqui você tem o armazenamento seguro de senhas para logar nos nossos sites parceiros.",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Image(
                            painter = painterResource(R.drawable.superid_exemplo),
                            contentDescription = "Salve e categorize suas senhas",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Salve e categorize suas senhas",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        val annotatedText = buildAnnotatedString {
                            val termosUrl = "https://firebasestorage.googleapis.com/v0/b/superid-760b7.firebasestorage.app/o/SuperID-Termos%20de%20Uso.pdf?alt=media&token=3a7cf7a4-0663-47d3-b114-84bda9cccde1"

                            pushStringAnnotation(
                                tag = "URL",
                                annotation = termosUrl
                            )
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("TERMOS DE USO")
                            }
                            pop()
                        }

                        BasicText(
                            text = annotatedText,
                            modifier = Modifier
                                .clickable {
                                    annotatedText.getStringAnnotations(tag = "URL", start = 0, end = annotatedText.length)
                                        .firstOrNull()?.let { annotation ->
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                            context.startActivity(intent)
                                        }
                                }
                                .padding(8.dp),
                            style = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))



                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    isChecked = it
                                    if (it) showError = false
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.onSurface,
                                    checkmarkColor = MaterialTheme.colorScheme.surface
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Li e aceito os termos de uso",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (showError) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aceite os termos de uso antes de prosseguir",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (isChecked) {
                                introTermos = false
                                setPrimeiraVez(context, false)
                                val intent = Intent(context, RegisterActivity::class.java)
                                context.startActivity(intent)
                                if (context is ComponentActivity) {
                                    context.finish()
                                }
                            } else {
                                // erro ao não aceitar termos
                                showError = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Continuar", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview
@Composable
fun WelcomeScreenPreview() {
    SuperIDTheme {
        WelcomeScreen()
    }
}
