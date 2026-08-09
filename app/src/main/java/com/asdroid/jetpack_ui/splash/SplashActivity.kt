package com.asdroid.jetpack_ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.asdroid.jetpack_ui.MainActivity
import com.asdroid.jetpack_ui.R
import com.asdroid.jetpack_ui.splash.ui.theme.Black
import com.asdroid.jetpack_ui.splash.ui.theme.ElectricBlue
import com.asdroid.jetpack_ui.splash.ui.theme.ExtraGreen
import com.asdroid.jetpack_ui.splash.ui.theme.JetPackUITheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetPackUITheme {
                lifecycleScope.launch {
                    delay(
                        2000L.milliseconds
                    )
                    startActivity(Intent(
                        this@SplashActivity, MainActivity::class.java
                    ))
                    finish()
                }
               SplashUi()
            }
        }
    }
}

@Composable
fun SplashUi() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        ElectricBlue,
                        ExtraGreen,
                        Black
                    )
                )
            ),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(
            Modifier.height(290.dp)
        )
        Image(
            painter = painterResource(R.drawable.logo),
            "Logo",
            modifier = Modifier.size(200.dp)
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )
        Text(
            "5G LTE",
            color = Color.LightGray,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Black,
            fontSize = 30.sp
        )
        Spacer(
            modifier = Modifier.height(200.dp)
        )
        Text(
            "Designed and developed by AS",
            fontSize = 18.sp,
            fontFamily = FontFamily.SansSerif,
            color = Color.LightGray,
        )
    }
}