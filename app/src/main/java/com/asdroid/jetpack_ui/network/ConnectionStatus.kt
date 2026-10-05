package com.asdroid.jetpack_ui.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Live view of the active network. Everything here comes from ConnectivityManager
 * and needs only the normal ACCESS_NETWORK_STATE permission, so it works with no
 * runtime prompt.
 */
enum class ConnectionTransport { WIFI, CELLULAR, ETHERNET, VPN, OTHER, NONE }

data class ConnectionSnapshot(
    val connected: Boolean = false,
    val transport: ConnectionTransport = ConnectionTransport.NONE,
    val validated: Boolean = false,
    val metered: Boolean = false,
    val downstreamKbps: Int = 0,
    val upstreamKbps: Int = 0,
    val interfaceName: String? = null,
    val dnsServers: List<String> = emptyList(),
)

fun readConnectionSnapshot(context: Context): ConnectionSnapshot {
    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return ConnectionSnapshot()
    return try {
        val network = manager.activeNetwork ?: return ConnectionSnapshot()
        val capabilities = manager.getNetworkCapabilities(network) ?: return ConnectionSnapshot()
        val link = manager.getLinkProperties(network)
        ConnectionSnapshot(
            connected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
            transport = capabilities.toTransport(),
            validated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
            metered = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED),
            downstreamKbps = capabilities.linkDownstreamBandwidthKbps,
            upstreamKbps = capabilities.linkUpstreamBandwidthKbps,
            interfaceName = link?.interfaceName,
            dnsServers = link?.dnsServers?.mapNotNull { it.hostAddress }.orEmpty(),
        )
    } catch (e: Exception) {
        ConnectionSnapshot()
    }
}

private fun NetworkCapabilities.toTransport(): ConnectionTransport = when {
    hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> ConnectionTransport.VPN
    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionTransport.WIFI
    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionTransport.CELLULAR
    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionTransport.ETHERNET
    else -> ConnectionTransport.OTHER
}

/**
 * Observes the default network until the composable leaves the composition.
 * Callbacks arrive on a binder thread, so updates are posted to the main looper.
 */
@Composable
fun rememberConnectionSnapshot(): ConnectionSnapshot {
    val context = LocalContext.current
    var snapshot by remember(context) { mutableStateOf(readConnectionSnapshot(context)) }

    DisposableEffect(context) {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (manager == null) {
            onDispose { }
        } else {
            val main = Handler(Looper.getMainLooper())
            val callback = object : ConnectivityManager.NetworkCallback() {
                private fun refresh() {
                    main.post { snapshot = readConnectionSnapshot(context) }
                }

                override fun onAvailable(network: Network) = refresh()
                override fun onLost(network: Network) = refresh()
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities,
                ) = refresh()

                override fun onLinkPropertiesChanged(
                    network: Network,
                    linkProperties: LinkProperties,
                ) = refresh()
            }
            manager.registerDefaultNetworkCallback(callback)
            onDispose { runCatching { manager.unregisterNetworkCallback(callback) } }
        }
    }

    return snapshot
}
