package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.GameEntity
import com.example.data.model.GameProfileType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ExtremeBooster", appName)
    }

    @Test
    fun `test game entity profile conversion`() {
        val game = GameEntity(
            packageName = "com.dts.freefireth",
            appName = "Free Fire",
            profileType = GameProfileType.EXTREME_PERFORMANCE.name,
            downscaleFactor = 0.8f,
            targetFps = 120,
            aotCompileSpeed = true
        )
        val config = game.toProfileConfig()
        assertEquals(GameProfileType.EXTREME_PERFORMANCE, config.profileType)
        assertEquals(0.8f, config.downscaleFactor)
        assertEquals(120, config.targetFps)
        assertTrue(config.aotCompileSpeed)
    }
}
