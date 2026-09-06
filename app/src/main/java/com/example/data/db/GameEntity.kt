package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.GameProfileConfig
import com.example.data.model.GameProfileType

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isCustomApp: Boolean = false,
    val profileType: String = GameProfileType.EXTREME_PERFORMANCE.name,
    val downscaleFactor: Float = 0.8f,
    val targetFps: Int = 120,
    val aotCompileSpeed: Boolean = true,
    val enableAngleVulkan: Boolean = true,
    val whitelistDoze: Boolean = true,
    val grantWakelock: Boolean = true,
    val bypassJoyose: Boolean = true,
    val enableGameTurbo: Boolean = true,
    val touchResponseLevel: Int = 3,
    val lastLaunched: Long = 0L
) {
    fun toProfileConfig(): GameProfileConfig {
        val parsedType = try {
            GameProfileType.valueOf(profileType)
        } catch (_: Exception) {
            GameProfileType.EXTREME_PERFORMANCE
        }
        return GameProfileConfig(
            profileType = parsedType,
            downscaleFactor = downscaleFactor,
            targetFps = targetFps,
            aotCompileSpeed = aotCompileSpeed,
            enableAngleVulkan = enableAngleVulkan,
            whitelistDoze = whitelistDoze,
            grantWakelock = grantWakelock,
            bypassJoyose = bypassJoyose,
            enableGameTurbo = enableGameTurbo,
            touchResponseLevel = touchResponseLevel
        )
    }
}
