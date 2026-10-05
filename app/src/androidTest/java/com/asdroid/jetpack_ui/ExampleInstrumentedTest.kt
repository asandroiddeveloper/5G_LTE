package com.asdroid.jetpack_ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appTitleIsVisibleOnLaunch() {
        val title = composeRule.activity.getString(R.string.app_name)
        composeRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun targetContextBelongsToThisApp() {
        // startsWith because the debug build type carries an .debug applicationId suffix.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertTrue(appContext.packageName.startsWith("com.asdroid.jetpack_ui"))
    }
}
