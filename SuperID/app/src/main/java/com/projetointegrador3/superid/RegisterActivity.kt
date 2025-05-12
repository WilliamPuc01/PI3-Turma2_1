package com.projetointegrador3.superid

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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

fun createAccount(name:String, email: String, password:String, context: android.content.Context, onResult: (String) -> Unit){
    val auth = Firebase.auth
    val db = Firebase.firestore
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val userId = user?.uid

                // Envia o e-mail de verificação
                user?.sendEmailVerification()
                    ?.addOnCompleteListener { verifyTask ->
                        if (verifyTask.isSuccessful) {
                            onResult("Conta criada! Verifique seu e-mail para ativá-la.")
                        } else {
                            onResult("Conta criada, mas falha ao enviar e-mail de verificação.")
                        }
                    }

                // Salva dados no Firestore
                val userMap = hashMapOf("nome" to name, "email" to email)
                userId?.let {id ->
                    db.collection("usuarios").document(id)
                        .set(userMap)
                        .addOnSuccessListener {
                            println("Dados do usuário salvos com sucesso!")
                            createDefaultCategories(id)
                        }
                        .addOnFailureListener { e ->
                            println("Erro ao salvar dados: ${e.message}")
                        }
                }

                // Redireciona para tela de login
                val intent = Intent(context, SignInActivity::class.java)
                context.startActivity(intent)

            } else {
                val exception = task.exception
                val errorMessage = when ((exception as? com.google.firebase.auth.FirebaseAuthException)?.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "Este e-mail já está em uso."
                    "ERROR_INVALID_EMAIL" -> "E-mail inválido."
                    "ERROR_WEAK_PASSWORD" -> "A senha deve ter pelo menos 6 caracteres."
                    else -> "Erro ao criar conta: ${exception?.localizedMessage}"
                }
                onResult(errorMessage)
            }
        }
}

fun createDefaultCategories(userId: String) {
    val db = Firebase.firestore
    val categorias = listOf("Sites Web", "Aplicativos", "Teclados Físicos")

    categorias.forEach { categoryName ->
        db.collection("usuarios")
            .document(userId)
            .collection("categorias")
            .document(categoryName)
            .set(hashMapOf<String, Any>()) // Sem campos no começo
    }
}

@Composable
fun RegisterScreen(modifier: Modifier = Modifier.fillMaxSize()) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val context = LocalContext.current

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
                painter = painterResource(id = R.drawable.superid_removebg),
                contentDescription = "Logo SuperID",
                modifier = Modifier
                    .height(180.dp)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )

            // Título
            Text("Criar Conta", fontSize = 30.sp, color = MaterialTheme.colorScheme.onSurface)

            Spacer(modifier = Modifier.height(20.dp))

            // Campo Nome
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome completo") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Botão Criar Conta
            Button(
                onClick = {
                    createAccount(name, email, password, context) { result -> message = result }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(text = "Criar Conta", color = Color.Black)
            }

            // Mensagem de feedback
            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = message, color = MaterialTheme.colorScheme.onPrimary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Voltar para Login
            TextButton(
                onClick = {
                    val intent = Intent(context, SignInActivity::class.java)
                    context.startActivity(intent)
                }
            ) {
                if (isSystemInDarkTheme()) {
                    Text("Já tem uma conta? ", color = Color.White)
                    Text(
                        "Entrar",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = TextDecoration.Underline
                        )
                    )
                } else {
                    Text("Já tem uma conta? ", color = Color.Black)
                    Text(
                        "Entrar",
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = TextDecoration.Underline
                        )
                    )
                }
            }
        }
    }
}

