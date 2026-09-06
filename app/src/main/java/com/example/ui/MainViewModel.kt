package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.GameEntity
import com.example.data.model.CommandLogEntry
import com.example.data.model.GameProfileConfig
import com.example.data.model.GameProfileType
import com.example.data.model.HardwareTelemetry
import com.example.data.model.TweakCategory
import com.example.data.model.TweakItem
import com.example.data.repository.GameRepository
import com.example.data.repository.InstalledAppInfo
import com.example.engine.OptimizationEngine
import com.example.engine.SystemMonitor
import com.example.shizuku.ShizukuManager
import com.example.shizuku.ShizukuStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val shizukuManager = ShizukuManager(application)
    val systemMonitor = SystemMonitor(application)
    val optimizationEngine = OptimizationEngine(application, shizukuManager, systemMonitor)
    val gameRepository = GameRepository(application, db.gameDao())

    val shizukuStatus: StateFlow<ShizukuStatus> = shizukuManager.status
    val commandLogs: StateFlow<List<CommandLogEntry>> = shizukuManager.commandLogs
    val activeTweaks: StateFlow<Map<String, Boolean>> = optimizationEngine.activeTweaks
    val isOptimizing: StateFlow<Boolean> = optimizationEngine.isOptimizing

    val allGames: StateFlow<List<GameEntity>> = gameRepository.allGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hardwareTelemetry: StateFlow<HardwareTelemetry> = systemMonitor.pollTelemetryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), systemMonitor.getHardwareTelemetry())

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    private val _launchProgress = MutableStateFlow<String?>(null)
    val launchProgress: StateFlow<String?> = _launchProgress.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        scanGames()
        refreshShizuku()
    }

    fun refreshShizuku() {
        shizukuManager.checkStatus()
    }

    fun requestShizukuPermission() {
        shizukuManager.requestPermission()
    }

    fun scanGames() {
        viewModelScope.launch {
            gameRepository.scanAndSyncInstalledGames()
        }
    }

    fun loadAllInstalledApps() {
        viewModelScope.launch {
            _installedApps.value = gameRepository.getAllInstalledApps()
        }
    }

    fun toggleTweak(tweak: TweakItem) {
        viewModelScope.launch {
            val result = optimizationEngine.toggleTweak(tweak)
            _statusMessage.value = if (result.isSuccess) {
                "${tweak.title}: Applied successfully"
            } else {
                "${tweak.title}: Failed (check Terminal/Logs)"
            }
        }
    }

    fun applyAllGlobalTweaks() {
        viewModelScope.launch {
            optimizationEngine.applyAllGlobalTweaks()
            _statusMessage.value = "All Global Hardware Tweaks Dispatched!"
        }
    }

    fun applyCategoryTweaks(category: TweakCategory) {
        viewModelScope.launch {
            optimizationEngine.applyCategoryTweaks(category)
            _statusMessage.value = "${category.title} tweaks applied!"
        }
    }

    fun restoreJoyose() {
        viewModelScope.launch {
            val res = optimizationEngine.restoreAllJoyose()
            _statusMessage.value = if (res.isSuccess) "Joyose Service Re-enabled" else "Failed to restore Joyose"
        }
    }

    fun flushRam() {
        viewModelScope.launch {
            val result = optimizationEngine.flushMemory()
            _statusMessage.value = result
        }
    }

    fun autoSmoothGame(packageName: String? = null) {
        viewModelScope.launch {
            val result = optimizationEngine.autoSmoothGame(packageName)
            _statusMessage.value = result
        }
    }

    fun applyUltraDeepOverrides() {
        viewModelScope.launch {
            val result = optimizationEngine.applyUltraDeepOverrides()
            _statusMessage.value = result
        }
    }

    fun launchGame(game: GameEntity, config: GameProfileConfig, launchHud: Boolean) {
        viewModelScope.launch {
            _launchProgress.value = "Preparing launch sequence..."
            gameRepository.updateLastLaunched(game.packageName)
            val success = optimizationEngine.launchGameWithProfile(
                packageName = game.packageName,
                appName = game.appName,
                config = config,
                launchHud = launchHud,
                onProgress = { _launchProgress.value = it }
            )
            _launchProgress.value = null
            if (!success) {
                _statusMessage.value = "Could not launch ${game.appName}. Verify package installation."
            }
        }
    }

    fun dismissLaunchDialog() {
        _launchProgress.value = null
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun saveGameProfile(packageName: String, appName: String, config: GameProfileConfig) {
        viewModelScope.launch {
            gameRepository.saveGameProfile(packageName, appName, config)
            _statusMessage.value = "Profile saved for $appName"
        }
    }

    fun addCustomGame(packageName: String, appName: String, profileType: GameProfileType) {
        viewModelScope.launch {
            val config = GameProfileConfig(profileType = profileType)
            gameRepository.saveGameProfile(packageName, appName, config)
            _statusMessage.value = "Added $appName to Game Library"
        }
    }

    fun removeGame(packageName: String) {
        viewModelScope.launch {
            gameRepository.removeGame(packageName)
        }
    }

    fun executeManualCommand(cmd: String) {
        viewModelScope.launch {
            shizukuManager.executeCommand(cmd)
        }
    }

    fun clearCommandLogs() {
        shizukuManager.clearLogs()
    }

    override fun onCleared() {
        super.onCleared()
        shizukuManager.cleanup()
    }
}
