package com.projetointegrador3.superid

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddCategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AddCategoryScreen()
        }
    }
}


fun saveCategory(categoryName: String, context: Context, onResult: (Boolean) -> Unit = {}) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    if (user != null) {
        val userUid = user.uid

        val categoria = hashMapOf(
            "nome" to categoryName
        )

        db.collection("usuarios")
            .document(userUid) // UID correto do usuário
            .collection("categorias")
            .document(categoryName) // Nome da categoria como ID
            .set(categoria)
            .addOnSuccessListener {
                Toast.makeText(context, "Categoria salva com sucesso!", Toast.LENGTH_SHORT).show()
                onResult(true)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao salvar categoria", Toast.LENGTH_SHORT).show()
                onResult(false)
            }
    } else {
        Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
        onResult(false)
    }
}


@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen() {
    var categoryName by remember { mutableStateOf("") }
    val context = LocalContext.current
    val navController = rememberNavController()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        Image(
            painter = painterResource(id = R.drawable.fundo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Ícone de voltar
        IconButton(
            onClick = { (context as? ComponentActivity)?.finish() },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(32.dp)
            )
        }


            // Conteúdo principal centralizado
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)

            ) {
                Text(
                    text = "Criar categoria",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                TextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    placeholder = {
                        Text(text = "Nome da categoria", color = Color.LightGray)
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xFF2C2C2C),
                        cursorColor = Color(0xFFFFC107),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        // Aqui ce vai implementar a logica nier
                        if (categoryName.isNotBlank()) {
                            saveCategory(categoryName, context)
                        } else {
                            Toast.makeText(context, "Nome da categoria não pode estar vazio", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC107), // Botão dourado
                        contentColor = Color.Black // Texto preto
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Criar", fontSize = 18.sp)
                }
            }
        }
    }

