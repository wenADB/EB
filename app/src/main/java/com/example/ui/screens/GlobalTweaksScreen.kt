package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TweakCategory
import com.example.ui.MainViewModel
import com.example.ui.components.TweakItemCard
import com.example.ui.theme.SleekAmber
import com.example.ui.theme.SleekCardBorder
import com.example.ui.theme.SleekEmerald
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceDark
import com.example.ui.theme.SleekTextBody
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextWhite

@Composable
fun GlobalTweaksScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val activeTweaks by viewModel.activeTweaks.collectAsStateWithLifecycle()
    val allTweaks = viewModel.optimizationEngine.defaultTweaks.filter { it.category != TweakCategory.XIAOMI_HYPEROS }
    val haptic = LocalHapticFeedback.current

    val categories = listOf(
        Pair(TweakCategory.ULTRA_DEEP_HARDWARE, Icons.Default.Whatshot),
        Pair(TweakCategory.DISPLAY_VSYNC, Icons.Default.Tv),
        Pair(TweakCategory.CPU_GOVERNOR, Icons.Default.Speed),
        Pair(TweakCategory.REFRESH_TOUCH, Icons.Default.TouchApp),
        Pair(TweakCategory.HWUI_MEMORY, Icons.Default.Memory),
        Pair(TweakCategory.NETWORK, Icons.Default.CellTower)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // PART B Hero Header Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SleekSurface)
                    .border(1.dp, SleekEmerald.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Column {
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
                                    imageVector = Icons.Default.Whatshot,
                                    contentDescription = null,
                                    tint = SleekEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "PART B: ULTRA-DEEP SYSTEM OVERRIDES",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SleekTextWhite,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "Hardware Throughput & Thermal Bypass",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SleekEmerald
                                )
                            }
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.applyUltraDeepOverrides()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekEmerald,
                                contentColor = Color(0xFF0A0B0E)
                            ),
                            shape = RoundedCornerShape(percent = 50),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("apply_all_ultra_deep_button")
                        ) {
                            Text(
                                text = "OVERRIDE ALL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Enforces Skia Vulkan GPU engine, disables SurfaceFlinger overlay limits, forces 4x MSAA, bypasses thermal daemon caps, and neutralizes EAS power limits via Shizuku.",
                        fontSize = 11.sp,
                        color = SleekTextMuted,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        categories.forEach { (cat, icon) ->
            val tweaksInCat = allTweaks.filter { it.category == cat }
            if (tweaksInCat.isNotEmpty()) {
                item {
                    CategoryHeaderRow(
                        title = cat.title,
                        icon = icon,
                        onApplyAll = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.applyCategoryTweaks(cat)
                        }
                    )
                }

                items(tweaksInCat, key = { it.id }) { tweak ->
                    val isActive = activeTweaks[tweak.id] ?: false
                    TweakItemCard(
                        tweak = tweak,
                        isActive = isActive,
                        onToggle = { viewModel.toggleTweak(tweak) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun CategoryHeaderRow(
    title: String,
    icon: ImageVector,
    onApplyAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SleekEmerald,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextWhite,
                letterSpacing = 1.sp
            )
        }

        OutlinedButton(
            onClick = onApplyAll,
            shape = RoundedCornerShape(percent = 50),
            modifier = Modifier.height(30.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekEmerald),
            border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
        ) {
            Text(
                text = "Apply All",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
