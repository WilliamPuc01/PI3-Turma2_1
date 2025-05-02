
package com.projetointegrador3.superid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.projetointegrador3.superid.ui.theme.SuperIDTheme


class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                MainNavigation()
            }
        }
    }
}

@Composable
//@Preview
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController)
        }
        composable("detail/{categoryName}") { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            CategoryDetailScreen(navController, categoryName)
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    var searchText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val categoriasState = remember { mutableStateOf<List<String>>(emptyList()) }

    // Inicia o listener do Firestore
    LaunchedEffect(Unit) {
        listenToCategories(context) { categorias ->
            categoriasState.value = categorias
        }
    }


    MaterialTheme{
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
                    Icon(Icons.Default.Add, contentDescription = "Entrar com o SuperID")
                }
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                ) {
                    NavigationBarItem(
                        icon = {
                            Icon(

                                imageVector = Icons.Default.Home,
                                contentDescription = null
                            )
                        },
                        label = {
                            Text(
                                "Categoria"
                            )
                        },
                        selected = true,
                        /* Fazer com que retorne para pagina de categoria */
                        onClick = {}
                    )
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null
                            )
                        },
                        label = {
                            Text(
                                "Senhas"
                            )
                        },
                        selected = false,
                        /*Criar uma outra activity, uma ideia parecida do Authenticator da
                          Microsoft */
                        onClick = {}
                    )
                }
            }
        ) { padding ->

            Box(modifier = Modifier.fillMaxSize()) {
            BackgroundImage()
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
                    Cards(nomeCategoria to icon, context, navController)
                }

                // Card para adicionar nova categoria
                item {
                    Cards("Adicionar Categoria" to Icons.Default.Add, context, navController)
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
fun Cards(category: Pair<String, ImageVector>, context: Context, navController: NavController) {
    Card(
        modifier = Modifier.clickable {
            if (category.first == "Adicionar Categoria") {
                context.startActivity(Intent(context, AddCategoryActivity::class.java))
            } else {
                // Navegação para a tela de detalhes da categoria
                navController.navigate("detail/${category.first}")
            }
        }
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

fun loadSenhas(
    context: Context,
    categoryName: String,
    onResult: (List<Triple<String?, String?, String?>>) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser ?: return

    db.collection("usuarios")
        .document(user.uid)
        .collection("categorias")
        .document(categoryName)
        .collection(categoryName)
        .get()
        .addOnSuccessListener { result ->
            val lista = result.documents.map { doc ->
                val usuario = doc.getString("usuario")
                val descricao = doc.getString("descricao")
                val senha = doc.getString("senha")
                Triple(usuario, descricao, senha)
            }.filter { it.third != null }
            onResult(lista)
        }
}

fun saveSenha(
    context: Context,
    categoryName: String,
    usuario: String?,
    descricao: String?,
    senha: String,
    onComplete: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser ?: return

    val novaSenha = hashMapOf(
        "usuario" to usuario,
        "descricao" to descricao,
        "senha" to senha
    )

    db.collection("usuarios")
        .document(user.uid)
        .collection("categorias")
        .document(categoryName)
        .collection(categoryName)
        .add(novaSenha)
        .addOnSuccessListener {
            Toast.makeText(context, "Senha adicionada com sucesso!", Toast.LENGTH_SHORT).show()
            onComplete()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Erro ao salvar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

@Composable
fun CategoryDetailScreen(navController: NavController, categoryName: String) {
    val context = LocalContext.current
    val senhasState = remember { mutableStateOf<List<Triple<String?, String?, String?>>>(emptyList()) }
    val showAddSenhaDialog = remember { mutableStateOf(false) }

    //variavel de padronização do tema
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        BackgroundImage()

        // Carregar senhas ao entrar
        LaunchedEffect(categoryName) {
            loadSenhas(context, categoryName) { senhas ->
                senhasState.value = senhas
            }
        }

        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddSenhaDialog.value = true },
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar senha")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Botão de voltar
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = colors.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    "Categoria: $categoryName",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (senhasState.value.isEmpty()) {
                    Text("Nenhuma senha cadastrada ainda.", color = colors.onSurface)
                } else {
                    senhasState.value.forEach { (usuario, descricao, senha) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colors.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Usuário: ${usuario ?: "-"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Descrição: ${descricao ?: "-"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Senha: ${senha ?: "-"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Diálogo de adicionar senha
        if (showAddSenhaDialog.value) {
            AddSenhaDialog(
                categoryName = categoryName,
                onDismiss = { showAddSenhaDialog.value = false },
                onSave = { usuario, descricao, senha ->
                    saveSenha(
                        context = context,
                        categoryName = categoryName,
                        usuario = usuario,
                        descricao = descricao,
                        senha = senha
                    ) {
                        showAddSenhaDialog.value = false
                        // Recarrega as senhas
                        loadSenhas(context, categoryName) { senhas ->
                            senhasState.value = senhas
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun AddSenhaDialog(
    categoryName: String,
    onDismiss: () -> Unit,
    onSave: (String?, String?, String) -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Senha") },
        text = {
            Column {
                TextField(
                    value = usuario,
                    onValueChange = { usuario = it },
                    label = { Text("Usuário (opcional)") }
                )
                TextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição (opcional)") }
                )
                TextField(
                    value = senha,
                    onValueChange = { senha = it },
                    label = { Text("Senha") }
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    if (senha.isNotBlank()) {
                        onSave(
                            usuario.takeIf { it.isNotBlank() },
                            descricao.takeIf { it.isNotBlank() },
                            senha
                        )
                    }
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun BackgroundImage() {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundImage = if (isDarkTheme) {
        painterResource(R.drawable.fundo)
    } else {
        painterResource(R.drawable.fundo_claro_things)
    }

    Image(
        painter = backgroundImage,
        contentDescription = "Fundo do app",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.2f)
    )
}
