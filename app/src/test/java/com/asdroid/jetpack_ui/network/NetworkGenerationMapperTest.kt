package com.asdroid.jetpack_ui.network

import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pure mapping logic behind the "Network type" row. These constants are inlined at
 * compile time, so the test runs on the JVM without a device.
 */
class NetworkGenerationMapperTest {

    @Test
    fun `nr network type maps to standalone 5g`() {
        assertEquals(
            NetworkGeneration.NR_STANDALONE,
            NetworkGenerationMapper.fromNetworkType(TelephonyManager.NETWORK_TYPE_NR),
        )
    }

    @Test
    fun `lte network type maps to 4g`() {
        assertEquals(
            NetworkGeneration.LTE,
            NetworkGenerationMapper.fromNetworkType(TelephonyManager.NETWORK_TYPE_LTE),
        )
    }

    @Test
    fun `umts network type maps to 3g`() {
        assertEquals(
            NetworkGeneration.THREE_G,
            NetworkGenerationMapper.fromNetworkType(TelephonyManager.NETWORK_TYPE_UMTS),
        )
    }

    @Test
    fun `edge network type maps to 2g`() {
        assertEquals(
            NetworkGeneration.TWO_G,
            NetworkGenerationMapper.fromNetworkType(TelephonyManager.NETWORK_TYPE_EDGE),
        )
    }

    @Test
    fun `unknown network types stay unknown`() {
        assertEquals(
            NetworkGeneration.UNKNOWN,
            NetworkGenerationMapper.fromNetworkType(TelephonyManager.NETWORK_TYPE_UNKNOWN),
        )
        assertEquals(
            NetworkGeneration.UNKNOWN,
            NetworkGenerationMapper.fromNetworkType(-1),
        )
    }

    @Test
    fun `display override wins over the raw network type`() {
        @Suppress("DEPRECATION")
        val override = TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA
        assertEquals(
            NetworkGeneration.NR_NON_STANDALONE,
            NetworkGenerationMapper.fromDisplayInfo(TelephonyManager.NETWORK_TYPE_LTE, override),
        )
    }

    @Test
    fun `no override falls back to the network type`() {
        assertEquals(
            NetworkGeneration.LTE,
            NetworkGenerationMapper.fromDisplayInfo(
                TelephonyManager.NETWORK_TYPE_LTE,
                TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NONE,
            ),
        )
    }
}
