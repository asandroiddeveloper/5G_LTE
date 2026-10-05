package com.asdroid.jetpack_ui.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.asdroid.jetpack_ui.R
import com.asdroid.jetpack_ui.network.ConnectionSnapshot
import com.asdroid.jetpack_ui.network.ConnectionTransport
import com.asdroid.jetpack_ui.network.NetworkGeneration
import com.asdroid.jetpack_ui.network.NetworkSettingsNavigator
import com.asdroid.jetpack_ui.network.TelephonySnapshot
import com.asdroid.jetpack_ui.network.buildDiagnosticsReport
import com.asdroid.jetpack_ui.network.rememberConnectionSnapshot
import com.asdroid.jetpack_ui.network.rememberTelephonySnapshot
import com.asdroid.jetpack_ui.ui.components.InfoRow
import com.asdroid.jetpack_ui.ui.components.SectionCard
import com.asdroid.jetpack_ui.ui.components.StatusPill
import com.asdroid.jetpack_ui.ui.theme.SignalLime
import com.asdroid.jetpack_ui.util.copyToClipboard
import java.util.Locale

/**
 * Live network state. The connection card needs no permission at all; the network
 * type card asks for READ_PHONE_STATE only when the user opts in.
 */
@Composable
fun StatusScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val connection = rememberConnectionSnapshot()
    val navigator = remember(context) { NetworkSettingsNavigator(context) }

    // Bumped after the permission dialog closes so the telephony state is re-read.
    var refreshKey by remember { mutableStateOf(0) }
    val telephony = rememberTelephonySnapshot(refreshKey)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { refreshKey++ }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ConnectionCard(connection)
        MobileNetworkCard(telephony)

        if (!telephony.permissionGranted) {
            SectionCard(title = stringResource(R.string.permission_title)) {
                Text(
                    text = stringResource(R.string.permission_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.READ_PHONE_STATE) },
                ) {
                    Text(stringResource(R.string.permission_button))
                }
            }
        }

        OutlinedButton(
            onClick = {
                val report = buildDiagnosticsReport(
                    connection = connection,
                    telephony = telephony,
                    resolvedTarget = navigator.probeDescription(),
                    lastOpenResult = "n/a",
                )
                if (copyToClipboard(context, "5G LTE diagnostics", report)) {
                    Toast.makeText(context, R.string.report_copied, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.copy_diagnostics))
        }
    }
}

@Composable
private fun ConnectionCard(connection: ConnectionSnapshot) {
    SectionCard(
        title = stringResource(R.string.card_live_connection),
        trailing = {
            StatusPill(
                text = stringResource(
                    if (connection.connected) R.string.state_online else R.string.state_offline,
                ),
                positive = connection.connected,
            )
        },
    ) {
        InfoRow(stringResource(R.string.label_transport), transportLabel(connection.transport))
        InfoRow(
            label = stringResource(R.string.label_state),
            value = if (connection.validated) {
                stringResource(R.string.state_validated)
            } else {
                stringResource(R.string.value_unknown)
            },
        )
        InfoRow(
            label = stringResource(R.string.label_billing),
            value = stringResource(
                if (connection.metered) R.string.billing_metered else R.string.billing_unmetered,
            ),
        )
        InfoRow(stringResource(R.string.label_download), bandwidth(connection.downstreamKbps))
        InfoRow(stringResource(R.string.label_upload), bandwidth(connection.upstreamKbps))
        InfoRow(
            label = stringResource(R.string.label_interface),
            value = connection.interfaceName ?: stringResource(R.string.value_unknown),
        )
        InfoRow(
            label = stringResource(R.string.label_dns),
            value = connection.dnsServers.take(2).joinToString(", ")
                .ifEmpty { stringResource(R.string.value_unknown) },
        )
    }
}

@Composable
private fun MobileNetworkCard(telephony: TelephonySnapshot) {
    SectionCard(title = stringResource(R.string.card_mobile_network)) {
        InfoRow(
            label = stringResource(R.string.label_carrier),
            value = telephony.carrierName ?: stringResource(R.string.value_unknown),
        )
        InfoRow(
            label = stringResource(R.string.label_generation),
            value = if (telephony.permissionGranted) {
                generationLabel(telephony.generation)
            } else {
                stringResource(R.string.gen_unknown)
            },
            valueColor = if (telephony.permissionGranted) {
                SignalLime
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun transportLabel(transport: ConnectionTransport): String = stringResource(
    when (transport) {
        ConnectionTransport.WIFI -> R.string.transport_wifi
        ConnectionTransport.CELLULAR -> R.string.transport_cellular
        ConnectionTransport.ETHERNET -> R.string.transport_ethernet
        ConnectionTransport.VPN -> R.string.transport_vpn
        ConnectionTransport.OTHER -> R.string.transport_other
        ConnectionTransport.NONE -> R.string.transport_none
    },
)

@Composable
private fun generationLabel(generation: NetworkGeneration): String = stringResource(
    when (generation) {
        NetworkGeneration.NR_STANDALONE -> R.string.gen_5g_sa
        NetworkGeneration.NR_NON_STANDALONE -> R.string.gen_5g_nsa
        NetworkGeneration.NR -> R.string.gen_5g
        NetworkGeneration.LTE -> R.string.gen_4g
        NetworkGeneration.THREE_G -> R.string.gen_3g
        NetworkGeneration.TWO_G -> R.string.gen_2g
        NetworkGeneration.UNKNOWN -> R.string.gen_unknown
    },
)

@Composable
private fun bandwidth(kbps: Int): String = if (kbps <= 0) {
    stringResource(R.string.value_unknown)
} else {
    stringResource(R.string.unit_mbps, String.format(Locale.US, "%.1f", kbps / 1000.0))
}
