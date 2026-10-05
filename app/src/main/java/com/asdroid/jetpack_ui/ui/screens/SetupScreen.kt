package com.asdroid.jetpack_ui.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.asdroid.jetpack_ui.R
import com.asdroid.jetpack_ui.network.NetworkSettingsNavigator
import com.asdroid.jetpack_ui.network.RadioTargetKind
import com.asdroid.jetpack_ui.ui.components.ButtonRow
import com.asdroid.jetpack_ui.ui.components.SectionCard
import com.asdroid.jetpack_ui.ui.components.StepRow
import com.asdroid.jetpack_ui.ui.theme.SignalLime
import com.asdroid.jetpack_ui.ui.theme.TextMuted
import com.asdroid.jetpack_ui.ui.theme.WarningAmber
import com.asdroid.jetpack_ui.util.copyToClipboard

/**
 * Guided setup plus the safety warning. The button opens the hidden radio menu when
 * the device exposes it; otherwise the user gets an honest manual path.
 */
@Composable
fun SetupScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val navigator = remember(context) { NetworkSettingsNavigator(context) }
    var showManualHelp by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionCard(title = stringResource(R.string.setup_heading)) {
            Text(
                text = stringResource(R.string.setup_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            StepRow(1, stringResource(R.string.setup_step_1))
            StepRow(2, stringResource(R.string.setup_step_2))
            StepRow(3, stringResource(R.string.setup_step_3))
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = {
                    val result = navigator.open()
                    // Only the real hidden menu does the job; anything else needs the
                    // manual instructions as well.
                    if (!result.opened || result.kind == RadioTargetKind.SETTINGS_FALLBACK) {
                        showManualHelp = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.setup_open_button),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(
                    R.string.setup_device_hint,
                    Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
                    stringResource(navigator.devicePathRes()),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
        }

        WarningCard()
    }

    if (showManualHelp) {
        ManualHelpDialog(
            onDismiss = { showManualHelp = false },
            onCopy = {
                val copied = copyToClipboard(
                    context = context,
                    label = "Radio info code",
                    text = NetworkSettingsNavigator.USSD_CODE,
                )
                if (copied) {
                    Toast.makeText(context, R.string.setup_copied, Toast.LENGTH_SHORT).show()
                }
            },
            onDial = { navigator.openDialer() },
        )
    }
}

@Composable
private fun WarningCard() {
    SectionCard(
        title = stringResource(R.string.warning_title),
        titleColor = WarningAmber,
    ) {
        Text(
            text = stringResource(R.string.warning_body_1),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.warning_body_2),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.warning_body_3),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.warning_footer),
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
        )
    }
}

@Composable
private fun ManualHelpDialog(
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onDial: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp),
        ) {
            Text(
                text = stringResource(R.string.setup_open_failed_title),
                style = MaterialTheme.typography.titleMedium,
                color = SignalLime,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(
                    R.string.setup_open_failed_body,
                    NetworkSettingsNavigator.USSD_CODE,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            ButtonRow {
                OutlinedButton(onClick = onCopy) {
                    Text(stringResource(R.string.setup_copy_code))
                }
                Button(onClick = onDial) {
                    Text(stringResource(R.string.setup_dial_fallback))
                }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.setup_close))
            }
        }
    }
}
