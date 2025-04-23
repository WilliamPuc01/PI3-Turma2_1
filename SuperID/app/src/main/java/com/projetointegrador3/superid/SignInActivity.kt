package com.projetointegrador3.superid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.projetointegrador3.superid.ui.theme.SuperIDTheme


class SignInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                LoginScreenPreview()
            }
        }
    }
}


@Preview
@Composable
fun LoginScreenPreview(){
    LoginScreen()
}

fun login(email: String, password:String, context: android.content.Context, onResult: (String) -> Unit){
    val auth = Firebase.auth
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult("Login realizado com sucesso!")
                val intent = Intent(context, HomeActivity::class.java)
                context.startActivity(intent)
            } else {
                val exception = task.exception
                val errorMessage = when ((exception as? com.google.firebase.auth.FirebaseAuthException)?.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "E-mail inválido."
                    "ERROR_WRONG_PASSWORD" -> "Senha incorreta."
                    else -> "Erro ao fazer login"
                }
                onResult(errorMessage)
            }
        }
}

// Função para enviar email para redefinir senha
fun sendPasswordReset(email: String, onResult: (String) -> Unit) {
    val auth = Firebase.auth
    auth.sendPasswordResetEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult("E-mail de redefinição enviado com sucesso.")
            } else {
                val exception = task.exception
                val errorMessage = when ((exception as? com.google.firebase.auth.FirebaseAuthException)?.errorCode) {
                    "ERROR_USER_NOT_FOUND" -> "Usuário não encontrado."
                    "ERROR_INVALID_EMAIL" -> "E-mail inválido."
                    else -> "Erro ao enviar e-mail: ${exception?.localizedMessage}"
                }
                onResult(errorMessage)
            }
        }
}


// Alert Dialog para abrir quando clicar em "Esqueci minha senha"
@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Redefinir senha") },
        text = {
            Column {
                Text("Digite seu e-mail para receber o link de redefinição.")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSend(email)
                onDismiss()
            }) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        }
    )
}



@Composable
fun LoginScreen(modifier: Modifier = Modifier
    .fillMaxSize()
    ) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val customColors = darkColorScheme(
        primary = Color(0xFFD4AF37), // Dourado
        surface = Color(0xFF121212), // Preto
        onSurface = Color.White
    )

    MaterialTheme(colorScheme = customColors) {
        Box(modifier = modifier) {
            // Imagem de fundo
            Image(
                painter = painterResource(R.drawable.fundo),
                contentDescription = "Fundo do app",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(1.0f)
            )
                Column(
                    modifier = modifier
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Login", fontSize = 30.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", color = Color.White) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Senha", color = Color.White) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))



                    TextButton(onClick = { showForgotPasswordDialog = true }) {
                        Text("Esqueci minha senha", color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline))
                    }

                    Spacer(modifier = Modifier.height(1.dp))

                    TextButton(
                        onClick = {
                            val intent = Intent(context, RegisterActivity::class.java)
                            context.startActivity(intent)
                        }
                    ) {
                        Text("Não tem conta? ", color = Color.White)
                        Text("Criar conta", color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline))
                    }

                    Button(
                        onClick = {
                            login(email, password, context) { error -> message = error }
                        }, modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(48.dp)
                    ) {
                        Text(text = "Entrar")
                    }

                    // Mostra mensagens ao usuário
                    if (message.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = message, color = Color.Red)
                    }
                }

            // Abre o Dialog para digitar o email
            if (showForgotPasswordDialog) {
                ForgotPasswordDialog(
                    onDismiss = { showForgotPasswordDialog = false },
                    onSend = { email ->
                        sendPasswordReset(email) { result ->
                            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
        }
    }
}
