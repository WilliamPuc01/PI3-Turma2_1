
package com.projetointegrador3.superid

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.projetointegrador3.superid.ui.theme.SuperIDTheme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                HomeScreen()
            }
        }
    }
}




@Composable
@Preview
fun HomeScreen() {
    var searchText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val categoriasState = remember { mutableStateOf<List<String>>(emptyList()) }

    // Inicia o listener do Firestore
    LaunchedEffect(Unit) {
        listenToCategories(context) { categorias ->
            categoriasState.value = categorias
        }
    }

    val customColors = darkColorScheme(
        primary = Color(0xFFD4AF37),
        surface = Color(0xFF121212),
        onSurface = Color.White
    )

    MaterialTheme(colorScheme = customColors) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Column {
                    Image(
                        painter = painterResource(R.drawable.superid_removebg),
                        contentDescription = "Logo SuperID",
                        modifier = Modifier
                            .height(124.dp)
                            .align(Alignment.CenterHorizontally)
                            .padding(2.dp)
                    )
                    Text(
                        "Senhas",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    SearchBar(
                        query = searchText,
                        onQueryChange = { searchText = it }
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { /* funcionalidade futura */ },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar senha")
                }
            }
        ) { padding ->

            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(R.drawable.fundo),
                    contentDescription = "Fundo do app",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.2f)
                )
            }

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

fun getIconForCategory(nome: String): ImageVector {
    return when (nome) {
        "Sites Web" -> Icons.Default.Email
        "Aplicativos" -> Icons.Default.Favorite
        "Teclados Fisicos" -> Icons.Default.Menu
        "Codigos" -> Icons.Default.Lock
        else -> Icons.Default.Lock // Ícone padrão
    }
}




//Definição dos CARDS
@Composable
fun Cards(category: Pair<String, ImageVector>, context: android.content.Context) {
    Card(

        modifier = Modifier.clickable { if (category.first == "Adicionar Categoria") {
            context.startActivity(Intent(context, AddCategoryActivity::class.java))
        } else {
            // Aqui você pode colocar ações para as outras categorias depois, se quiser
        } }
            .size(160.dp) // Tamanho quadrado
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    )
    //Conteudo do CARD
    {
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
                tint = MaterialTheme.colorScheme.primary,
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
                    Toast.makeText(context, "Erro ao ouvir categorias: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val categorias = snapshot.documents.map { it.id } // Pega o nome da categoria (ID do documento)
                    onDataChange(categorias)
                } else {
                    onDataChange(emptyList())
                }
            }
    } else {
        Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
    }
}



@OptIn(ExperimentalMaterial3Api::class)
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



