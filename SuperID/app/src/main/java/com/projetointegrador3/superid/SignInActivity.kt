@file:Suppress("NAME_SHADOWING")

package com.projetointegrador3.superid

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.layout.padding
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

    // Tentativa de login com senha inválida só pra puxar o user
    auth.signInWithEmailAndPassword(email, "qualquerCoisa")
        .addOnCompleteListener { task ->
            val exception = task.exception
            val user = auth.currentUser

            if (task.isSuccessful || exception?.message?.contains("The password is invalid") == true) {
                // Verifica se o email está verificado
                if (user != null && user.isEmailVerified) {
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { resetTask ->
                            if (resetTask.isSuccessful) {
                                onResult("E-mail de redefinição enviado com sucesso.")
                            } else {
                                onResult("Erro ao enviar e-mail.")
                            }
                        }
                }
            } else {
                val errorMessage = when ((exception as? com.google.firebase.auth.FirebaseAuthException)?.errorCode) {
                    "ERROR_USER_NOT_FOUND" -> "Usuário não encontrado."
                    "ERROR_INVALID_EMAIL" -> "E-mail inválido."
                    else -> "Esse e-mail ainda não foi verificado."
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
fun LoginScreen(modifier: Modifier = Modifier.fillMaxSize()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }


    Box(modifier = modifier) {
            BackgroundImage()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // LOGO
                Image(
                    painter = painterResource(id = R.drawable.superid_removebg), // use o seu logo aqui
                    contentDescription = "Logo SuperID",
                    modifier = Modifier
                        .height(180.dp)
                        .padding(bottom = 16.dp),
                    contentScale = ContentScale.Fit
                )

                // Título
                Text("Login", fontSize = 30.sp, color = MaterialTheme.colorScheme.onSurface)

                Spacer(modifier = Modifier.height(20.dp))

                // Campo Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo Senha
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Senha") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Ocultar senha" else "Mostrar senha"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = icon, contentDescription = description)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Esqueci minha senha
                TextButton(onClick = { showForgotPasswordDialog = true }) {
                    if (isSystemInDarkTheme()) {
                        Text(
                            "Esqueci minha senha",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    } else {
                        Text(
                            "Esqueci minha senha",
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    }
                }
                // Criar conta
                TextButton(
                    onClick = {
                        val intent = Intent(context, RegisterActivity::class.java)
                        context.startActivity(intent)
                    }
                ) {
                    if (isSystemInDarkTheme()) {
                        Text("Não tem conta? ", color = Color.White)
                    Text(
                        "Criar conta",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = TextDecoration.Underline
                        )
                    )
                }else{
                        Text("Não tem conta? ", color = Color.Black)
                        Text(
                            "Criar conta",
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    }
                }

                // Botão Entrar
                Button(
                    onClick = {
                        login(email, password, context) { error -> message = error }
                    },
                    modifier = Modifier
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(text = "Entrar", color = Color.Black)
                }

                // Mensagem de erro ou sucesso
                if (message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = message, color = MaterialTheme.colorScheme.onSurface)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Diálogo de redefinição de senha
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

