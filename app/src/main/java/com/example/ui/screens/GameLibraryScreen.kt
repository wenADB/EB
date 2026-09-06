package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.GameEntity
import com.example.ui.MainViewModel
import com.example.ui.components.AddGameDialog
import com.example.ui.components.GameItemCard
import com.example.ui.components.GameProfileSheet
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
fun GameLibraryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allGames by viewModel.allGames.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val launchProgress by viewModel.launchProgress.collectAsStateWithLifecycle()
    val telemetry by viewModel.hardwareTelemetry.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var editingGame by remember { mutableStateOf<GameEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Predictive back gesture: closes open dialog or configuration sheet
    BackHandler(enabled = showAddDialog || editingGame != null) {
        showAddDialog = false
        editingGame = null
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SleekBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.loadAllInstalledApps()
                    showAddDialog = true
                },
                containerColor = SleekEmerald,
                contentColor = Color(0xFF0A0B0E),
                shape = CircleShape,
                modifier = Modifier.testTag("add_game_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Game")
            }
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 320.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(10.dp))
                // Sleek Section Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "GAME LIBRARY",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextWhite,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${allGames.size} Titles Configured",
                            fontSize = 11.sp,
                            color = SleekTextMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(SleekSurface)
                            .border(1.dp, SleekCardBorder, RoundedCornerShape(percent = 50))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.scanGames()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("refresh_games_button")
                    ) {
                        Text(
                            text = "SCAN NEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekEmerald,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            if (allGames.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SleekSurface)
                            .border(1.dp, SleekCardBorder, RoundedCornerShape(24.dp))
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Gamepad,
                                contentDescription = null,
                                tint = SleekTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Games Detected",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextWhite
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Automatically scan for installed 3D games or register any emulator manually.",
                                fontSize = 12.sp,
                                color = SleekTextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.scanGames()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SleekEmerald,
                                        contentColor = Color(0xFF0A0B0E)
                                    ),
                                    shape = RoundedCornerShape(percent = 50)
                                ) {
                                    Text("Scan Games", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.loadAllInstalledApps()
                                        showAddDialog = true
                                    },
                                    shape = RoundedCornerShape(percent = 50),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                                ) {
                                    Text("Add Manually", color = SleekTextWhite)
                                }
                            }
                        }
                    }
                }
            } else {
                items(allGames, key = { it.packageName }) { game ->
                    GameItemCard(
                        game = game,
                        onLaunch = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.launchGame(game, game.toProfileConfig(), launchHud = true)
                        },
                        onConfigure = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            editingGame = game
                        },
                        onDelete = {
                            viewModel.removeGame(game.packageName)
                        }
                    )
                }

                // Add Manual Entry Sleek Card
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SleekSurface.copy(alpha = 0.6f))
                            .border(1.dp, SleekCardBorderSubtle, RoundedCornerShape(24.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.loadAllInstalledApps()
                                showAddDialog = true
                            }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SleekSurfaceDark)
                                    .border(1.dp, SleekCardBorder, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = SleekTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Add manual entry...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = SleekTextMuted,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Profile Tuning Sheet
    editingGame?.let { game ->
        GameProfileSheet(
            game = game,
            isXiaomiDevice = telemetry.isXiaomiDevice,
            onDismiss = { editingGame = null },
            onSaveProfile = { config ->
                viewModel.saveGameProfile(game.packageName, game.appName, config)
            }
        )
    }

    // Add Game Dialog
    if (showAddDialog) {
        AddGameDialog(
            installedApps = installedApps,
            existingPackageNames = remember(allGames) { allGames.map { it.packageName }.toSet() },
            onDismiss = { showAddDialog = false },
            onAddApp = { app, profileType ->
                viewModel.addCustomGame(app.packageName, app.appName, profileType)
            }
        )
    }

    // Launch Sequence Dialog
    launchProgress?.let { progressText ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissLaunchDialog() },
            title = {
                Text("Engaging Turbo Pipeline", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SleekTextWhite)
            },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = SleekEmerald,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = progressText, fontSize = 13.sp, color = SleekTextBody)
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLaunchDialog() }) {
                    Text("Cancel", color = SleekTextMuted)
                }
            },
            containerColor = SleekSurface
        )
    }
}
