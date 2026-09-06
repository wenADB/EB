package com.example.data.model

enum class GameProfileType(val displayName: String, val description: String) {
    EXTREME_PERFORMANCE(
        "Extreme Performance",
        "80% render downscale, 120/144Hz cap, AOT bytecode speed compile & CPU clock lock"
    ),
    BALANCED(
        "Balanced",
        "90% render downscale, 90Hz pacing, standard game mode & power balance"
    ),
    BATTERY_SAVER(
        "Battery Saver",
        "60% downscale, 60 FPS cap, battery game mode & throttling mitigation"
    ),
    CUSTOM(
        "Custom Hardware Tuner",
        "User-defined resolution scale, target FPS, ANGLE Vulkan translation, and GPU parameters"
    )
}

data class GameProfileConfig(
    val profileType: GameProfileType = GameProfileType.EXTREME_PERFORMANCE,
    val downscaleFactor: Float = 0.8f,
    val targetFps: Int = 120,
    val aotCompileSpeed: Boolean = true,
    val enableAngleVulkan: Boolean = true,
    val whitelistDoze: Boolean = true,
    val grantWakelock: Boolean = true,
    val bypassJoyose: Boolean = true,
    val enableGameTurbo: Boolean = true,
    val touchResponseLevel: Int = 3
)

enum class TweakCategory(val title: String, val iconName: String) {
    ULTRA_DEEP_HARDWARE("Ultra-Deep Hardware & Thermal Bypass", "ultra_deep"),
    DISPLAY_VSYNC("VSYNC & Frame Pacing", "display"),
    CPU_GOVERNOR("CPU & Thermal Governor", "cpu"),
    REFRESH_TOUCH("Refresh Rate & Touch Latency", "touch"),
    HWUI_MEMORY("HWUI Pipeline & Memory", "memory"),
    NETWORK("Network Latency Stack", "network"),
    XIAOMI_HYPEROS("Xiaomi HyperOS / MIUI", "xiaomi")
}

data class TweakItem(
    val id: String,
    val title: String,
    val description: String,
    val category: TweakCategory,
    val command: String,
    val isEnabled: Boolean = false,
    val isXiaomiOnly: Boolean = false,
    val revertCommand: String? = null
)

data class CommandLogEntry(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val command: String,
    val exitCode: Int,
    val output: String,
    val isSuccess: Boolean = exitCode == 0
)

data class HardwareTelemetry(
    val cpuLoadPercent: Int = 35,
    val ramUsedMb: Long = 0L,
    val ramTotalMb: Long = 0L,
    val batteryTempC: Float = 32.0f,
    val batteryLevel: Int = 85,
    val refreshRateHz: Float = 120f,
    val isCharging: Boolean = false,
    val deviceVendor: String = "Generic",
    val deviceModel: String = "Android Device",
    val isXiaomiDevice: Boolean = false,
    val hyperOsOrMiuiVersion: String? = null
) {
    val ramUsagePercent: Int
        get() = if (ramTotalMb > 0) ((ramUsedMb.toDouble() / ramTotalMb) * 100).toInt().coerceIn(0, 100) else 0

    val thermalStatus: String
        get() = when {
            batteryTempC >= 44f -> "Critical Throttle Risk"
            batteryTempC >= 40f -> "Warm (Joyose Trigger Zone)"
            batteryTempC >= 36f -> "Optimal High Performance"
            else -> "Cool"
        }
}
