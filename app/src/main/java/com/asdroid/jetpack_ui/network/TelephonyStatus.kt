package com.asdroid.jetpack_ui.network

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyCallback
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.awaitCancellation

/** How the phone currently reports its radio link. */
enum class NetworkGeneration {
    NR_STANDALONE,
    NR_NON_STANDALONE,
    NR,
    LTE,
    THREE_G,
    TWO_G,
    UNKNOWN,
}

/**
 * Pure mapping from `TelephonyManager.NETWORK_TYPE_*` codes to a generation.
 * Kept free of Android calls so it can be unit tested on the JVM.
 */
object NetworkGenerationMapper {

    fun fromNetworkType(networkType: Int): NetworkGeneration = when (networkType) {
        TelephonyManager.NETWORK_TYPE_NR -> NetworkGeneration.NR_STANDALONE
        TelephonyManager.NETWORK_TYPE_LTE -> NetworkGeneration.LTE
        TelephonyManager.NETWORK_TYPE_HSPAP,
        TelephonyManager.NETWORK_TYPE_HSPA,
        TelephonyManager.NETWORK_TYPE_HSDPA,
        TelephonyManager.NETWORK_TYPE_HSUPA,
        TelephonyManager.NETWORK_TYPE_UMTS,
        TelephonyManager.NETWORK_TYPE_TD_SCDMA,
        TelephonyManager.NETWORK_TYPE_EVDO_0,
        TelephonyManager.NETWORK_TYPE_EVDO_A,
        TelephonyManager.NETWORK_TYPE_EVDO_B,
        TelephonyManager.NETWORK_TYPE_EHRPD,
        -> NetworkGeneration.THREE_G

        TelephonyManager.NETWORK_TYPE_EDGE,
        TelephonyManager.NETWORK_TYPE_GPRS,
        TelephonyManager.NETWORK_TYPE_GSM,
        TelephonyManager.NETWORK_TYPE_CDMA,
        TelephonyManager.NETWORK_TYPE_1xRTT,
        TelephonyManager.NETWORK_TYPE_IDEN,
        -> NetworkGeneration.TWO_G

        else -> NetworkGeneration.UNKNOWN
    }

    /**
     * Prefers the carrier display override (that is where "5G" on an LTE anchor
     * shows up) and falls back to the raw network type.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @Suppress("DEPRECATION")
    fun fromDisplayInfo(networkType: Int, overrideType: Int): NetworkGeneration = when (overrideType) {
        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA,
        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE,
        -> NetworkGeneration.NR_NON_STANDALONE

        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED -> NetworkGeneration.NR

        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_CA,
        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_ADVANCED_PRO,
        -> NetworkGeneration.LTE

        else -> fromNetworkType(networkType)
    }
}

data class TelephonySnapshot(
    val permissionGranted: Boolean = false,
    val carrierName: String? = null,
    val generation: NetworkGeneration = NetworkGeneration.UNKNOWN,
    val networkTypeCode: Int = TelephonyManager.NETWORK_TYPE_UNKNOWN,
    val roaming: Boolean? = null,
)

/**
 * Reads the carrier and network type. Android hides the network type behind
 * READ_PHONE_STATE, so this reports `permissionGranted = false` instead of
 * failing when the user has not opted in.
 */
@SuppressLint("MissingPermission") // Guarded by the checkSelfPermission call below.
fun readTelephonySnapshot(context: Context): TelephonySnapshot {
    val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        ?: return TelephonySnapshot()

    val carrier = runCatching { manager.networkOperatorName }
        .getOrNull()
        ?.takeIf { it.isNotBlank() }

    val granted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_PHONE_STATE,
    ) == PackageManager.PERMISSION_GRANTED

    if (!granted) {
        return TelephonySnapshot(permissionGranted = false, carrierName = carrier)
    }

    val networkType = runCatching { manager.dataNetworkType }
        .getOrDefault(TelephonyManager.NETWORK_TYPE_UNKNOWN)

    return TelephonySnapshot(
        permissionGranted = true,
        carrierName = carrier,
        generation = NetworkGenerationMapper.fromNetworkType(networkType),
        networkTypeCode = networkType,
        roaming = runCatching { manager.isNetworkRoaming }.getOrNull(),
    )
}

@RequiresApi(Build.VERSION_CODES.S)
private class DisplayInfoRelay(
    private val onChange: (TelephonyDisplayInfo) -> Unit,
) : TelephonyCallback(), TelephonyCallback.DisplayInfoListener {

    override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
        onChange(telephonyDisplayInfo)
    }
}

/** Registers the 5G display callback and suspends until the effect is cancelled. */
@RequiresApi(Build.VERSION_CODES.S)
@SuppressLint("MissingPermission") // Only called once READ_PHONE_STATE is granted.
private suspend fun observeDisplayInfo(
    context: Context,
    onChange: (TelephonyDisplayInfo) -> Unit,
) {
    val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager ?: return
    val relay = DisplayInfoRelay(onChange)
    val registered = runCatching {
        manager.registerTelephonyCallback(context.mainExecutor, relay)
    }.isSuccess
    if (!registered) return
    try {
        awaitCancellation()
    } finally {
        runCatching { manager.unregisterTelephonyCallback(relay) }
    }
}

/**
 * Telephony state plus live updates. Pass a new [refreshKey] after the user grants
 * or revokes the permission to force a re-read.
 */
@Composable
fun rememberTelephonySnapshot(refreshKey: Int = 0): TelephonySnapshot {
    val context = LocalContext.current
    var snapshot by remember(refreshKey) { mutableStateOf(readTelephonySnapshot(context)) }

    LaunchedEffect(refreshKey) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && snapshot.permissionGranted) {
            observeDisplayInfo(context) { displayInfo ->
                snapshot = snapshot.copy(
                    generation = NetworkGenerationMapper.fromDisplayInfo(
                        networkType = displayInfo.networkType,
                        overrideType = displayInfo.overrideNetworkType,
                    ),
                )
            }
        }
    }

    return snapshot
}
