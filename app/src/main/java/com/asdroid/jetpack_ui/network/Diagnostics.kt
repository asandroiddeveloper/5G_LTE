package com.asdroid.jetpack_ui.network

import android.os.Build

/**
 * Plain-text report the user can paste into a bug report. Includes the values that
 * decide whether the app's one feature (opening the hidden radio menu) can work on
 * their device.
 */
fun buildDiagnosticsReport(
    connection: ConnectionSnapshot,
    telephony: TelephonySnapshot,
    resolvedTarget: String?,
    lastOpenResult: String,
): String = buildString {
    appendLine("5G LTE diagnostics")
    appendLine("--- device ---")
    appendLine("manufacturer: ${Build.MANUFACTURER}")
    appendLine("model: ${Build.MODEL}")
    appendLine("android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
    appendLine("--- connection ---")
    appendLine("connected: ${connection.connected}")
    appendLine("transport: ${connection.transport}")
    appendLine("validated: ${connection.validated}")
    appendLine("metered: ${connection.metered}")
    appendLine("downstream_kbps: ${connection.downstreamKbps}")
    appendLine("upstream_kbps: ${connection.upstreamKbps}")
    appendLine("interface: ${connection.interfaceName ?: "-"}")
    appendLine("dns: ${connection.dnsServers.joinToString().ifEmpty { "-" }}")
    appendLine("--- telephony ---")
    appendLine("permission_granted: ${telephony.permissionGranted}")
    appendLine("carrier: ${telephony.carrierName ?: "-"}")
    appendLine("generation: ${telephony.generation}")
    appendLine("network_type_code: ${telephony.networkTypeCode}")
    appendLine("roaming: ${telephony.roaming ?: "-"}")
    appendLine("--- radio menu ---")
    appendLine("resolved: ${resolvedTarget ?: "none"}")
    appendLine("last_open_attempt: $lastOpenResult")
}
