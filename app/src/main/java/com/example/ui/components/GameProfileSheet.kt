package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.GameEntity
import com.example.data.model.GameProfileConfig
import com.example.data.model.GameProfileType
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.HyperAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TurboCrimson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameProfileSheet(
    game: GameEntity,
    isXiaomiDevice: Boolean,
    onDismiss: () -> Unit,
    onSaveProfile: (GameProfileConfig) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentConfig = remember(game) { game.toProfileConfig() }

    var selectedType by remember { mutableStateOf(currentConfig.profileType) }
    var downscaleFactor by remember { mutableFloatStateOf(currentConfig.downscaleFactor) }
    var targetFps by remember { mutableIntStateOf(currentConfig.targetFps) }
    var aotCompile by remember { mutableStateOf(currentConfig.aotCompileSpeed) }
    var angleVulkan by remember { mutableStateOf(currentConfig.enableAngleVulkan) }
    var dozeWhitelist by remember { mutableStateOf(currentConfig.whitelistDoze) }
    var joyoseBypass by remember { mutableStateOf(currentConfig.bypassJoyose) }
    var gameTurbo by remember { mutableStateOf(currentConfig.enableGameTurbo) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CyberSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Tuning Profile: ${game.appName}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = game.packageName,
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Preset Selector
            Text(
                text = "PERFORMANCE PRESET",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GameProfileType.values().forEach { type ->
                    val isSelected = selectedType == type
                    val (color, badge) = when (type) {
                        GameProfileType.EXTREME_PERFORMANCE -> Pair(TurboCrimson, "120 FPS • 80% Buffer • AOT Speed")
                        GameProfileType.BALANCED -> Pair(ElectricBlue, "90 FPS • 90% Buffer • Standard Mode")
                        GameProfileType.BATTERY_SAVER -> Pair(NeonGreen, "60 FPS • 60% Buffer • Battery Mode")
                        GameProfileType.CUSTOM -> Pair(HyperAmber, "Full Granular Control")
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) color.copy(alpha = 0.15f) else CyberSurfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) color else CyberCardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedType = type
                                when (type) {
                                    GameProfileType.EXTREME_PERFORMANCE -> {
                                        downscaleFactor = 0.8f
                                        targetFps = 120
                                        aotCompile = true
                                    }
                                    GameProfileType.BALANCED -> {
                                        downscaleFactor = 0.9f
                                        targetFps = 90
                                        aotCompile = false
                                    }
                                    GameProfileType.BATTERY_SAVER -> {
                                        downscaleFactor = 0.6f
                                        targetFps = 60
                                        aotCompile = false
                                    }
                                    GameProfileType.CUSTOM -> {}
                                }
                            }
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = type.displayName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) color else TextPrimary
                                )
                                if (isSelected) {
                                    Text(
                                        text = "ACTIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = color
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = badge,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Granular Hardware Parameters
            Text(
                text = "HARDWARE PARAMETERS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Backbuffer Downscale Factor Slider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberSurfaceVariant)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Backbuffer Downscale Factor",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${(downscaleFactor * 100).toInt()}% Resolution",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "Reduces physical GPU rasterization load to eliminate thermal throttling while sustaining higher FPS",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Slider(
                        value = downscaleFactor,
                        onValueChange = {
                            downscaleFactor = it
                            selectedType = GameProfileType.CUSTOM
                        },
                        valueRange = 0.5f..1.0f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = Color(0xFF1E293B)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Target FPS Selector
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberSurfaceVariant)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "Target Frame Rate Lock",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(60, 90, 120, 144).forEach { fps ->
                            val isSelected = targetFps == fps
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else Color(0xFF0C1424))
                                    .border(1.dp, if (isSelected) NeonCyan else CyberCardBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        targetFps = fps
                                        selectedType = GameProfileType.CUSTOM
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$fps Hz",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) NeonCyan else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Extra Overrides Toggles
            ProfileToggleRow(
                title = "Pre-compile Bytecode Ahead-Of-Time (AOT)",
                subtitle = "Executes 'cmd package compile -m speed' to eliminate in-game JIT stutters",
                checked = aotCompile,
                onCheckedChange = {
                    aotCompile = it
                    selectedType = GameProfileType.CUSTOM
                }
            )

            ProfileToggleRow(
                title = "Doze Whitelist & Process Wakelock",
                subtitle = "Prevents system power manager from sleeping game render threads",
                checked = dozeWhitelist,
                onCheckedChange = { dozeWhitelist = it }
            )

            ProfileToggleRow(
                title = "Enforce ANGLE Vulkan Driver",
                subtitle = "Renders game through Vulkan multi-threaded command buffers",
                checked = angleVulkan,
                onCheckedChange = { angleVulkan = it }
            )

            if (isXiaomiDevice) {
                ProfileToggleRow(
                    title = "Bypass Xiaomi Joyose Dynamic Throttling",
                    subtitle = "Force-stops com.xiaomi.joyose to avoid 40°C frame throttling",
                    checked = joyoseBypass,
                    onCheckedChange = { joyoseBypass = it }
                )

                ProfileToggleRow(
                    title = "Xiaomi Game Turbo Hardware Registers",
                    subtitle = "Sets game_booster_mode and unlocks Level 3 touch digitizer sensitivity",
                    checked = gameTurbo,
                    onCheckedChange = { gameTurbo = it }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val config = GameProfileConfig(
                        profileType = selectedType,
                        downscaleFactor = downscaleFactor,
                        targetFps = targetFps,
                        aotCompileSpeed = aotCompile,
                        enableAngleVulkan = angleVulkan,
                        whitelistDoze = dozeWhitelist,
                        grantWakelock = true,
                        bypassJoyose = joyoseBypass,
                        enableGameTurbo = gameTurbo,
                        touchResponseLevel = 3
                    )
                    onSaveProfile(config)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text(
                    text = "SAVE PROFILE CONFIGURATION",
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CyberSurfaceVariant)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 11.sp, color = TextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NeonCyan,
                    checkedTrackColor = Color(0xFF0C3844),
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = Color(0xFF1E293B)
                )
            )
        }
    }
}
