package com.asdroid.jetpack_ui.appui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asdroid.jetpack_ui.R
import com.asdroid.jetpack_ui.splash.ui.theme.Black
import com.asdroid.jetpack_ui.splash.ui.theme.ElectricBlue
import com.asdroid.jetpack_ui.splash.ui.theme.ExtraGreen

@Composable
fun LTE_UI(innerPadding: PaddingValues) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize().verticalScroll(rememberScrollState())
            .padding(innerPadding)
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
            Modifier.height(30.dp)
        )
        Text(
            "5G  LTE",
            color = Color.LightGray,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            fontSize = 26.sp
        )

        Text(
            "Welcome to  5G  LTE  app Designed and  developed by AS",
            color = Color.LightGray,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif,
            fontSize = 12.sp,
        )
        Text(
            "Instructions and Warnings",
            color = Color.LightGray,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif,
            fontSize = 18.sp
        )

        Text(
            "⚠\uFE0F Network Mode Warning\n" +
                    "" +
                    "Selecting \"LTE Only\" or \"NR Only\" may prevent you from making \n" +
                    "or receiving calls if your carrier does not support VoLTE/VoNR \n" +
                    "in your area." +
                    "" +
                    "For reliable calling, keep the mode set to:\n" +
                    "\"LTE/WCDMA/GSM (Auto) / (PRL) \" — this allows automatic fallback to \n" +
                    "3G/2G networks for calls if LTE/5G voice isn't available.\n" +
                    "" +
                    "Only choose LTE/NR Only if:\n" +
                    "- Your carrier fully supports VoLTE or VoNR, AND\n" +
                    "- You're in an area with strong LTE/5G coverage\n" +
                    "" +
                    "Changing this setting may cause missed calls, dropped \n" +
                    "connections, or \"no service\" errors.",
            color = Color.LightGray,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif,
            fontSize = 10.sp,
        )

        RenderSuggestions()
        // Best



        Button(
            onClick = { tryLTE(context) },
            modifier = Modifier
                .width(200.dp)
                .height(70.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricBlue,
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                focusedElevation = 12.dp,
                pressedElevation = 14.dp,
                hoveredElevation = 10.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                "Setting",
                tint = Color.LightGray,
                modifier = Modifier.size(35.dp)
            )
            Spacer(
                modifier = Modifier.width(6.dp)
            )
            Text(
                "Set 5G LTE ",
                fontSize = 20.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                color = Color.LightGray
            )
        }
        Spacer(
            Modifier.padding(bottom = 16.dp)
        )
    }
}

fun tryLTE(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_MAIN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            intent.setClassName("com.android.phone", "com.android.phone.settings.RadioInfo")
        } else {
            intent.setClassName("com.android.settings", "com.android.settings.RadioInfo")
        }
        context.startActivity(intent)
    } catch (
        e: Exception
    ) {
        Toast.makeText(
            context,
            "${e}Your Device doesn't support this Feature.Thanks",
            Toast.LENGTH_LONG
        ).show()
    }
}

@Preview(showSystemUi = true)
@Composable
fun RenderSuggestions() {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
        state = rememberLazyListState(),
        contentPadding = PaddingValues(18.dp)
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.Top,

                ) {
                Text(
                    "Best for All scenarios",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFcde018)
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Image(
                    painter = painterResource(R.drawable.best_for_all),
                    "Best_for_all"
                )
            }


            Spacer(
                Modifier.width(20.dp)
            )

            Column(
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    "Best for 4G INTERNET",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFcde018)
                )
                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Image(
                    painter = painterResource(R.drawable.best_for_4g),
                    "Best_for_4g"
                )

            }


            Spacer(
                Modifier.width(20.dp)
            )


            Column(
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    "Best for 5G INTERNET",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFcde018)
                )
                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Image(
                    painter = painterResource(R.drawable.best_for_5g),
                    "Best_for_5g"
                )
            }
        }
    }
}