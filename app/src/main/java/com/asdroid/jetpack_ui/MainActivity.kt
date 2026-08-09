package com.asdroid.jetpack_ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import com.asdroid.jetpack_ui.appui.LTE_UI
import com.asdroid.jetpack_ui.ui.theme.JetPackUITheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JetPackUITheme {
                Scaffold(

                ) {innerPadding ->
                    LTE_UI(innerPadding)
                }

            }
        }
    }
}
