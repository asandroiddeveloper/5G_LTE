package com.asdroid.jetpack_ui.network

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri
import com.asdroid.jetpack_ui.R
import java.util.Locale

/** How good the screen we managed to open actually is. */
enum class RadioTargetKind {
    /** The hidden radio menu that contains "Set Preferred Network Type". */
    HIDDEN_MENU,

    /** A normal settings screen; the user may still have to dig further. */
    SETTINGS_FALLBACK,
}

data class OpenResult(
    val kind: RadioTargetKind?,
    val target: String?,
) {
    val opened: Boolean get() = kind != null
}

/**
 * Opens the hidden "Phone info" screen.
 *
 * There is no supported API for this, and hardcoding two AOSP class names (as the
 * first version of this app did) fails on most OEM ROMs. So we walk a ladder of
 * known entry points, trying each one and catching the failure, then fall back to
 * the documented settings screens and the dialer code.
 */
class NetworkSettingsNavigator(private val context: Context) {

    private data class Candidate(val intent: Intent, val kind: RadioTargetKind)

    private fun candidates(): List<Candidate> = listOf(
        // AOSP / Pixel and most near-stock ROMs
        Candidate(component("com.android.phone", "com.android.phone.settings.RadioInfo"), RadioTargetKind.HIDDEN_MENU),
        Candidate(component("com.android.settings", "com.android.settings.RadioInfo"), RadioTargetKind.HIDDEN_MENU),
        Candidate(component("com.android.phone", "com.android.phone.settings.RadioInfoActivity"), RadioTargetKind.HIDDEN_MENU),
        // OEM entry points, probed rather than assumed
        Candidate(
            component(
                "com.samsung.android.app.telephonyui",
                "com.samsung.android.app.telephonyui.hiddennetworksetting.MainActivity",
            ),
            RadioTargetKind.HIDDEN_MENU,
        ),
        Candidate(component("com.android.phone", "com.android.phone.MiuiMobileNetworkSettings"), RadioTargetKind.HIDDEN_MENU),
        Candidate(component("com.mediatek.settings", "com.mediatek.settings.RadioInfo"), RadioTargetKind.HIDDEN_MENU),
        // Documented screens that always exist
        Candidate(Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS), RadioTargetKind.SETTINGS_FALLBACK),
        Candidate(Intent(Settings.ACTION_WIRELESS_SETTINGS), RadioTargetKind.SETTINGS_FALLBACK),
        Candidate(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS), RadioTargetKind.SETTINGS_FALLBACK),
    )

    fun open(): OpenResult {
        val activity = context as? Activity
        for (candidate in candidates()) {
            val started = try {
                val intent = Intent(candidate.intent)
                if (activity == null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                true
            } catch (e: Exception) {
                // Not present on this ROM, not exported, or blocked by policy: try the next rung.
                false
            }
            if (started) return OpenResult(candidate.kind, candidate.intent.describe())
        }
        return OpenResult(kind = null, target = null)
    }

    /** Opens the dialer pre-filled with the radio info code — a permission-free fallback. */
    fun openDialer(): Boolean = try {
        val intent = Intent(
            Intent.ACTION_DIAL,
            ("tel:" + Uri.encode(USSD_CODE)).toUri(),
        )
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        false
    }

    /**
     * Best-effort description of the first candidate this device exposes, used in the
     * diagnostics report. On Android 11+ package visibility filtering can report
     * "unresolved" even when launching would have worked, so treat it as informational.
     */
    fun probeDescription(): String {
        val manager = context.packageManager
        val first = candidates().firstOrNull { candidate ->
            runCatching { candidate.intent.resolveActivity(manager) != null }.getOrDefault(false)
        }
        return first?.intent?.describe() ?: "unresolved"
    }

    /** A rough settings path for this manufacturer, so the copy can be specific. */
    fun devicePathRes(): Int = when (Build.MANUFACTURER.lowercase(Locale.ROOT)) {
        "samsung" -> R.string.oem_path_samsung
        "xiaomi", "redmi", "poco" -> R.string.oem_path_xiaomi
        else -> R.string.oem_path_generic
    }

    private fun component(packageName: String, className: String): Intent =
        Intent().setClassName(packageName, className)

    private fun Intent.describe(): String =
        component?.let { "${it.packageName}/${it.className}" } ?: (action ?: toString())

    companion object {
        /** The radio information menu code. */
        const val USSD_CODE = "*#*#4636#*#*"
    }
}
