package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.MainViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GameLibraryScreen
import com.example.ui.screens.GlobalTweaksScreen
import com.example.ui.screens.TerminalLogsScreen
import com.example.ui.screens.XiaomiEngineScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekCardBorder
import com.example.ui.theme.SleekEmerald
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextNavMuted

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ExtremeBoosterApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshShizuku()
    }
}

enum class NavigationTab(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("ENGINE", Icons.Default.Speed, "tab_dashboard"),
    TWEAKS("TWEAKS", Icons.Default.Tune, "tab_tweaks"),
    XIAOMI("HYPEROS", Icons.Default.LocalFireDepartment, "tab_xiaomi"),
    GAMES("GAMES", Icons.Default.Gamepad, "tab_games"),
    TERMINAL("LOGS", Icons.Default.Terminal, "tab_terminal")
}

@Composable
fun ExtremeBoosterApp(viewModel: MainViewModel) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    // Predictive back gesture: returns to Dashboard tab rather than closing app
    BackHandler(enabled = selectedTabIndex != 0) {
        selectedTabIndex = 0
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBackground)
    ) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            // Adaptive Tablet / Foldable / Landscape Mode: NavigationRail + Detail Pane
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier
                        .fillMaxHeight()
                        .border(0.5.dp, SleekCardBorder),
                    containerColor = SleekSurfaceVariant,
                    header = {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "ULTRA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = SleekEmerald,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                ) {
                    NavigationTab.values().forEachIndexed { index, tab ->
                        val isSelected = selectedTabIndex == index
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedTabIndex = index
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    letterSpacing = 0.8.sp
                                )
                            },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = SleekEmerald,
                                selectedTextColor = SleekEmerald,
                                indicatorColor = SleekEmerald.copy(alpha = 0.14f),
                                unselectedIconColor = SleekTextNavMuted,
                                unselectedTextColor = SleekTextNavMuted
                            ),
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = SleekBackground,
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            AnimatedTabContent(
                                selectedTabIndex = selectedTabIndex,
                                viewModel = viewModel,
                                onNavigate = { selectedTabIndex = it }
                            )
                        }
                    }
                }
            }
        } else {
            // Compact Phone Layout: Standard Scaffold with bottom NavigationBar
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SleekBackground),
                containerColor = SleekBackground,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, SleekCardBorder),
                        containerColor = SleekSurfaceVariant,
                        tonalElevation = 0.dp
                    ) {
                        NavigationTab.values().forEachIndexed { index, tab ->
                            val isSelected = selectedTabIndex == index
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedTabIndex = index
                                },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        letterSpacing = 0.8.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SleekEmerald,
                                    selectedTextColor = SleekEmerald,
                                    indicatorColor = SleekEmerald.copy(alpha = 0.12f),
                                    unselectedIconColor = SleekTextNavMuted,
                                    unselectedTextColor = SleekTextNavMuted
                                ),
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    AnimatedTabContent(
                        selectedTabIndex = selectedTabIndex,
                        viewModel = viewModel,
                        onNavigate = { selectedTabIndex = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedTabContent(
    selectedTabIndex: Int,
    viewModel: MainViewModel,
    onNavigate: (Int) -> Unit
) {
    AnimatedContent(
        targetState = selectedTabIndex,
        transitionSpec = {
            fadeIn(
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioNoBouncy
                )
            ) togetherWith fadeOut(
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioNoBouncy
                )
            )
        },
        label = "tab_transition"
    ) { targetIndex ->
        when (NavigationTab.values()[targetIndex]) {
            NavigationTab.DASHBOARD -> DashboardScreen(
                viewModel = viewModel,
                onNavigateToGames = { onNavigate(3) },
                onNavigateToXiaomi = { onNavigate(2) }
            )
            NavigationTab.TWEAKS -> GlobalTweaksScreen(viewModel = viewModel)
            NavigationTab.XIAOMI -> XiaomiEngineScreen(viewModel = viewModel)
            NavigationTab.GAMES -> GameLibraryScreen(viewModel = viewModel)
            NavigationTab.TERMINAL -> TerminalLogsScreen(viewModel = viewModel)
        }
    }
}
