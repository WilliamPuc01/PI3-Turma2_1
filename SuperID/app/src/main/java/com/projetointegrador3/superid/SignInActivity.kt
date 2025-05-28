package com.projetointegrador3.superid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.functions.functions
import com.projetointegrador3.superid.ui.theme.SuperIDTheme


class SignInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                LoginScreen()
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
                    "ERROR_INVALID_EMAIL" -> "O e-mail inserido é inválido."
                    "ERROR_WRONG_PASSWORD" -> "Senha incorreta. Tente novamente."
                    else -> "Erro ao fazer login"
                }
                onResult(errorMessage)
            }
        }
}

// Função para enviar email para redefinir senha
fun sendPasswordReset(email: String, onResultado: (String) -> Unit) {
    val functions = Firebase.functions("southamerica-east1")

    functions
        .getHttpsCallable("checkEmailVerification")
        .call(hashMapOf<String, Any>("email" to email))
        .addOnSuccessListener { result ->
            val data = result.data as? Map<*, *>
            val isVerified = data?.get("verified") as? Boolean ?: false

            if (isVerified) {
                Firebase.auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onResultado("E-mail de redefinição enviado com sucesso.")
                        } else {
                            onResultado("Erro ao enviar o e-mail: ${task.exception?.message}")
                        }
                    }
            } else {
                onResultado("Este e-mail ainda não foi verificado.")
            }
        }
        .addOnFailureListener { exception ->
            onResultado("Erro ao verificar e-mail: ${exception.message}")
        }
}

// Alert Dialog para abrir quando clicar em "Esqueci minha senha"
@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Redefinir Senha")
        },
        text = {
            Column {
                Text("Digite seu e-mail para receber um link de redefinição de senha.")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val cleanEmail = email.trim().lowercase()
                    onSend(cleanEmail)      // Usa o callback passado
                    onDismiss()
                },
                enabled = email.isNotEmpty()
            ) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

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

            Image(
                painter = painterResource(id = R.drawable.superid_removebg),
                contentDescription = "Logo SuperID",
                modifier = Modifier
                    .height(180.dp)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )

            Text("Login", fontSize = 30.sp, color = MaterialTheme.colorScheme.onSurface)

            Spacer(modifier = Modifier.height(20.dp))

            // Campo Email
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (it.isNotBlank()) emailError = null
                    },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = emailError != null
                )
                AnimatedVisibility(visible = emailError != null) {
                    Text(
                        text = emailError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Campo Senha
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (it.isNotBlank()) passwordError = null
                    },
                    label = { Text("Senha") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = passwordError != null,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Ocultar senha" else "Mostrar senha"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = icon, contentDescription = description)
                        }
                    }
                )
                AnimatedVisibility(visible = passwordError != null) {
                    Text(
                        text = passwordError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão Entrar
            Button(
                onClick = {
                    var isValid = true
                    if (email.isBlank()) {
                        emailError = "O e-mail é obrigatório."
                        isValid = false
                    }
                    if (password.isBlank()) {
                        passwordError = "A senha é obrigatória."
                        isValid = false
                    }

                    if (isValid) {
                        login(email.trim(), password, context) { error ->
                            message = error
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Entrar", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Esqueci minha senha
            TextButton(onClick = { showForgotPasswordDialog = true }) {
                val textColor = if (isSystemInDarkTheme()) Color.White else Color.Black
                Text(
                    "Esqueci minha senha",
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline)
                )
            }

            // Criar conta
            TextButton(
                onClick = {
                    val intent = Intent(context, RegisterActivity::class.java)
                    context.startActivity(intent)
                }
            ) {
                val textColor = if (isSystemInDarkTheme()) Color.White else Color.Black
                Row {
                    Text("Não tem conta? ", color = textColor)
                    Text(
                        "Criar conta",
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Toast de mensagem
            LaunchedEffect(message) {
                if (message.isNotEmpty()) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Diálogo de redefinição de senha
        if (showForgotPasswordDialog) {
            ForgotPasswordDialog(
                onDismiss = { showForgotPasswordDialog = false },
                onSend = { emailInput ->
                    sendPasswordReset(emailInput) { result ->
                        Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
    }
}