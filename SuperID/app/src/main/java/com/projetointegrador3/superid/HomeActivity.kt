
package com.projetointegrador3.superid

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
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
import com.projetointegrador3.superid.ui.theme.SuperIDTheme


class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                HomeScreen()
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun HomeScreen() {
    val context = LocalContext.current
    // Só alterar aqui se quiser mudar o tema do app!!!

    val customColors = darkColorScheme(
        primary = Color(0xFFD4AF37), // Dourado
        surface = Color(0xFF121212), // Preto
        onSurface = Color.White
    )

    MaterialTheme(colorScheme = customColors) {
        //ESTRUTURA BASICA DA TELA HOME DO APP
        Scaffold(
            modifier = Modifier.fillMaxSize(),

            //cabeçalho com o logo do app
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
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                        )
                    )
                }

            }, floatingActionButton = {
                FloatingActionButton(
                    onClick = { /* Todo: funcionalide de adicionar senhas */ },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Adicionar senha",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )
        
        //CONTEUDO PRINCIPAL
        { padding ->

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
                        .alpha(0.2f)
                )}
            // Grade das categorias num formato 2x2
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                //transforma os itens da lista de categorias em um card
                items(categories) { category ->
                    Cards(category, context)
                }
            }
        }
    }
}

// Categorias do app e seus icones, só add aqui se quiser uma nova
val categories = listOf(
    "Sites Web" to Icons.Default.Email,
    "Aplicativos" to Icons.Default.Favorite,
    "Teclados Físicos" to Icons.Default.Menu,
    "Códigos" to Icons.Default.Lock,
    "Adicionar Categoria" to Icons.Default.Add
)


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