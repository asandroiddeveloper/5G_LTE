package com.asdroid.jetpack_ui.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.asdroid.jetpack_ui.R
import com.asdroid.jetpack_ui.network.NetworkSettingsNavigator
import com.asdroid.jetpack_ui.network.RadioTargetKind
import com.asdroid.jetpack_ui.ui.components.GradientBackground
import com.asdroid.jetpack_ui.ui.components.NavRailColumn
import com.asdroid.jetpack_ui.ui.components.TabStrip
import com.asdroid.jetpack_ui.ui.theme.TextSecondary

/** Destinations of the app. */
enum class HomeTab(val labelRes: Int) {
    STATUS(R.string.tab_status),
    SETUP(R.string.tab_setup),
    GUIDE(R.string.tab_guide),
}

/**
 * Root screen: gradient background, tabs on phones and a navigation rail on tablets.
 *
 * @param openRadioInfoRequest incremented by MainActivity when the Quick Settings
 *   tile (or any external shortcut) asks for the hidden radio menu.
 */
@Composable
fun HomeScreen(
    openRadioInfoRequest: Int = 0,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val navigator = remember(context) { NetworkSettingsNavigator(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    var tab by rememberSaveable { mutableStateOf(HomeTab.STATUS) }

    LaunchedEffect(openRadioInfoRequest) {
        if (openRadioInfoRequest > 0) {
            tab = HomeTab.SETUP
            val result = navigator.open()
            val needsHelp = !result.opened || result.kind == RadioTargetKind.SETTINGS_FALLBACK
            if (needsHelp) {
                snackbarHostState.showSnackbar(context.getString(R.string.setup_open_failed_title))
            }
        }
    }

    // Three tabs deep: system back should return to the first tab, not leave the app.
    BackHandler(enabled = tab != HomeTab.STATUS) { tab = HomeTab.STATUS }

    val labels = HomeTab.entries.map { stringResource(it.labelRes) }

    GradientBackground(modifier = modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                val isTablet = maxWidth >= 720.dp
                if (isTablet) {
                    Column(Modifier.fillMaxSize()) {
                        Header()
                        Row(Modifier.fillMaxSize()) {
                            NavRailColumn(
                                labels = labels,
                                selectedIndex = tab.ordinal,
                                onSelect = { index -> tab = HomeTab.entries[index] },
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                            ) {
                                Crossfade(targetState = tab) { current -> ScreenContent(current) }
                            }
                        }
                    }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        Header()
                        TabStrip(
                            labels = labels,
                            selectedIndex = tab.ordinal,
                            onSelect = { index -> tab = HomeTab.entries[index] },
                        )
                        Spacer(Modifier.height(12.dp))
                        Box(Modifier.weight(1f)) {
                            Crossfade(targetState = tab) { current -> ScreenContent(current) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenContent(tab: HomeTab) {
    when (tab) {
        HomeTab.STATUS -> StatusScreen()
        HomeTab.SETUP -> SetupScreen()
        HomeTab.GUIDE -> GuideScreen()
    }
}

@Composable
private fun Header() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
        )
        Text(
            text = stringResource(R.string.app_tagline),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
    }
}
