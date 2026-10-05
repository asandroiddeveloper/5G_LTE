package com.asdroid.jetpack_ui.qstile

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.asdroid.jetpack_ui.MainActivity
import com.asdroid.jetpack_ui.R

/**
 * Quick Settings tile: one tap from the shade straight to the hidden radio menu.
 *
 * Rather than starting another app's activity directly (which varies per ROM and is
 * awkward from a service), it opens MainActivity with an action; MainActivity then
 * runs the same navigation ladder the Setup tab uses.
 */
class RadioInfoTile : TileService() {

    // getQsTile() is deprecated on API 33+ in favour of getTile(), but it exists on
    // every API level this app supports, which getTile() does not.
    @Suppress("DEPRECATION")
    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile ?: return
        tile.label = getString(R.string.qs_tile_label)
        tile.icon = Icon.createWithResource(this, R.drawable.ic_qs_radio)
        tile.state = Tile.STATE_INACTIVE
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java)
            .setAction(MainActivity.ACTION_OPEN_RADIO_INFO)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startActivityAndCollapse(
                    PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    ),
                )
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
        } catch (e: Exception) {
            // Fall back to simply opening the app.
            runCatching { startActivity(intent) }
        }
    }
}
