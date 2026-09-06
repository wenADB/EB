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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shizuku.ShizukuStatus
import com.example.ui.theme.SleekAmber
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekCardBorder
import com.example.ui.theme.SleekCrimson
import com.example.ui.theme.SleekEmerald
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceDark
import com.example.ui.theme.SleekTextBody
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextWhite

@Composable
fun ShizukuStatusBanner(
    status: ShizukuStatus,
    onRequestPermission: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAdbGuide by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current

    val (badgeBg, borderColor, title, desc, icon, accentColor) = when (status) {
        ShizukuStatus.AVAILABLE_AUTHORIZED -> Tuple6(
            SleekSurface,
            SleekCardBorder,
            "Shizuku Runtime Bound",
            "Root-free elevated privileges granted. Hardware overrides active.",
            Icons.Default.CheckCircle,
            SleekEmerald
        )
        ShizukuStatus.AVAILABLE_UNAUTHORIZED -> Tuple6(
            SleekSurface,
            SleekCardBorder,
            "Authorization Required",
            "Shizuku service detected. Tap Authorize to grant elevated shell access.",
            Icons.Default.Warning,
            SleekAmber
        )
        ShizukuStatus.PERMISSION_DENIED -> Tuple6(
            SleekSurface,
            SleekCardBorder,
            "Permission Denied",
            "Authorization denied. Enable ExtremeBooster in the Shizuku app.",
            Icons.Default.Warning,
            SleekCrimson
        )
        ShizukuStatus.UNAVAILABLE -> Tuple6(
            SleekSurface,
            SleekCardBorder,
            "Shizuku Service Inactive",
            "Start Shizuku via Wireless Debugging or ADB to enable overrides.",
            Icons.Default.HelpOutline,
            SleekTextMuted
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(badgeBg)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .padding(16.dp)
            .testTag("shizuku_status_banner")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Glowing Indicator Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextWhite
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = SleekTextMuted,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            when (status) {
                ShizukuStatus.AVAILABLE_AUTHORIZED -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(SleekEmerald.copy(alpha = 0.15f))
                            .border(1.dp, SleekEmerald.copy(alpha = 0.3f), RoundedCornerShape(percent = 50))
                            .clickable { onRefresh() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekEmerald,
                            letterSpacing = 1.sp
                        )
                    }
                }
                ShizukuStatus.AVAILABLE_UNAUTHORIZED -> {
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SleekEmerald,
                            contentColor = Color(0xFF0A0B0E)
                        ),
                        shape = RoundedCornerShape(percent = 50),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            "AUTHORIZE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
                else -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = onRefresh,
                            modifier = Modifier.height(34.dp),
                            shape = RoundedCornerShape(percent = 50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekEmerald,
                                contentColor = Color(0xFF0A0B0E)
                            )
                        ) {
                            Text("RE-CHECK", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                        }
                        OutlinedButton(
                            onClick = { showAdbGuide = true },
                            modifier = Modifier.height(34.dp),
                            shape = RoundedCornerShape(percent = 50),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextMuted),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                        ) {
                            Text("GUIDE", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                        }
                    }
                }
            }
        }
    }

    if (showAdbGuide) {
        AlertDialog(
            onDismissRequest = { showAdbGuide = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = SleekEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Shizuku & ADB Setup", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SleekTextWhite)
                }
            },
            text = {
                Column {
                    Text(
                        text = "ExtremeBooster Engine uses Shizuku to execute non-root system performance tweaks (cmd power, settings put, device_config).",
                        fontSize = 12.sp,
                        color = SleekTextBody
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Option A: Wireless Debugging (No PC)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekEmerald
                    )
                    Text(
                        text = "1. Install Shizuku from Play Store or GitHub\n2. Open Shizuku -> Start via Wireless Debugging\n3. Pair in Developer Options & Start service",
                        fontSize = 12.sp,
                        color = SleekTextBody
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Option B: ADB via PC Terminal",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekEmerald
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SleekSurfaceDark)
                            .border(1.dp, SleekCardBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                clipboardManager.setText(
                                    AnnotatedString("adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh")
                                )
                            }
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SleekEmerald
                        )
                    }
                    Text(
                        text = "(Tap command box above to copy to clipboard)",
                        fontSize = 10.sp,
                        color = SleekTextMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAdbGuide = false
                        onRefresh()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleekEmerald,
                        contentColor = Color(0xFF0A0B0E)
                    ),
                    shape = RoundedCornerShape(percent = 50)
                ) {
                    Text("Check Status", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdbGuide = false }) {
                    Text("Close", color = SleekTextMuted)
                }
            },
            containerColor = SleekSurface
        )
    }
}

private data class Tuple6<A, B, C, D, E, F>(
    val a: A, val b: B, val c: C, val d: D, val e: E, val f: F
)
