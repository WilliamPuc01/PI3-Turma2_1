package com.projetointegrador3.superid

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.projetointegrador3.superid.ui.theme.SuperIDTheme
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class CategoryDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val categoryName = intent.getStringExtra("categoryName") ?: ""

        setContent {
            SuperIDTheme {
                CategoryDetailScreen(categoryName = categoryName)
            }
        }
    }
}

data class Senha(
    val id: String,
    val usuario: String?,
    val descricao: String?,
    val senhaCriptografada: String?
)

fun loadSenhas(
    context: Context,
    categoryName: String,
    onResult: (List<Senha>) -> Unit
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
                Senha(
                    id = doc.id,
                    usuario = doc.getString("usuario"),
                    descricao = doc.getString("descricao"),
                    senhaCriptografada = doc.getString("senha")
                )
            }
            onResult(lista)
        }
}

fun saveSenha(
    context: Context,
    categoryName: String,
    usuario: String?,
    descricao: String?,
    senha: String,
    accessToken: String,
    onComplete: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser ?: return

    val novaSenha = hashMapOf(
        "usuario" to usuario,
        "descricao" to descricao,
        "senha" to senha,
        "AccessToken" to accessToken
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

object EncryptionUtils {

    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"


    fun generateAccessToken(): String {
        val randomBytes = ByteArray(192) // 192 bytes codificados ≈ 256 caracteres em Base64
        SecureRandom().nextBytes(randomBytes)
        return Base64.getEncoder().encodeToString(randomBytes)
    }

    fun generateFixedKey(): SecretKeySpec {
        val keyBytes = ByteArray(32) { 0x01 }
        return SecretKeySpec(keyBytes, "AES")
    }


    fun encrypt(data: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = ByteArray(cipher.blockSize)
        SecureRandom().nextBytes(iv)
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        val ivAndEncryptedData = iv + encryptedBytes
        return Base64.getEncoder().encodeToString(ivAndEncryptedData)
    }

    fun decrypt(encryptedData: String, secretKey: SecretKey): String {
        val ivAndEncryptedData = Base64.getDecoder().decode(encryptedData)
        val iv = ivAndEncryptedData.copyOfRange(0, 16)
        val encryptedBytes = ivAndEncryptedData.copyOfRange(16, ivAndEncryptedData.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }
}

fun deleteSenha(
    context: Context,
    categoryName: String,
    usuario: String?,
    descricao: String?,
    senhaCriptografada: String,
    onComplete: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser ?: return

    val categoriaRef = db.collection("usuarios")
        .document(user.uid)
        .collection("categorias")
        .document(categoryName)
        .collection(categoryName)

    categoriaRef
        .whereEqualTo("usuario", usuario)
        .whereEqualTo("descricao", descricao)
        .whereEqualTo("senha", senhaCriptografada)
        .get()
        .addOnSuccessListener { result ->
            for (document in result) {
                categoriaRef.document(document.id).delete()
            }
            Toast.makeText(context, "Senha excluída com sucesso", Toast.LENGTH_SHORT).show()
            onComplete()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Erro ao excluir senha", Toast.LENGTH_SHORT).show()
        }
}

fun updateSenha(
    context: Context,
    categoryName: String,
    senhaItem: Senha,
    novaSenhaCriptografada: String,
    novoUsuario: String?,
    novaDescricao: String?,
    accessToken: String,
    onComplete: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser ?: return

    val senhaAtualizada = hashMapOf(
        "usuario" to novoUsuario,
        "descricao" to novaDescricao,
        "senha" to novaSenhaCriptografada,
        "acessToken" to accessToken
    )

    db.collection("usuarios")
        .document(user.uid)
        .collection("categorias")
        .document(categoryName)
        .collection(categoryName)
        .document(senhaItem.id)
        .set(senhaAtualizada)
        .addOnSuccessListener {
            Toast.makeText(context, "Senha atualizada com sucesso", Toast.LENGTH_SHORT).show()
            onComplete()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Erro ao atualizar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

fun updateCategoria(
    context: Context,
    categoriaAtual: String,
    novoNome: String,
    onComplete: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser ?: return

    val userRef = db.collection("usuarios").document(user.uid)
    val categoriaRef = userRef.collection("categorias").document(categoriaAtual)

    categoriaRef.get().addOnSuccessListener { document ->
        if (document.exists()) {
            val dadosCategoria = document.data ?: return@addOnSuccessListener

            // Copia os dados para o novo nome
            userRef.collection("categorias").document(novoNome).set(dadosCategoria)
                .addOnSuccessListener {
                    // Agora copia as senhas
                    categoriaRef.collection(categoriaAtual).get()
                        .addOnSuccessListener { snapshot ->
                            val batch = db.batch()
                            snapshot.documents.forEach { senhaDoc ->
                                val novaRef = userRef.collection("categorias")
                                    .document(novoNome)
                                    .collection(novoNome)
                                    .document(senhaDoc.id)
                                batch.set(novaRef, senhaDoc.data ?: return@forEach)
                            }

                            batch.commit().addOnSuccessListener {
                                // Remove categoria antiga
                                categoriaRef.delete().addOnSuccessListener {
                                    onComplete()
                                    Toast.makeText(context, "Categoria renomeada com sucesso", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                }
        }
    }
}

fun deleteCategoria(
    context: Context,
    nomeCategoria: String,
    onComplete: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser ?: return

    val categoriaRef = db.collection("usuarios").document(user.uid)
        .collection("categorias").document(nomeCategoria)

    categoriaRef.collection(nomeCategoria).get()
        .addOnSuccessListener { result ->
            val batch = db.batch()
            for (document in result) {
                batch.delete(categoriaRef.collection(nomeCategoria).document(document.id))
            }
            batch.commit().addOnSuccessListener {
                categoriaRef.delete().addOnSuccessListener {
                    Toast.makeText(context, "Categoria excluída", Toast.LENGTH_SHORT).show()
                    onComplete()
                }
            }
        }
}

@Composable
fun CategoryDetailScreen(categoryName: String) {
    val context = LocalContext.current
    val senhasState = remember { mutableStateOf<List<Senha>>(emptyList()) }
    val showAddSenhaDialog = remember { mutableStateOf(false) }
    val senhaParaEditar = remember { mutableStateOf<Senha?>(null) }
    val showDeleteConfirmationDialog = remember { mutableStateOf(false) }
    val senhaParaExcluir = remember { mutableStateOf<Senha?>(null) }
    val secretKey = remember { EncryptionUtils.generateFixedKey() }
    val showEditCategoryDialog = remember { mutableStateOf(false) }
    val showDeleteCategoryDialog = remember { mutableStateOf(false) }
    var accessToken by remember { mutableStateOf(EncryptionUtils.generateAccessToken()) }


    val colors = MaterialTheme.colorScheme

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
                containerColor = colors.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar senha")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    IconButton(
                        onClick = { (context as? Activity)?.finish() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = colors.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.onSurface,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(2.dp))

                    // Não mostra na categoria "Sites Web" por ser padrão e obrigatória
                    if (categoryName != "Sites Web") {
                        IconButton(onClick = { showEditCategoryDialog.value = true }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar Categoria", tint = colors.primary)
                        }
                        IconButton(onClick = { showDeleteCategoryDialog.value = true }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Excluir Categoria", tint = Color.Red)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (senhasState.value.isEmpty()) {
                    Text(
                        text = "Nenhuma senha cadastrada.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurface,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn {
                        items(senhasState.value) { senhaItem ->
                            val decrypted = try {
                                EncryptionUtils.decrypt(
                                    senhaItem.senhaCriptografada ?: "",
                                    secretKey
                                )
                            } catch (e: Exception) {
                                "Erro"
                            }

                            SenhaCard(
                                senha = senhaItem,
                                decryptedSenha = decrypted,
                                onEditar = { senhaParaEditar.value = senhaItem },
                                onExcluir = {
                                    senhaParaExcluir.value = senhaItem
                                    showDeleteConfirmationDialog.value = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddSenhaDialog.value) {
        AddSenhaDialog(
            categoryName = categoryName,
            onDismiss = { showAddSenhaDialog.value = false },
            onSave = { usuario, descricao, senha, accessToken ->
                saveSenha(context, categoryName, usuario, descricao, senha, accessToken) {
                    showAddSenhaDialog.value = false
                    loadSenhas(context, categoryName) { senhas ->
                        senhasState.value = senhas
                    }
                }
            }
        )
    }

    senhaParaEditar.value?.let { senha ->
        EditSenhaDialog(
            categoryName = categoryName,
            currentUsuario = senha.usuario,
            currentDescricao = senha.descricao,
            currentSenhaCriptografada = senha.senhaCriptografada ?: "",
            onDismiss = { senhaParaEditar.value = null },
            onSave = { novoUsuario, novaDescricao, novaSenhaCriptografada, acessToken ->
                updateSenha(
                    context, categoryName, senha,
                    novaSenhaCriptografada, novoUsuario, novaDescricao, accessToken
                ) {
                    senhaParaEditar.value = null
                    loadSenhas(context, categoryName) { senhas ->
                        senhasState.value = senhas
                    }
                }
            }
        )
    }

    if (showDeleteConfirmationDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog.value = false },
            title = { Text("Excluir senha") },
            text = { Text("Tem certeza que deseja excluir esta senha?") },
            confirmButton = {
                TextButton(onClick = {
                    senhaParaExcluir.value?.let { senha ->
                        deleteSenha(
                            context,
                            categoryName,
                            senha.usuario,
                            senha.descricao,
                            senha.senhaCriptografada ?: ""
                        ) {
                            loadSenhas(context, categoryName) {
                                senhasState.value = it
                            }
                        }
                    }
                    showDeleteConfirmationDialog.value = false
                }) {
                    Text("Confirmar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmationDialog.value = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    if (showEditCategoryDialog.value) {
        var novoNome by remember { mutableStateOf(categoryName) }
        AlertDialog(
            onDismissRequest = { showEditCategoryDialog.value = false },
            title = { Text("Renomear Categoria") },
            text = {
                TextField(
                    value = novoNome,
                    onValueChange = { novoNome = it },
                    label = { Text("Novo nome da categoria") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    updateCategoria(context, categoryName, novoNome) {
                        (context as? Activity)?.finish()
                    }
                    showEditCategoryDialog.value = false
                }) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditCategoryDialog.value = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    if (showDeleteCategoryDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteCategoryDialog.value = false },
            title = { Text("Excluir Categoria") },
            text = { Text("Tem certeza que deseja excluir esta categoria e todas as senhas dentro dela?") },
            confirmButton = {
                TextButton(onClick = {
                    deleteCategoria(context, categoryName) {
                        (context as? Activity)?.finish()
                    }
                    showDeleteCategoryDialog.value = false
                }) {
                    Text("Confirmar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCategoryDialog.value = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}


//dialog para criar uma nova senha na categoria
@Composable
fun AddSenhaDialog(
    categoryName: String,
    onDismiss: () -> Unit,
    onSave: (String?, String?, String, String) -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    val secretKey = remember { EncryptionUtils.generateFixedKey() }
    var accessToken by remember { mutableStateOf(EncryptionUtils.generateAccessToken()) }


    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Adicionar Senha") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            TextButton(
                onClick = {
                    val encryptedSenha = EncryptionUtils.encrypt(senha, secretKey)

                    if (encryptedSenha.isNotBlank()) {
                        onSave(
                            usuario.takeIf { it.isNotBlank() },
                            descricao.takeIf { it.isNotBlank() },
                            encryptedSenha,
                            accessToken
                        )
                    }
                }
            ) {
                Text(text ="Salvar",
                    color = MaterialTheme.colorScheme.onBackground)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text ="Cancelar",
                    color = MaterialTheme.colorScheme.onBackground)
            }
        }
    )
}

//Dialog pra editar uma senha
@Composable
fun EditSenhaDialog(
    categoryName: String,
    currentUsuario: String?,
    currentDescricao: String?,
    currentSenhaCriptografada: String,
    onDismiss: () -> Unit,
    onSave: (String?, String?, String, String) -> Unit
) {
    var usuario by remember { mutableStateOf(currentUsuario ?: "") }
    var descricao by remember { mutableStateOf(currentDescricao ?: "") }
    var accessToken by remember { mutableStateOf(EncryptionUtils.generateAccessToken()) }
    val decryptedSenha = try {
        EncryptionUtils.decrypt(currentSenhaCriptografada, EncryptionUtils.generateFixedKey())
    } catch (e: Exception) {
        ""
    }
    var senha by remember { mutableStateOf(decryptedSenha) }
    var senhaVisivel by remember { mutableStateOf(false) }

    val secretKey = remember { EncryptionUtils.generateFixedKey() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Editar Senha") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    value = if (senhaVisivel) senha else "•".repeat(senha.length),
                    onValueChange = { senha = it },
                    label = { Text("Senha") },
                    singleLine = true,
                    trailingIcon = {
                        TextButton(onClick = { senhaVisivel = !senhaVisivel }) {
                            Text( text = if (senhaVisivel) "Ocultar" else "Mostrar",
                                color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val senhaCriptografada = EncryptionUtils.encrypt(senha, secretKey)
                onSave(
                    usuario.takeIf { it.isNotBlank() },
                    descricao.takeIf { it.isNotBlank() },
                    senhaCriptografada,
                    accessToken
                )
            }) {
                Text(
                    text =  "Salvar",
                    color = MaterialTheme.colorScheme.onBackground)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar",
                    color = MaterialTheme.colorScheme.onBackground)
            }
        }
    )
}

//DEFINIÇÃO DOS CARDS DE SENHA
@Composable
fun SenhaCard(
    senha: Senha,
    decryptedSenha: String,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = senha.usuario ?: "-",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Descrição: ${senha.descricao ?: "-"}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Senha: ${
                    if (showPassword)
                        decryptedSenha
                    else
                        "•".repeat(decryptedSenha.length.coerceAtMost(16))
                }",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface
            )

            Text(
                text = if (showPassword) "Ocultar" else "Mostrar",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = colors.onPrimary,
                    textDecoration = TextDecoration.Underline
                ),
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clickable { showPassword = !showPassword }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onEditar) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = colors.onPrimary
                    )
                }
                IconButton(onClick = onExcluir) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun CategoryDetailScreenPreview() {
    CategoryDetailScreen("Categoria")
}