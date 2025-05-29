
package com.projetointegrador3.superid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.projetointegrador3.superid.ui.theme.DarkPrimary
import com.projetointegrador3.superid.ui.theme.LightPrimary
import com.projetointegrador3.superid.ui.theme.SuperIDTheme
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase


class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this) {
            // Impede o usuário de voltar para activity anterior
        }
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen() {
    var searchText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val categoriasState = remember { mutableStateOf<List<String>>(emptyList()) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Inicia o listener do Firestore
    LaunchedEffect(Unit) {
        listenToCategories(context) { categorias ->
            categoriasState.value = categorias
        }
    }


    MaterialTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Column {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(R.drawable.superid_removebg),
                            contentDescription = "Logo SuperID",
                            modifier = Modifier
                                .height(124.dp)
                                .align(Alignment.Center)
                                .padding(2.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(16.dp)
                                .clickable {
                                    showLogoutDialog = true
                                }
                        )
                    }

                    Text(
                        "Categorias",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        verificarEmail(context){
                            val intent = Intent(context, QrScannerActivity::class.java)
                            context.startActivity(intent)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black
                ) {
                    Icon(imageVector = Icons.Rounded.QrCode, contentDescription = "QR Code")
                }
            },

            )
        { padding ->

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    confirmButton = {
                        Text(
                            "Sair",
                            modifier = Modifier
                                .clickable {
                                    signOut(context)
                                    showLogoutDialog = false
                                }
                                .padding(8.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    dismissButton = {
                        Text(
                            "Cancelar",
                            modifier = Modifier
                                .clickable { showLogoutDialog = false }
                                .padding(8.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    },
                    title = { Text("Confirmar Logout") },
                    text = { Text("Tem certeza que deseja sair da sua conta?") }
                )
            }


            // Box com fundo preto ou branco conforme o tema
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Lista dinâmica do Firestore
                items(categoriasState.value) { nomeCategoria ->
                    val icon = getIconForCategory(nomeCategoria)
                    Cards(nomeCategoria to icon, context)
                }

                // Card para adicionar nova categoria
                item {
                    Cards("Adicionar Categoria" to Icons.Default.Add, context)
                }
            }
        }
    }
}

fun verificarEmail(context: Context, onAutorizado: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser

    if (user != null) {
        Firebase.functions("southamerica-east1")
            .getHttpsCallable("checkEmailVerification")
            .call(hashMapOf("email" to user.email))
            .addOnSuccessListener { result ->
                val data = result.data as? Map<*, *>
                val isVerified = data?.get("verified") as? Boolean ?: false

                if (isVerified) {
                    onAutorizado()
                } else {
                    Toast.makeText(context, "Você precisa verificar seu e-mail para usar essa funcionalidade.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Erro ao verificar e-mail: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    } else {
        Toast.makeText(context, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
    }
}


fun getIconForCategory(nome: String): ImageVector {
    return when (nome) {
        "Sites Web" -> Icons.Default.Email
        "Aplicativos" -> Icons.Default.Favorite
        "Teclados Fisicos" -> Icons.Default.Menu
        "Codigos" -> Icons.Default.Lock
        else -> Icons.Default.Lock
    }
}


//Definição dos CARDS
@Composable
fun Cards(category: Pair<String, ImageVector>, context: Context) {
    val isDarkTheme = isSystemInDarkTheme()
    val cardColor = if (isDarkTheme) DarkPrimary else LightPrimary



    Card(
        modifier = Modifier
            .clickable {
                if (category.first == "Adicionar Categoria") {
                    context.startActivity(Intent(context, AddCategoryActivity::class.java))
                } else {
                    // Navegação para a tela de detalhes da categoria
                    val intent = Intent(context, CategoryDetailActivity::class.java)
                    intent.putExtra("categoryName", category.first)
                    context.startActivity(intent)
                }
            }
            .size(160.dp) // Tamanho quadrado
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = category.second,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(if (category.first == "Adicionar Categoria") 50.dp else 40.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = category.first,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun listenToCategories(context: Context, onDataChange: (List<String>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    if (user != null) {
        val userUid = user.uid

        db.collection("usuarios")
            .document(userUid)
            .collection("categorias")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(
                        context,
                        "Erro ao ouvir categorias: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val categorias =
                        snapshot.documents.map { it.id } // Pega o nome da categoria (ID do documento)
                    onDataChange(categorias)
                } else {
                    onDataChange(emptyList())
                }
            }
    } else {
        Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
    }
}


//  Barra de pesquisa de categorias
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar..."
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Ícone de busca"
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        )

    )
}

//logica pro logout

fun signOut(context: Context) {
    FirebaseAuth.getInstance().signOut()
    Toast.makeText(context, "Logout realizado", Toast.LENGTH_SHORT).show()

    val intent = Intent(context, SignInActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    context.startActivity(intent)
}


@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}