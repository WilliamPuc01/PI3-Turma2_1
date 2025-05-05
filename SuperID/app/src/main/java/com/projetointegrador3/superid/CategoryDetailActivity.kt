package com.projetointegrador3.superid

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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
            CategoryDetailScreen(categoryName = categoryName)
        }
    }
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

@Composable
fun CategoryDetailScreen(categoryName: String) {
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
                    senhasState.value.forEach { (usuario, descricao,senhaCriptografada) ->
                        val decryptedSenha = try {
                            EncryptionUtils.decrypt(senhaCriptografada ?: "", secretKey)
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
                                    "Senha: $decryptedSenha",
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