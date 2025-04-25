package com.projetointegrador3.superid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.projetointegrador3.superid.ui.theme.SuperIDTheme

class AddCategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                    AddNewCategoryScreen()

                }
            }
        }
    }

@Composable
fun AddNewCategory() {
    Text(
        text = "Tela pra adicionar nova categoria",
    )
}



@Preview(showBackground = true)
@Composable
fun AddNewCategoryScreen() {
    SuperIDTheme {
       AddNewCategory()
    }
}