package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.GameEntity
import com.example.service.GameOverlayService
import com.example.shizuku.ShizukuStatus
import com.example.ui.MainViewModel
import com.example.ui.components.HardwareGaugesGrid
import com.example.ui.components.ShizukuStatusBanner
import com.example.ui.components.SleekFeatureStatusPills
import com.example.ui.components.SleekHeroPerformanceCard
import com.example.ui.theme.SleekAmber
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekCardBorder
import com.example.ui.theme.SleekCardBorderSubtle
import com.example.ui.theme.SleekEmerald
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceDark
import com.example.ui.theme.SleekTextBody
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextWhite

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToGames: () -> Unit,
    onNavigateToXiaomi: () -> Unit,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.hardwareTelemetry.collectAsStateWithLifecycle()
    val shizukuStatus by viewModel.shizukuStatus.collectAsStateWithLifecycle()
    val allGames by viewModel.allGames.collectAsStateWithLifecycle()
    val isOptimizing by viewModel.isOptimizing.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sleek Top Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ExtremeBooster",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextWhite,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (shizukuStatus == ShizukuStatus.AVAILABLE_AUTHORIZED) SleekEmerald else SleekAmber)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (shizukuStatus == ShizukuStatus.AVAILABLE_AUTHORIZED) viewModel.shizukuManager.getShizukuDetails().uppercase() else "SHIZUKU STANDBY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (shizukuStatus == ShizukuStatus.AVAILABLE_AUTHORIZED) SleekEmerald else SleekAmber,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            // Settings / Refresh Circular Action Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SleekSurface)
                    .border(1.dp, SleekCardBorder, CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.refreshShizuku()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Telemetry",
                    tint = SleekTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Sleek Hero Performance Card (32dp rounded, gradient, 3 metrics grid)
        SleekHeroPerformanceCard(telemetry = telemetry)

        // Sleek 2-Column Feature Status Pills (Joyose Bypass & Touch Buffer)
        SleekFeatureStatusPills(isJoyoseBypassed = telemetry.isXiaomiDevice)

        // Shizuku Connection Status
        ShizukuStatusBanner(
            status = shizukuStatus,
            onRequestPermission = { viewModel.requestShizukuPermission() },
            onRefresh = { viewModel.refreshShizuku() }
        )

        // Secondary Hardware Gauges (RAM buffer, Thermal state)
        HardwareGaugesGrid(telemetry = telemetry)

        // 1-Click Extreme Boost Master Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF1F1218), SleekSurface)
                    )
                )
                .border(1.dp, SleekCardBorder, RoundedCornerShape(24.dp))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.applyAllGlobalTweaks()
                }
                .padding(18.dp)
                .testTag("one_click_boost_button")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = SleekEmerald,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1-CLICK EXTREME TURBO",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SleekTextWhite,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Applies all 15+ kernel, GPU, HWUI, refresh rate, and touch latency overrides",
                        fontSize = 11.sp,
                        color = SleekTextMuted,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (isOptimizing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = SleekEmerald,
                        strokeWidth = 3.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(SleekEmerald)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "BOOST",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0A0B0E),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // Auto-Smooth Quick Action Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SleekSurface)
                .border(1.dp, SleekEmerald.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.autoSmoothGame()
                }
                .padding(14.dp)
                .testTag("auto_smooth_quick_button")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SleekEmerald.copy(alpha = 0.15f))
                            .border(1.dp, SleekEmerald.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = SleekEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AUTO-SMOOTH ENGINE",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SleekTextWhite,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Lock 120Hz • Flush Cache • Zero Latency",
                            fontSize = 11.sp,
                            color = SleekTextMuted
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(SleekEmerald)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "SMOOTH",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0A0B0E),
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Quick Action 2: Ultra-Deep Hardware Overrides (Part B)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SleekSurface)
                .border(1.dp, SleekCardBorder, RoundedCornerShape(20.dp))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.applyUltraDeepOverrides()
                }
                .padding(14.dp)
                .testTag("ultra_deep_quick_button")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SleekAmber.copy(alpha = 0.15f))
                            .border(1.dp, SleekAmber.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = null,
                            tint = SleekAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ULTRA-DEEP HARDWARE BYPASS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SleekTextWhite,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Skia Vulkan • Thermal Caps • Phantom Limits",
                            fontSize = 11.sp,
                            color = SleekTextMuted
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(SleekAmber)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "OVERRIDE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0A0B0E),
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Quick System Utility Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Flush RAM
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SleekSurface)
                    .border(1.dp, SleekCardBorder, RoundedCornerShape(20.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.flushRam()
                    }
                    .padding(14.dp)
                    .testTag("quick_flush_ram_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = null,
                        tint = SleekEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Flush RAM",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextWhite
                        )
                        Text(
                            text = "Free memory cache",
                            fontSize = 10.sp,
                            color = SleekTextMuted
                        )
                    }
                }
            }

            // Floating HUD Controller
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SleekSurface)
                    .border(1.dp, SleekCardBorder, RoundedCornerShape(20.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (Settings.canDrawOverlays(context)) {
                            val intent = Intent(context, GameOverlayService::class.java).apply {
                                putExtra(GameOverlayService.EXTRA_GAME_NAME, "Manual HUD")
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(intent)
                            } else {
                                context.startService(intent)
                            }
                        } else {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }
                    }
                    .padding(14.dp)
                    .testTag("start_floating_hud_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = SleekAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Floating HUD",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextWhite
                        )
                        Text(
                            text = "In-game overlay",
                            fontSize = 10.sp,
                            color = SleekTextMuted
                        )
                    }
                }
            }
        }

        // Xiaomi Special Banner if Xiaomi Device
        if (telemetry.isXiaomiDevice) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SleekSurface)
                    .border(1.dp, SleekAmber.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .clickable { onNavigateToXiaomi() }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "XIAOMI / HYPEROS HARDWARE ENGINE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SleekAmber,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Joyose bypass, 120Hz Security bypass & Memory Extension killer",
                            fontSize = 11.sp,
                            color = SleekTextMuted
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = SleekAmber
                    )
                }
            }
        }

        // Quick Launch Games Strip
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "GAME LIBRARY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextWhite,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "SCAN NEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SleekEmerald,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.clickable { onNavigateToGames() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (allGames.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SleekSurface)
                        .border(1.dp, SleekCardBorder, RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No games registered yet",
                            fontSize = 13.sp,
                            color = SleekTextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.scanGames() },
                            shape = RoundedCornerShape(percent = 50),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                        ) {
                            Text("Scan Installed Games", color = SleekEmerald)
                        }
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allGames.take(6), key = { it.packageName }) { game ->
                        QuickGameCard(
                            game = game,
                            onLaunch = {
                                viewModel.launchGame(game, game.toProfileConfig(), launchHud = true)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickGameCard(
    game: GameEntity,
    onLaunch: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SleekSurface)
            .border(1.dp, SleekCardBorder, RoundedCornerShape(20.dp))
            .clickable { onLaunch() }
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = game.appName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextWhite,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = game.profileType.replace("_", " "),
                fontSize = 9.sp,
                color = SleekEmerald,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(percent = 50))
                    .background(SleekEmerald)
                    .padding(vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "LAUNCH",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0A0B0E),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
