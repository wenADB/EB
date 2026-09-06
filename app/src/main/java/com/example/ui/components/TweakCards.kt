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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TweakItem
import com.example.ui.theme.SleekAmber
import com.example.ui.theme.SleekCardBorder
import com.example.ui.theme.SleekCardBorderSubtle
import com.example.ui.theme.SleekEmerald
import com.example.ui.theme.SleekEmeraldContainer
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceDark
import com.example.ui.theme.SleekTextBody
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextWhite

@Composable
fun TweakItemCard(
    tweak: TweakItem,
    isActive: Boolean,
    onToggle: (TweakItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val clipboard = LocalClipboardManager.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SleekSurface)
            .border(
                1.dp,
                if (isActive) SleekEmerald.copy(alpha = 0.4f) else SleekCardBorder,
                RoundedCornerShape(20.dp)
            )
            .padding(15.dp)
            .testTag("tweak_card_${tweak.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tweak.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextWhite
                        )
                        if (tweak.isXiaomiOnly) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(SleekAmber.copy(alpha = 0.15f))
                                    .border(0.5.dp, SleekAmber.copy(alpha = 0.3f), RoundedCornerShape(percent = 50))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "HyperOS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekAmber
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = tweak.description,
                        fontSize = 11.sp,
                        color = SleekTextMuted,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Switch(
                    checked = isActive,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggle(tweak)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF0A0B0E),
                        checkedTrackColor = SleekEmerald,
                        uncheckedThumbColor = SleekTextMuted,
                        uncheckedTrackColor = SleekSurfaceDark,
                        uncheckedBorderColor = SleekCardBorder
                    ),
                    modifier = Modifier.testTag("tweak_switch_${tweak.id}")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Command snippet preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SleekSurfaceDark)
                    .border(1.dp, SleekCardBorderSubtle, RoundedCornerShape(10.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        clipboard.setText(AnnotatedString("adb shell ${tweak.command}"))
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = tweak.command,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (isActive) SleekEmerald else SleekTextMuted,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy command",
                    tint = SleekTextMuted,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
