package com.projetointegrador3.superid

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.projetointegrador3.superid.ui.theme.SuperIDTheme

class RegisterActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                RegisterScreen()
            }
        }
    }
}

// Cria a conta
fun createAccount(name:String, email: String, password:String, context: android.content.Context, onResult: (String) -> Unit){
    val auth = Firebase.auth
    val db = Firebase.firestore
    val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val userId = user?.uid

                user?.sendEmailVerification()
                    ?.addOnCompleteListener { verifyTask ->
                        if (verifyTask.isSuccessful) {
                            onResult("Conta criada! Verifique seu e-mail para ativá-la.")
                        } else {
                            onResult("Conta criada, mas falha ao enviar e-mail de verificação.")
                        }
                    }

                val userMap = hashMapOf("nome" to name, "email" to email, "androidId" to androidId)
                userId?.let { id ->
                    db.collection("usuarios").document(id)
                        .set(userMap)
                        .addOnSuccessListener {
                            println("Dados do usuário salvos com sucesso!")
                            // Cria 3 categorias automaticamente
                            createDefaultCategories(id)
                        }
                        .addOnFailureListener { e ->
                            println("Erro ao salvar dados: \${e.message}")
                        }
                }

            } else {
                val exception = task.exception
                val errorMessage = when ((exception as? com.google.firebase.auth.FirebaseAuthException)?.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "Este e-mail já está em uso."
                    "ERROR_INVALID_EMAIL" -> "E-mail inválido."
                    "ERROR_WEAK_PASSWORD" -> "A senha deve ter pelo menos 6 caracteres."
                    else -> "Erro ao criar conta: \${exception?.localizedMessage}"
                }
                onResult(errorMessage)

            }
        }
}

// Cria 3 categorias automaticamente
fun createDefaultCategories(userId: String) {
    val db = Firebase.firestore
    val categorias = listOf("Sites Web", "Aplicativos", "Teclados Físicos")

    categorias.forEach { categoryName ->
        db.collection("usuarios")
            .document(userId)
            .collection("categorias")
            .document(categoryName)
            .set(hashMapOf<String, Any>())
    }
}

@Composable
fun RegisterScreen(modifier: Modifier = Modifier.fillMaxSize()) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    var passwordVisible by remember { mutableStateOf(false) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf<FirebaseAuth?>(null) }

    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme

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

            Text("Criar Conta", fontSize = 30.sp, color = colors.onBackground)
            Spacer(modifier = Modifier.height(20.dp))

            // Nome
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = null
                    },
                    label = { Text("Nome completo", color = colors.onSurface) },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = nameError != null
                )
                // Avisa o usuário caso tenha algum campo vazio
                AnimatedVisibility(nameError != null) {
                    Text(nameError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Email
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (it.isNotBlank()) emailError = null
                    },
                    label = { Text("Email", color = colors.onSurface) },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = emailError != null
                )
                // Avisa o usuário caso tenha algum campo vazio
                AnimatedVisibility(emailError != null) {
                    Text(emailError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Senha
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (it.isNotBlank()) passwordError = null
                    },
                    label = { Text("Senha", color = colors.onSurface) },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = passwordError != null,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(icon, contentDescription = null, tint = colors.onSurface)
                        }
                    }
                )
                // Avisa o usuário caso tenha algum campo vazio
                AnimatedVisibility(passwordError != null) {
                    Text(passwordError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Confirmar senha
            Column {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        if (it.isNotBlank()) confirmPasswordError = null
                    },
                    label = { Text("Confirmar Senha", color = colors.onSurface) },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = confirmPasswordError != null,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(icon, contentDescription = null, tint = colors.onSurface)
                        }
                    }
                )
                // Avisa o usuário caso tenha algum campo vazio
                AnimatedVisibility(confirmPasswordError != null) {
                    Text(confirmPasswordError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Só chama a função createAccount quando todos os campos forem preenchidos
            Button(
                onClick = {
                    var isValid = true

                    if (name.isBlank()) {
                        nameError = "Nome obrigatório"
                        isValid = false
                    }
                    if (email.isBlank()) {
                        emailError = "Email obrigatório"
                        isValid = false
                    }
                    if (password.isBlank()) {
                        passwordError = "Senha obrigatória"
                        isValid = false
                    }
                    if (confirmPassword.isBlank()) {
                        confirmPasswordError = "Confirme a senha"
                        isValid = false
                    } else if (password != confirmPassword) {
                        confirmPasswordError = "As senhas não coincidem"
                        isValid = false
                    }

                    if (isValid) {
                        createAccount(name, email.trim(), password, context) { result ->
                            message = result
                            currentUser = Firebase.auth
                            if (result.contains("Verifique seu e-mail")) {
                                showVerificationDialog = true
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Criar Conta", color = colors.onPrimary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = {
                val intent = Intent(context, SignInActivity::class.java)
                context.startActivity(intent)
            }) {
                Text("Já tem uma conta? ", color = colors.onSurface)
                Text(
                    "Entrar",
                    color = colors.onSurface,
                    style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline)
                )
            }

            LaunchedEffect(message) {
                if (message.isNotEmpty()) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        //dialog de verificação de conta
        if (showVerificationDialog) {
            AlertDialog(
                onDismissRequest = { },
                confirmButton = {
                    Button(onClick = {
                        currentUser?.currentUser?.reload()?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                if (currentUser?.currentUser?.isEmailVerified == true) {
                                    showVerificationDialog = false
                                    val intent = Intent(context, SignInActivity::class.java)
                                    context.startActivity(intent)
                                } else {
                                    message = "Seu e-mail ainda não foi verificado. Verifique sua caixa de entrada."
                                }
                            } else {
                                message = "Erro ao verificar status do e-mail."
                            }
                        }
                    }) {
                        Text("Verificado", color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showVerificationDialog = false
                        val intent = Intent(context, SignInActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Text("Continuar sem verificar", color = Color.White)
                    }
                },
                title = { Text("Verificação de email", color = Color.White) },
                text = {
                    Text(
                        "Verifique seu email antes de fazer login para ter acesso a todas as funcionalidades do SuperID!",
                        color = Color.White
                    )
                },
                containerColor = Color(0xFF4B3D1F)
            )
        }
    }
}