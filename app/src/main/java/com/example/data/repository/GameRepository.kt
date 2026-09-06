package com.example.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.example.data.db.GameDao
import com.example.data.db.GameEntity
import com.example.data.model.GameProfileConfig
import com.example.data.model.GameProfileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val isGame: Boolean
)

class GameRepository(
    private val context: Context,
    private val gameDao: GameDao
) {
    val allGames: Flow<List<GameEntity>> = gameDao.getAllGames()

    suspend fun getGame(packageName: String): GameEntity? = withContext(Dispatchers.IO) {
        gameDao.getGameByPackage(packageName)
    }

    suspend fun saveGameProfile(packageName: String, appName: String, config: GameProfileConfig) = withContext(Dispatchers.IO) {
        val entity = GameEntity(
            packageName = packageName,
            appName = appName,
            isCustomApp = true,
            profileType = config.profileType.name,
            downscaleFactor = config.downscaleFactor,
            targetFps = config.targetFps,
            aotCompileSpeed = config.aotCompileSpeed,
            enableAngleVulkan = config.enableAngleVulkan,
            whitelistDoze = config.whitelistDoze,
            grantWakelock = config.grantWakelock,
            bypassJoyose = config.bypassJoyose,
            enableGameTurbo = config.enableGameTurbo,
            touchResponseLevel = config.touchResponseLevel
        )
        gameDao.insertGame(entity)
    }

    suspend fun removeGame(packageName: String) = withContext(Dispatchers.IO) {
        gameDao.deleteGame(packageName)
    }

    suspend fun updateLastLaunched(packageName: String) = withContext(Dispatchers.IO) {
        gameDao.updateLastLaunched(packageName, System.currentTimeMillis())
    }

    suspend fun scanAndSyncInstalledGames(): Int = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val installedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(0)
        }

        val detectedGames = mutableListOf<GameEntity>()
        val selfPkg = context.packageName

        for (app in installedApps) {
            if (app.packageName == selfPkg) continue

            val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            if (isSystem && !isKnownGameOrEmulator(app.packageName)) {
                continue
            }

            var isGame = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (app.category == ApplicationInfo.CATEGORY_GAME) {
                    isGame = true
                }
            }

            @Suppress("DEPRECATION")
            if ((app.flags and ApplicationInfo.FLAG_IS_GAME) != 0) {
                isGame = true
            }

            if (!isGame && isKnownGameOrEmulator(app.packageName)) {
                isGame = true
            }

            if (isGame) {
                val label = pm.getApplicationLabel(app).toString()
                detectedGames.add(
                    GameEntity(
                        packageName = app.packageName,
                        appName = label,
                        isCustomApp = false,
                        profileType = GameProfileType.EXTREME_PERFORMANCE.name,
                        downscaleFactor = 0.8f,
                        targetFps = 120,
                        aotCompileSpeed = true,
                        enableAngleVulkan = true,
                        whitelistDoze = true,
                        grantWakelock = true,
                        bypassJoyose = true,
                        enableGameTurbo = true,
                        touchResponseLevel = 3
                    )
                )
            }
        }

        if (detectedGames.isNotEmpty()) {
            gameDao.insertAll(detectedGames)
        }
        detectedGames.size
    }

    suspend fun getAllInstalledApps(): List<InstalledAppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val installedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(0)
        }

        val selfPkg = context.packageName
        installedApps
            .filter { it.packageName != selfPkg }
            .map { app ->
                val label = pm.getApplicationLabel(app).toString()
                var isGame = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && app.category == ApplicationInfo.CATEGORY_GAME) {
                    isGame = true
                }
                @Suppress("DEPRECATION")
                if ((app.flags and ApplicationInfo.FLAG_IS_GAME) != 0) {
                    isGame = true
                }
                if (isKnownGameOrEmulator(app.packageName)) {
                    isGame = true
                }
                InstalledAppInfo(
                    packageName = app.packageName,
                    appName = label,
                    isGame = isGame
                )
            }
            .sortedWith(compareByDescending<InstalledAppInfo> { it.isGame }.thenBy { it.appName.lowercase() })
    }

    private fun isKnownGameOrEmulator(packageName: String): Boolean {
        val lower = packageName.lowercase()
        val keywords = listOf(
            "emulator", "retroarch", "ppsspp", "dolphin", "yuzu", "citra", "aethersx2",
            "skyline", "strato", "net.kdt.pojavlaunch", "genshin", "pubg", "codm",
            "wildrift", "roblox", "minecraft", "unity", "unreal", "supercell", "riotgames",
            "mihoyo", "hoyoverse", "epicgames", "ea.gp", "gameloft", "nintendo"
        )
        return keywords.any { lower.contains(it) }
    }
}
