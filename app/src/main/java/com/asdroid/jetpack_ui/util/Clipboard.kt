package com.asdroid.jetpack_ui.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

/** Copies [text] to the primary clipboard. Returns false if the device exposes no clipboard. */
fun copyToClipboard(context: Context, label: String, text: String): Boolean {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        ?: return false
    return try {
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        true
    } catch (e: Exception) {
        false
    }
}
