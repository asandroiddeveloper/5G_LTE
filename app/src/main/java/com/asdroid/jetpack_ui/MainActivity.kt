package com.asdroid.jetpack_ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.asdroid.jetpack_ui.ui.screens.HomeScreen
import com.asdroid.jetpack_ui.ui.theme.JetPackUITheme

class MainActivity : ComponentActivity() {

    /**
     * Incremented when something outside the UI (the Quick Settings tile) asks for
     * the hidden radio menu. HomeScreen reacts to the change.
     */
    private var openRadioInfoRequest by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Hands the launch splash to androidx.core:core-splashscreen, so there is one
        // seamless splash on every API level instead of a second custom activity.
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // The UI is always the dark brand gradient, so pin light system-bar icons
        // instead of letting the system theme decide.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )

        handleIntent(intent)

        setContent {
            JetPackUITheme {
                HomeScreen(openRadioInfoRequest = openRadioInfoRequest)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == ACTION_OPEN_RADIO_INFO) {
            openRadioInfoRequest++
        }
    }

    companion object {
        const val ACTION_OPEN_RADIO_INFO = "com.asdroid.jetpack_ui.action.OPEN_RADIO_INFO"
    }
}
