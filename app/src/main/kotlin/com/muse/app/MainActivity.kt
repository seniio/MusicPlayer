package com.muse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muse.app.ui.screens.HomeScreen
import com.muse.app.ui.screens.LibraryScreen
import com.muse.app.ui.screens.MiniPlayerCard
import com.muse.app.ui.screens.PlayerScreen
import com.muse.app.ui.screens.ProfileScreen
import com.muse.app.ui.screens.SearchScreen
import com.muse.app.ui.theme.IlvaneBrass
import com.muse.app.ui.theme.MuseTheme
import com.muse.app.ui.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {

    private val playerViewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uiState by playerViewModel.uiState.collectAsState()
            var activeTab by rememberSaveable { mutableStateOf("home") }

            MuseTheme(isLightMode = uiState.isLightMode) {
                if (uiState.activeScreen == "player") {
                    BackHandler {
                        playerViewModel.switchScreen(activeTab)
                    }
                    PlayerScreen(
                        viewModel = playerViewModel,
                        onBack = { playerViewModel.switchScreen(activeTab) }
                    )
                } else {
                    Scaffold(
                        bottomBar = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black)
                            ) {
                                // Floating Mini Player above Bottom Bar
                                if (uiState.isConnected && uiState.title != "No track playing") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        MiniPlayerCard(
                                            title = uiState.title,
                                            artist = uiState.artist,
                                            artworkUrl = uiState.artworkUri,
                                            isPlaying = uiState.isPlaying,
                                            progress = if (uiState.durationMs > 0) uiState.currentPositionMs.toFloat() / uiState.durationMs.toFloat() else 0f,
                                            isLight = uiState.isLightMode,
                                            onCardClick = { playerViewModel.switchScreen("player") },
                                            onPlayPauseClick = { playerViewModel.togglePlayPause() },
                                            onNextClick = { playerViewModel.playNextTrack() }
                                        )
                                    }
                                }

                                // 4-Tab Spotify-Style Bottom Navigation Bar (Solid AMOLED Black)
                                NavigationBar(
                                    containerColor = Color.Black,
                                    tonalElevation = 0.dp,
                                    modifier = Modifier.height(64.dp)
                                ) {
                                    // 1. Home
                                    NavigationBarItem(
                                        selected = activeTab == "home",
                                        onClick = { activeTab = "home" },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.Home,
                                                contentDescription = "Home"
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "Home",
                                                fontSize = 10.sp,
                                                fontWeight = if (activeTab == "home") FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = IlvaneBrass,
                                            selectedTextColor = IlvaneBrass,
                                            indicatorColor = Color.Transparent,
                                            unselectedIconColor = Color(0xFFA1A1AA),
                                            unselectedTextColor = Color(0xFFA1A1AA)
                                        )
                                    )

                                    // 2. Search
                                    NavigationBarItem(
                                        selected = activeTab == "search",
                                        onClick = { activeTab = "search" },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Search"
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "Search",
                                                fontSize = 10.sp,
                                                fontWeight = if (activeTab == "search") FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = IlvaneBrass,
                                            selectedTextColor = IlvaneBrass,
                                            indicatorColor = Color.Transparent,
                                            unselectedIconColor = Color(0xFFA1A1AA),
                                            unselectedTextColor = Color(0xFFA1A1AA)
                                        )
                                    )

                                    // 3. Library (Koleksi)
                                    NavigationBarItem(
                                        selected = activeTab == "library",
                                        onClick = { activeTab = "library" },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.LibraryMusic,
                                                contentDescription = "Koleksi"
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "Koleksi",
                                                fontSize = 10.sp,
                                                fontWeight = if (activeTab == "library") FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = IlvaneBrass,
                                            selectedTextColor = IlvaneBrass,
                                            indicatorColor = Color.Transparent,
                                            unselectedIconColor = Color(0xFFA1A1AA),
                                            unselectedTextColor = Color(0xFFA1A1AA)
                                        )
                                    )

                                    // 4. Profile
                                    NavigationBarItem(
                                        selected = activeTab == "profile",
                                        onClick = { activeTab = "profile" },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Profil"
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "Profil",
                                                fontSize = 10.sp,
                                                fontWeight = if (activeTab == "profile") FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = IlvaneBrass,
                                            selectedTextColor = IlvaneBrass,
                                            indicatorColor = Color.Transparent,
                                            unselectedIconColor = Color(0xFFA1A1AA),
                                            unselectedTextColor = Color(0xFFA1A1AA)
                                        )
                                    )
                                }
                            }
                        }
                    ) { paddingValues ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = paddingValues.calculateBottomPadding())
                        ) {
                            when (activeTab) {
                                "home" -> HomeScreen(
                                    viewModel = playerViewModel,
                                    onOpenPlayer = { playerViewModel.switchScreen("player") },
                                    onNavigateToSearch = { query ->
                                        activeTab = "search"
                                        playerViewModel.onSearchQueryChange(query, immediate = true)
                                    }
                                )
                                "search" -> SearchScreen(
                                    viewModel = playerViewModel,
                                    onOpenPlayer = { playerViewModel.switchScreen("player") },
                                    showEmbeddedMiniPlayer = false
                                )
                                "library" -> LibraryScreen(
                                    viewModel = playerViewModel,
                                    onOpenPlayer = { playerViewModel.switchScreen("player") }
                                )
                                "profile" -> ProfileScreen(
                                    viewModel = playerViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
