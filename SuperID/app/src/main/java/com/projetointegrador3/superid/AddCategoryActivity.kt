package com.projetointegrador3.superid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.projetointegrador3.superid.ui.theme.SuperIDTheme

class AddCategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperIDTheme {
                AddCategoryScreen()
            }
        }
    }
}


fun saveCategory(categoryName: String, context: Context, onResult: (Boolean) -> Unit = {}) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    if (user != null) {
        val userUid = user.uid

        // Salva o nome da categoria para futuramente adicionar senhas
        db.collection("usuarios")
            .document(userUid) // UID correto do usuário
            .collection("categorias")
            .document(categoryName) // Nome da categoria como ID
            .set(hashMapOf<String, Any>())
            .addOnSuccessListener {
                Toast.makeText(context, "Categoria salva com sucesso!", Toast.LENGTH_SHORT).show()
                onResult(true)
                val intent = Intent(context, HomeActivity::class.java)
                context.startActivity(intent)
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


fun checkIfCategoryExists(
    categoryName: String,
    context: Context,
    onExists: () -> Unit,
    onNotExists: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    if (user != null) {
        val userUid = user.uid
        val docRef = db.collection("usuarios")
            .document(userUid)
            .collection("categorias")
            .document(categoryName)

        docRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                onExists()
            } else {
                onNotExists()
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Erro ao verificar categoria", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
    }
}

// Caso o usuário tente criar uma categoria com um nome já existente
@Composable
fun ConfirmDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            },
            title = { Text("Categoria já existe") },
            text = { Text("Já existe uma categoria com esse nome. Deseja criar mesmo assim?") }
        )
    }
}


@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen() {
    var categoryName by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    //variaveis de padronização do tema e fundo
    val colors = MaterialTheme.colorScheme
    val isDarkTheme = isSystemInDarkTheme()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        BackgroundImage()

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
                tint = colors.primary,
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
                    color = colors.onBackground
                )

                Spacer(modifier = Modifier.height(24.dp))

                TextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    placeholder = {
                        Text(text = "Nome da categoria", color = colors.onBackground)
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = colors.surfaceVariant,
                        cursorColor = colors.primary,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = colors.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        // Aqui ce vai implementar a logica nier
                        if (categoryName.isNotBlank()) {
                            checkIfCategoryExists(categoryName, context, onExists = { showDialog = true },
                                onNotExists = { saveCategory(categoryName, context) }
                            )
                        } else {
                            Toast.makeText(context, "Nome da categoria não pode estar vazio", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Criar", fontSize = 18.sp)
                }
            }
        // Dialogo de confirmação
        ConfirmDialog(
            showDialog = showDialog,
            onDismiss = { showDialog = false },
            onConfirm = {
                saveCategory(categoryName, context)
                showDialog = false
            }
        )
    }
}


