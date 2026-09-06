package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HardwareTelemetry
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
fun SleekHeroPerformanceCard(
    telemetry: HardwareTelemetry,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(SleekSurface, SleekSurfaceDark)
                )
            )
            .border(1.dp, SleekCardBorder, RoundedCornerShape(32.dp))
            .padding(20.dp)
    ) {
        // Decorative background watermark icon
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 4.dp, top = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.RocketLaunch,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.05f),
                modifier = Modifier.size(72.dp)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Engine Status & Temperature Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "ENGINE STATUS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekTextMuted,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ultra Performance",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Light,
                        color = SleekTextWhite,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "${telemetry.deviceVendor} ${telemetry.deviceModel}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SleekEmerald,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${"%.1f".format(telemetry.batteryTempC)}°C",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekEmerald,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "CORE TEMP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekTextMuted,
                        letterSpacing = 1.sp
                    )
                }
            }

            // 3-Column Sleek Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SleekMetricBox(
                    label = "CPU LOAD",
                    value = "${telemetry.cpuLoadPercent}%",
                    highlight = false,
                    modifier = Modifier.weight(1f)
                )

                SleekMetricBox(
                    label = "FPS LOCK",
                    value = "${telemetry.refreshRateHz.toInt()} HZ",
                    highlight = true,
                    modifier = Modifier.weight(1f)
                )

                SleekMetricBox(
                    label = "RAM USAGE",
                    value = "${telemetry.ramUsagePercent}%",
                    highlight = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SleekMetricBox(
    label: String,
    value: String,
    highlight: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SleekBackground.copy(alpha = 0.6f))
            .border(1.dp, SleekCardBorderSubtle, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = SleekTextMuted,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (highlight) SleekEmerald else SleekTextWhite,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun SleekFeatureStatusPills(
    isJoyoseBypassed: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Joyose Bypass Status Pill
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(SleekSurface)
                .border(1.dp, SleekCardBorder, RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Joyose Bypass",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SleekTextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isJoyoseBypassed) "ENABLED" else "ACTIVE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekEmerald
                    )
                }
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SleekEmerald,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Touch Buffer Status Pill
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(SleekSurface)
                .border(1.dp, SleekCardBorder, RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Touch Buffer",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SleekTextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ZERO LATENCY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekEmerald
                    )
                }
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = SleekEmerald,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Backwards compatibility alias for DeviceHeaderCard & HardwareGaugesGrid
@Composable
fun DeviceHeaderCard(
    telemetry: HardwareTelemetry,
    modifier: Modifier = Modifier
) {
    SleekHeroPerformanceCard(telemetry = telemetry, modifier = modifier)
}

@Composable
fun HardwareGaugesGrid(
    telemetry: HardwareTelemetry,
    modifier: Modifier = Modifier
) {
    // Renders sleek secondary gauges
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricCard(
            title = "RAM BUFFER",
            value = "${telemetry.ramUsedMb} MB",
            subtitle = "${telemetry.ramUsagePercent}% of ${telemetry.ramTotalMb}MB",
            progress = telemetry.ramUsagePercent / 100f,
            icon = Icons.Default.Memory,
            accentColor = SleekEmerald,
            modifier = Modifier.weight(1f)
        )

        MetricCard(
            title = "THERMAL STATE",
            value = telemetry.thermalStatus,
            subtitle = "${"%.1f".format(telemetry.batteryTempC)}°C Monitored",
            progress = ((telemetry.batteryTempC - 25f) / 25f).coerceIn(0f, 1f),
            icon = Icons.Default.Thermostat,
            accentColor = SleekEmerald,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    progress: Float,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioLowBouncy
        ),
        label = "progressAnim"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(SleekSurface)
            .border(1.dp, SleekCardBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SleekTextMuted,
                    letterSpacing = 1.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = value,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextWhite,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = SleekTextMuted,
                        maxLines = 1
                    )
                }

                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(28.dp),
                    color = accentColor,
                    trackColor = SleekBackground,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}
