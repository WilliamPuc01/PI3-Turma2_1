package com.projetointegrador3.superid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.projetointegrador3.superid.ui.theme.SuperIDTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.core.net.toUri
import android.net.Uri
import android.net.Uri.parse
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

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
                    WelcomeScreenPreview()
                }
            }
        }
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
fun WelcomeScreen(
) {
    val context = LocalContext.current

    // verifica se já foi aceito os termos de uso
    var introTermos by remember { mutableStateOf(primeiraVez(context)) }

    val customColors = darkColorScheme(
        primary = Color(0xFFD4AF37), // Dourado
        surface = Color(0xFF121212), // Preto
        onSurface = Color.White
    )

    MaterialTheme(colorScheme = customColors) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagem de fundo
            Image(
                painter = painterResource(R.drawable.fundo),
                contentDescription = "Fundo do app",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(1.0f)
            )

            // pop up de introdução
            var isChecked by remember { mutableStateOf(false) }
            var showError by remember { mutableStateOf(false) }

            if (introTermos) {
                AlertDialog(
                    onDismissRequest = {
                    },
                    properties = DialogProperties(
                        dismissOnClickOutside = false,
                        dismissOnBackPress = false
                    ),
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Bem-Vindo ao SuperID",
                                fontSize = 24.sp,
                                color = Color.White
                            )
                        }
                    },
                    text = {
                        Column(

                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Aqui você tem o armazenamento seguro de senhas para logar nos nossos sites parceiros",
                                fontSize = 16.sp,
                                color = Color.White,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center // Alinhar texto no centro
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Image(
                                painter = painterResource(R.drawable.superid_exemplo),
                                contentDescription = "salve e categorize suas senhas",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp),
                                contentScale = ContentScale.Fit
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "salve e categorize suas senhas",
                                fontSize = 14.sp,
                                color = Color.White,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                                        color = Color(0xFF64B5F6), // azul claro para link
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append("TERMOS DE USO")
                                }
                                pop()
                            }

                            val uriHandler = LocalContext.current

                            BasicText(
                                text = annotatedText,
                                modifier = Modifier
                                    .clickable {
                                        annotatedText.getStringAnnotations(tag = "URL", start = 0, end = annotatedText.length)
                                            .firstOrNull()?.let { annotation ->
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                                uriHandler.startActivity(intent)
                                            }
                                    }
                                    .padding(8.dp),
                                style = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp
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
                                        checkedColor = Color(0xFFD4AF37),
                                        uncheckedColor = Color.White,
                                        checkmarkColor = Color.Black
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Li e aceito os termos de uso",
                                    color = Color.White
                                )
                            }

                            if (showError) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Aceite os termos de uso antes de prosseguir",
                                    color = Color.Red,
                                    fontSize = 14.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                                    showError = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD4AF37),
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Continuar")
                        }
                    },
                    containerColor = Color(0xFF2C2C2C),
                    titleContentColor = Color.White,
                    textContentColor = Color.White
                )
            }
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