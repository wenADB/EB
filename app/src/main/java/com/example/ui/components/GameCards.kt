package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.GameEntity
import com.example.data.model.GameProfileType
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
fun GameItemCard(
    game: GameEntity,
    onLaunch: (GameEntity) -> Unit,
    onConfigure: (GameEntity) -> Unit,
    onDelete: (GameEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val appIconBitmap = remember(game.packageName) {
        try {
            val drawable = context.packageManager.getApplicationIcon(game.packageName)
            drawableToBitmap(drawable)
        } catch (_: Exception) {
            null
        }
    }

    val parsedType = try {
        GameProfileType.valueOf(game.profileType)
    } catch (_: Exception) {
        GameProfileType.EXTREME_PERFORMANCE
    }

    val profileSubtitle = when (parsedType) {
        GameProfileType.EXTREME_PERFORMANCE -> "Extreme Profile • ${(game.downscaleFactor * 100).toInt()}% Res"
        GameProfileType.BALANCED -> "Balanced Profile • ${(game.downscaleFactor * 100).toInt()}% Res"
        GameProfileType.BATTERY_SAVER -> "Battery Saver • ${(game.downscaleFactor * 100).toInt()}% Res"
        GameProfileType.CUSTOM -> "Custom ${game.targetFps} FPS • ${(game.downscaleFactor * 100).toInt()}% Res"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SleekSurface)
            .border(1.dp, SleekCardBorder, RoundedCornerShape(24.dp))
            .padding(14.dp)
            .testTag("game_card_${game.packageName}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF2E3440), Color(0xFF1E222A))
                        )
                    )
                    .border(1.dp, SleekCardBorder, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (appIconBitmap != null) {
                    Image(
                        bitmap = appIconBitmap.asImageBitmap(),
                        contentDescription = game.appName,
                        modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp))
                    )
                } else {
                    Text(
                        text = game.appName.firstOrNull()?.uppercase() ?: "G",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // App Name & Profile Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.appName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextWhite,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = profileSubtitle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = SleekTextMuted,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Tune / Config Icon Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(SleekSurfaceDark)
                    .border(1.dp, SleekCardBorderSubtle, CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onConfigure(game)
                    }
                    .testTag("tune_button_${game.packageName}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Profile Configuration",
                    tint = SleekTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Sleek Launch Pill Button
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLaunch(game)
                },
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekEmerald,
                    contentColor = Color(0xFF0A0B0E)
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp,
                    vertical = 0.dp
                ),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("launch_button_${game.packageName}")
            ) {
                Text(
                    text = "LAUNCH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
            }

            if (game.isCustomApp) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = { onDelete(game) },
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("delete_button_${game.packageName}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove game",
                        tint = SleekTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
