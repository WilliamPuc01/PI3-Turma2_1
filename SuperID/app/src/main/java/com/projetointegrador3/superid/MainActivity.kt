package com.projetointegrador3.superid

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.projetointegrador3.superid.permissions.WithPermission
import com.projetointegrador3.superid.ui.theme.SuperIDTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        super.onCreate(savedInstanceState)

        // Verifica se o usuário já aceitou os termos
        if (!primeiraVez(this)) {
            // Vai direto para a tela de login
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish() // Fecha a MainActivity para não voltar
        } else {
            // Exibe a tela de boas-vindas com termos
            enableEdgeToEdge()
            setContent {
                SuperIDTheme {
                    PermissionAndWelcomeFlow()
                }
            }
        }
    }
}

@Composable
fun PermissionAndWelcomeFlow() {
    WithPermission(permission = Manifest.permission.CAMERA) {
        WelcomeScreen()
    }
}

fun primeiraVez(context: Context): Boolean {
    val sharedPref = context.getSharedPreferences("SuperIDPrefs", Context.MODE_PRIVATE)
    return sharedPref.getBoolean("PrimeiraVez", true)
}

fun setPrimeiraVez(context: Context, primeiraVez: Boolean) {
    val sharedPref = context.getSharedPreferences("SuperIDPrefs", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putBoolean("PrimeiraVez", primeiraVez)
        apply()
    }
}


@Composable
fun WelcomeScreen() {
    val context = LocalContext.current
    var introTermos by remember { mutableStateOf(primeiraVez(context)) }
    var isChecked by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    val termosUsoTexto = """   Termos de Uso – Aplicativo SuperID
Última atualização: Abril de 2025
Bem-vindo ao aplicativo SuperID. Ao utilizar nossos serviços, você concorda com os
termos e condições descritos a seguir. Leia com atenção.
1. Objetivo do Aplicativo
O SuperID é um aplicativo desenvolvido para o usuário salvar suas senhas de forma
segura e rápida e facilitar o processo de autenticação segura de usuários em sites
parceiros por meio de QR Code dinâmico. O app permite que o usuário acesse essas
plataformas com rapidez, utilizando sua conta cadastrada no SuperID.
2. Cadastro e Acesso
• O acesso ao SuperID requer a criação de uma conta com e-mail e senha
válidos.
• Os dados de autenticação são armazenados de forma segura utilizando
tecnologias providas pelo Firebase Authentication.
• O usuário é responsável por manter a confidencialidade de suas credenciais.
3. Funcionamento da Autenticação
• Sites parceiros podem gerar um QR Code temporário via nossa API.
• O app SuperID escaneia esse QR Code e, caso o usuário esteja autenticado,
confirma o login no parceiro de forma criptografada.
• O processo é seguro, sem compartilhamento de senhas com terceiros.
4. Segurança e Criptografia
• As senhas armazenadas localmente no app são criptografadas antes de serem
salvas.
• O processo de escaneamento e confirmação de QR Code utiliza tokens
temporários e autenticação por usuário.
• Os dados de autenticação não são expostos durante nenhuma etapa do
processo.
5. Sites Parceiros
• O login via SuperID só é permitido em sites autorizados e registrados na base
do aplicativo.
• Cada parceiro possui uma chave de API única para validar sua identidade.
•
6. Privacidade dos Dados
• O SuperID não compartilha seus dados pessoais com terceiros sem
consentimento.
• Informações como UID (identificador único) e timestamp de login são
compartilhadas apenas com sites parceiros autorizados no momento da
autenticação.
7. Responsabilidade do Usuário
• O uso indevido do aplicativo, como tentativas de autenticar em sistemas não
autorizados, poderá resultar em bloqueio da conta.
• O usuário deve manter seu dispositivo protegido contra acesso não autorizado.
8. Limitação de Responsabilidade
• O SuperID é um projeto em desenvolvimento e não se responsabiliza por falhas
de conexão, indisponibilidade temporária ou uso indevido por parte dos
parceiros.
9. Alterações nos Termos
Estes termos podem ser atualizados periodicamente. O uso contínuo do app após
alterações implica na aceitação dos novos termos. 
""".trimIndent()
    var showTermos by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundImage()

        if (introTermos) {
            AlertDialog(
                onDismissRequest = { },
                properties = DialogProperties(
                    dismissOnClickOutside = false,
                    dismissOnBackPress = false
                ),
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.superid_removebg),
                            contentDescription = "Logo SuperID",
                            modifier = Modifier
                                .height(100.dp)
                                .padding(bottom = 2.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Bem-Vindo ao SuperID",
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Aqui você tem o armazenamento seguro de senhas para logar nos nossos sites parceiros.",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Image(
                            painter = painterResource(R.drawable.superid_exemplo),
                            contentDescription = "Salve e categorize suas senhas",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(340.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Salve e categorize suas senhas",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "TERMOS DE USO",
                            modifier = Modifier
                                .clickable {
                                    showTermos = true
                                }
                                .padding(8.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))



                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    isChecked = it
                                    if (it) showError = false
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.onSurface,
                                    checkmarkColor = MaterialTheme.colorScheme.surface
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Li e aceito os termos de uso",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (isChecked) {
                                introTermos = false
                                setPrimeiraVez(context, false)
                                val intent = Intent(context, RegisterActivity::class.java)
                                context.startActivity(intent)
                                if (context is ComponentActivity) {
                                    context.finish()
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Aceite os termos de uso antes de prosseguir",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Continuar", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurface
            )
        }

        if (showTermos) {
            AlertDialog(
                onDismissRequest = { showTermos = false },
                title = {
                    Text(
                        text = "Termos de Uso",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = termosUsoTexto,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Justify
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { showTermos = false }) {
                        Text("Fechar")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview
@Composable
fun WelcomeScreenPreview() {
    SuperIDTheme {
        WelcomeScreen()
    }
}
