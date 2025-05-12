package com.projetointegrador3.superid

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lint.kotlin.metadata.Visibility
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import com.projetointegrador3.superid.BackgroundImage
import com.projetointegrador3.superid.ui.theme.BotaoDourado
import com.projetointegrador3.superid.ui.theme.CardDarkBackground
import com.projetointegrador3.superid.ui.theme.CardLightBackground
import com.projetointegrador3.superid.ui.theme.SuperIDTheme

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

object EncryptionUtils {

    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"

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
    onComplete: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser ?: return

    val senhaAtualizada = hashMapOf(
        "usuario" to novoUsuario,
        "descricao" to novaDescricao,
        "senha" to novaSenhaCriptografada
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

@Composable
fun CategoryDetailScreen(categoryName: String) {
    val context = LocalContext.current
    val senhasState = remember { mutableStateOf<List<Senha>>(emptyList()) }
    val showAddSenhaDialog = remember { mutableStateOf(false) }
    val senhaParaEditar = remember { mutableStateOf<Senha?>(null) }
    val showDeleteConfirmationDialog = remember { mutableStateOf(false) }
    val senhaParaExcluir = remember { mutableStateOf<Senha?>(null) }

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
                    onClick = { (context as? android.app.Activity)?.finish() },
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


                val secretKey = remember { EncryptionUtils.generateFixedKey() }

                if (senhasState.value.isEmpty()) {
                    Text("Nenhuma senha cadastrada ainda.", color = colors.onSurface)
                } else {
                    senhasState.value.forEach { senhaItem ->
                        val decryptedSenha = try {
                            EncryptionUtils.decrypt(senhaItem.senhaCriptografada ?: "", secretKey)
                        } catch (e: Exception) {
                            "Erro ao descriptografar"
                        }

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
                                    "Usuário: ${senhaItem.usuario ?: "-"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Descrição: ${senhaItem.descricao ?: "-"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                var senhaVisivel by remember { mutableStateOf(false) }

                                if (senhaVisivel) {
                                    Text(
                                        "Senha: $decryptedSenha",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.onSurface
                                    )
                                } else {
                                    Text(
                                        "Senha: •••••••••",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.onSurface
                                    )
                                }

                                TextButton(onClick = { senhaVisivel = !senhaVisivel }) {
                                    Text(if (senhaVisivel) "Ocultar" else "Mostrar")
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Botões de editar e excluir
                                Row {
                                    IconButton(onClick = {
                                        senhaParaEditar.value = senhaItem
                                    }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Editar",
                                            tint = colors.primary
                                        )
                                    }
                                    IconButton(onClick = {
                                        senhaParaExcluir.value = senhaItem
                                        showDeleteConfirmationDialog.value = true  // Exibe o AlertDialog de confirmação
                                    }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Excluir",
                                            tint = Color.Red
                                        )
                                    }
                                }
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
        senhaParaEditar.value?.let { senhaItem ->
            EditSenhaDialog(
                categoryName = categoryName,
                currentUsuario = senhaItem.usuario ?: "",
                currentDescricao = senhaItem.descricao ?: "",
                currentSenhaCriptografada = senhaItem.senhaCriptografada ?: "",
                onDismiss = { senhaParaEditar.value = null },
                onSave = { novoUsuario, novaDescricao, novaSenhaCriptografada ->
                    updateSenha(
                        context = context,
                        categoryName = categoryName,
                        senhaItem = senhaItem,
                        novaSenhaCriptografada = novaSenhaCriptografada,
                        novoUsuario = novoUsuario,
                        novaDescricao = novaDescricao
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
                title = { Text("Excluir Senha") },
                text = { Text("Tem certeza de que deseja excluir esta senha?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            senhaParaExcluir.value?.let { senhaItem ->
                                // Chama a função de exclusão
                                deleteSenha(
                                    context,
                                    categoryName,
                                    senhaItem.usuario,
                                    senhaItem.descricao,
                                    senhaItem.senhaCriptografada ?: ""
                                ) {
                                    // Atualiza a lista de senhas
                                    loadSenhas(context, categoryName) { senhas ->
                                        senhasState.value = senhas
                                    }
                                }
                            }
                            showDeleteConfirmationDialog.value = false // Fecha o diálogo
                        }
                    ) {
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
    var encryptedSenha by remember { mutableStateOf("") }
    val secretKey = remember { EncryptionUtils.generateFixedKey() }

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
                    encryptedSenha = EncryptionUtils.encrypt(senha, secretKey)
                    if (encryptedSenha.isNotBlank()) {
                        onSave(
                            usuario.takeIf { it.isNotBlank() },
                            descricao.takeIf { it.isNotBlank() },
                            encryptedSenha
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
fun EditSenhaDialog(
    categoryName: String,
    currentUsuario: String?,
    currentDescricao: String?,
    currentSenhaCriptografada: String,
    onDismiss: () -> Unit,
    onSave: (String?, String?, String) -> Unit
) {
    var usuario by remember { mutableStateOf(currentUsuario ?: "") }
    var descricao by remember { mutableStateOf(currentDescricao ?: "") }
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
        title = { Text("Editar Senha") },
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
                    value = if (senhaVisivel) senha else "•".repeat(senha.length),
                    onValueChange = { senha = it },
                    label = { Text("Senha") },
                    singleLine = true,
                    trailingIcon = {
                        TextButton(onClick = { senhaVisivel = !senhaVisivel }) {
                            Text(if (senhaVisivel) "Ocultar" else "Mostrar")
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
                    senhaCriptografada
                )
            }) {
                Text("Salvar")
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
@Preview
fun CategoryDetailScreenPreview() {
    CategoryDetailScreen("Categoria")
}