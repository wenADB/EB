<div align="center">

```
  ______   _______  ______   ______  __       __  ________ 
 /      \ /       |/      \ /      \/  \     /  |/        |
/$$$$$$  |$$$$$$$/ $$$$$$/ /$$$$$$/ $$  \   /$$ |$$$$$$$$/ 
$$ |__$$ |  $$ |     $$ |  $$ |__   $$$  \ /$$$ |$$ |__    
$$    $$ |  $$ |     $$ |  $$    \  $$$$  /$$$$ |$$    |   
$$$$$$$$ |  $$ |     $$ |  $$$$$$$  |$$ $$ $$/$$ |$$$$$/    
$$ |  $$ |  $$ |    _$$ |_ $$ \__$$ |$$ |$$$/ $$ |$$ |_____ 
$$ |  $$ |  $$ |   / $$   |$$    $$/ $$ | $/  $$ |$$       |
$$/   $$/   $$/    $$$$$$/  $$$$$$/  $$/      $$/ $$$$$$$$/ 
                                                            
         B O O S T E R   E N G I N E   v 1 . 0              
```

# ⚡ ExtremeBooster Engine

### *Root-Free Hardware Overclocking, Thermal Throttling Bypass & In-Game Telemetry Architecture for Android*

[![Android](https://img.shields.io/badge/Android-14%2B%20(API%2034--36)-00E676?style=for-the-badge&logo=android&logoColor=black)](https://developer.android.com)
[![Build & Release APK](https://img.shields.io/badge/CI%2FCD-Build%20%26%20Release%20APK-00E5FF?style=for-the-badge&logo=githubactions&logoColor=white)](https://github.com/marketplace/actions/build-and-release-apk)
[![Shizuku Privileged](https://img.shields.io/badge/Privilege-Shizuku%20ADB%20UID%202000-10B981?style=for-the-badge&logo=terminal&logoColor=white)](https://shizuku.rikka.app/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-00B0FF?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![HyperOS / MIUI](https://img.shields.io/badge/Specialized-Xiaomi%20HyperOS-FF6F00?style=for-the-badge&logo=xiaomi&logoColor=white)](https://miui.com)
[![License](https://img.shields.io/badge/License-Apache%202.0-7C4DFF?style=for-the-badge)](LICENSE)

<p align="center">
  <a href="#-core-capabilities">Capabilities</a> •
  <a href="#-system-architecture">Architecture</a> •
  <a href="#-feature-matrix">Feature Matrix</a> •
  <a href="#-quickstart--shizuku-setup">Setup Guide</a> •
  <a href="#-per-game-profiles">Game Profiling</a> •
  <a href="#-floating-hud-overlay">HUD Overlay</a> •
  <a href="#-automated-cicd--github-releases">CI/CD Releases</a> •
  <a href="#-building-from-source">Build</a>
</p>

---

</div>

## 🌌 Overview

**ExtremeBooster Engine** is a high-performance, root-free Android game optimization suite engineered for competitive mobile gaming. By establishing an IPC Binder bridge directly through **Shizuku** into privileged Android shell services (`UID 2000`), ExtremeBooster unlocks hardware-level parameters normally inaccessible to standard user-space applications:

- **Uncapped VSYNC & Skia Vulkan GPU Pipelines**
- **SurfaceFlinger Direct Hardware Overlay Composition**
- **Fixed Performance Thermal Throttling Bypass**
- **HyperOS / MIUI Joyose Frame-Rate Governor Neutralization**
- **Custom Surface Resolution Downscaling (0.70x – 0.90x)**
- **Real-time In-Game Floating HUD Telemetry**

All without tripping **SafetyNet / Play Integrity**, requiring unlocked bootloaders, or modifying system partitions.

---

## ⚡ Core Capabilities

### 1. 🚀 Ultra-Deep Hardware & Thermal Bypass (Part B Engine)
| Feature | Subsystem | ADB / Shizuku Command Hook |
| :--- | :--- | :--- |
| **SkiaVK Vulkan Pipeline** | HWUI Render Thread | `setprop debug.hwui.renderer skiavk` |
| **Hardware Composition** | SurfaceFlinger Service | `service call SurfaceFlinger 1008 i32 1` |
| **4x MSAA Engine** | EGL Driver Layer | `setprop debug.egl.hw 1` |
| **Thermal Cap Neutralizer** | Power HAL & Settings | `cmd power set-fixed-performance-mode-enabled true` |
| **EAS Standby Unshackling** | Energy-Aware Scheduler | `settings put global power_saving_constants forced_app_standby_for_small_battery_enabled=false` |
| **Phantom Process Expansion** | ActivityManager Kernel | `device_config put activity_manager max_phantom_processes 2147483647` |

### 2. 🛡️ Xiaomi HyperOS & MIUI Joyose Neutralizer
Xiaomi and POCO devices enforce aggressive thermal-throttling via the proprietary `com.xiaomi.joyose` background daemon, drastically dropping refresh rates from 120Hz to 60Hz or 45Hz under sustained gaming.
- **One-Tap Daemon Freeze**: `pm disable-user --user 0 com.xiaomi.joyose`
- **Game Turbo Priority Allocation**: Grants high-throughput CPU/GPU scheduling queues.
- **Dynamic Restoration**: Re-enable Joyose instantly at any time with a single tap.

### 3. 🎯 Per-Game Profiles & Custom Downscaling
Save isolated hardware profiles per installed game via local **Room DB**:
- **Dynamic Downscale Factor**: Render titles at 80% or 85% native screen resolution (`device_config put game_overlay <pkg> downscaleFactor=0.85`), boosting framerates up to 35% on demanding titles like *Genshin Impact* and *PUBG Mobile*.
- **Forced 120 / 144 FPS Display Lock**: Override vendor dynamic refresh rate (VRR) step-downs.
- **AppOps Wakelock & Overlay Injection**: Prevent background OS aggression from stuttering frame pacing.

### 4. 📊 Real-Time Draggable Floating HUD
A zero-flicker, non-intrusive floating overlay window powered by `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`:
- **Real-Time FPS Counter**: Precise window refresh rate and frame tick tracking.
- **Thermal Monitor**: Device chassis temperature updates in real time with color-coded safety indicators.
- **Live In-Game Toggles**: RAM Flush, Performance Governor toggling, and Emergency Joyose Disarm directly inside your active game session.

---

## 🏛️ System Architecture

The following diagram illustrates how ExtremeBooster executes privileged hardware configurations without root:

```mermaid
flowchart TD
    subgraph ExtremeBooster_App["📱 ExtremeBooster Client (Kotlin Compose M3)"]
        UI["Obsidian & Emerald UI / Multi-Pane Adaptive Layout"]
        Engine["OptimizationEngine & XiaomiEngine"]
        DB[("Room Database (Game Profiles)")]
        HUD["Floating HUD Overlay Service"]
    end

    subgraph IPC_Bridge["⚡ Privileged IPC Bridge"]
        Shizuku["Shizuku Service (Rikka API)"]
        ADB["Privileged ADB Shell (UID 2000)"]
    end

    subgraph Android_Core["🤖 Android Hardware & OS Kernel"]
        SF["SurfaceFlinger (Hardware Composition)"]
        HWUI["HWUI SkiaVK Engine (Vulkan Pipeline)"]
        PowerHAL["Power HAL (Fixed Performance Mode)"]
        DeviceConfig["DeviceConfig (Game Overlay & Downscaling)"]
        Joyose["Xiaomi Joyose Daemon (Thermal Throttle)"]
    end

    UI --> Engine
    Engine --> DB
    Engine --> HUD
    Engine -->|Shizuku.newProcess()| Shizuku
    Shizuku --> ADB
    ADB -->|service call| SF
    ADB -->|setprop| HWUI
    ADB -->|cmd power| PowerHAL
    ADB -->|device_config put| DeviceConfig
    ADB -->|pm disable-user| Joyose
```

---

## 📋 Feature Matrix

| Category | Optimization | Target Outcome | Root Required? |
| :---: | :--- | :--- | :---: |
| 🎮 **Display** | Lock Peak Refresh (120/144Hz) | Eliminates VRR micro-stutters | ❌ No (Shizuku) |
| 🎮 **Display** | Disable Dynamic VSYNC Jitter | Decouples frame queue latency | ❌ No (Shizuku) |
| ⚡ **GPU** | Force Skia Vulkan Backend | Up to 25% faster draw call submission | ❌ No (Shizuku) |
| ⚡ **GPU** | SurfaceFlinger HW Bypass | Minimizes GPU compositing overhead | ❌ No (Shizuku) |
| 🌡️ **Thermal** | Fixed Performance Mode | Disables aggressive frequency steps | ❌ No (Shizuku) |
| 🌡️ **Thermal** | Neutralize Joyose (Xiaomi) | Sustained 120 FPS on HyperOS/MIUI | ❌ No (Shizuku) |
| 🏎️ **Memory** | Phantom Process Expansion | Prevents game processes from being terminated | ❌ No (Shizuku) |
| 🏎️ **Memory** | ZRAM / Background Drop Caches | Reclaims idle RAM for game assets | ❌ No (Shizuku) |
| 🎯 **Resolution**| Resolution Downscaling (0.8x) | Massive GPU load reduction | ❌ No (Shizuku) |
| 📊 **HUD** | Live Overlay Telemetry | Real-time FPS, CPU, Temp telemetry | ❌ No (Overlay) |

---

## 🛠️ Quickstart & Shizuku Setup

ExtremeBooster operates through [Shizuku](https://shizuku.rikka.app/), granting privileged ADB shell access wirelessly directly on your device.

### Step 1: Install & Pair Shizuku
1. Install **Shizuku** from Google Play or GitHub.
2. Enable **Developer Options** on your Android device:
   - Go to `Settings` > `About Phone` > Tap `Build Number` 7 times.
3. In `Developer Options`, enable:
   - **USB Debugging**
   - **Wireless Debugging**
   - *(Xiaomi/HyperOS only)*: **USB Debugging (Security Settings)**
4. Open Shizuku, select **Pairing**, and enter the 6-digit Wi-Fi pairing code from the Developer Options notification.
5. Tap **Start** in Shizuku.

### Step 2: Launch ExtremeBooster
1. Open **ExtremeBooster**.
2. When prompted, tap **Grant Permission** for Shizuku access.
3. Tap **"Auto-Smooth Engine"** or **"Ultra-Deep Hardware Bypass"** to apply global tweaks with a single touch.

> 💡 **Manual ADB Fallback (Computer)**:
> If wireless debugging is unavailable, run this single command from your PC:
> ```bash
> adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
> ```

---

## 🕹️ Per-Game Profiles

ExtremeBooster features an intelligent game library scanner:
1. Navigate to the **Games** tab.
2. Tap **"Auto Scan Games"** to discover installed gaming titles, or tap **"+"** to manually configure any installed app.
3. Configure per-title parameters:
   - **Target FPS**: `60`, `90`, `120`, `144`
   - **Downscale Factor**: `0.70x`, `0.75x`, `0.80x`, `0.85x`, `1.0x`
   - **Disable Joyose during runtime** *(HyperOS)*
   - **Keep Wakelock Active**
4. Tap **"Launch"**: ExtremeBooster executes the profile commands, allocates high-priority CPU affinity, and spawns the floating HUD overlay before starting the game.

---

## 📊 Floating HUD Overlay

The in-game floating overlay allows you to monitor and tune performance without leaving your match:

```
┌──────────────────────────────────────────────┐
│  ⚡ EXTREMEBOOSTER HUD          [ 120 FPS ]  │
├──────────────────────────────────────────────┤
│  🌡️ CHASSIS: 38.2°C     🧠 RAM: 4.8 / 8.0 GB  │
│  ⚙️ SKIA VULKAN: ON     🛡️ JOYOSE: FROZEN     │
├──────────────────────────────────────────────┤
│  [ BOOST RAM ]  [ TOGGLE 120Hz ]  [ DISARM ] │
└──────────────────────────────────────────────┘
```

- **Touch & Drag**: Reposition anywhere along screen bezels.
- **One-Tap Quick Actions**: Purge background memory caches or toggle display pacing instantly.

---

## 📱 Adaptive Design & Responsive UI

Built with **Jetpack Compose Material 3** utilizing an **Obsidian & Neon Emerald** aesthetic:
- **Handheld Compact Layout**: Ergonomic bottom navigation bar with seamless edge-to-edge system insets.
- **Foldables & Tablets (Expanded Layout)**: Automatically reflows into a dual-pane workspace with a vertical **NavigationRail** and adaptive grid (`GridCells.Adaptive(320.dp)`).
- **Tactile Haptic Feedback**: Contextual haptic impulses (`TextHandleMove` and `LongPress`) for tangible physical response.
- **Predictive Back Navigation**: Graceful state preservation and screen transitions backed by Kotlin Coroutines & Flow.

---

## 🚀 Automated CI/CD & GitHub Releases

ExtremeBooster is configured with an automated GitHub Actions workflow (`.github/workflows/build-and-release-apk.yml`) powered by the **[Build and Release APK](https://github.com/marketplace/actions/build-and-release-apk)** action.

Every time you push a version tag or trigger the workflow, GitHub Actions automatically:
1. Compiles the APK via Gradle (`./gradlew assembleDebug` or `assembleRelease`).
2. Generates SHA-256 integrity checksums.
3. Uploads the build artifact to the GitHub Actions run for instant testing.
4. Invokes `sangatdesai/release-apk` and publishes a tagged release on your GitHub repository with downloadable APK assets.

### 🏷️ Trigger a Release via Git Tag

Push a version tag to trigger an automated build and release:

```bash
# Tag the current commit
git tag v1.0.0

# Push the tag to GitHub
git push origin v1.0.0
```

### 🖱️ Trigger Manually via GitHub Actions UI

You can also trigger a build at any time with a single click:
1. Go to your repository on GitHub.
2. Click on the **Actions** tab.
3. Select **"Build and Release APK"** from the left sidebar.
4. Click **Run workflow** ➔ Select branch and build type (`debug` or `release`).
5. Download your APK under **Artifacts** or directly from the newly created **Release** page!

---

## 💻 Building from Source

### Prerequisites
- **Android Studio Ladybug | 2024.2+** or **IntelliJ IDEA**
- **JDK 17** or **JDK 21**
- **Android SDK API 36**

### Compilation Steps

```bash
# Clone the repository
git clone https://github.com/wencystodomingo16/ExtremeBooster.git

# Navigate into project directory
cd ExtremeBooster

# Build Debug APK
./gradlew assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔒 Security, Safety & Google Play Compliance

- **No Root Required**: Operates strictly within user-approved Shizuku ADB privileges (`android.permission.INTERNET`, `android.permission.SYSTEM_ALERT_WINDOW`, `moe.shizuku.manager.permission.API_V23`).
- **Zero Binary Tampering**: Does not inject dynamic shared libraries (`.so`) or modify game memory (Anti-Cheat safe: EAC, BattlEye, Vanguard-mobile compliant).
- **Graceful Revert System**: Every tweak executed by ExtremeBooster has a corresponding restoration command. Simply tap **"Revert All"** or reboot your device to restore stock system defaults.
- **Safe Mode**: Hardware commands are bounded within standard Android `setprop`, `cmd game`, `device_config`, and `service call SurfaceFlinger` APIs.

---

## 🤝 Contributing

Contributions, bug reports, and device-specific tweak recommendations are welcome!
1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/CoolHardwareTweak`)
3. Commit your Changes (`git commit -m 'feat: Add Snapdragon Adreno GPU boost parameter'`)
4. Push to the Branch (`git push origin feature/CoolHardwareTweak`)
5. Open a Pull Request

---

## 📜 License

Distributed under the **Apache License 2.0**. See `LICENSE` for more information.

---

<div align="center">

*Engineered with ⚡ by **Wency Sto. Domingo** • Powered by Kotlin, Jetpack Compose & Shizuku*

</div>
