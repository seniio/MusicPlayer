package com.muse.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.muse.app.data.model.OnlineTrack
import com.muse.app.ui.theme.IlvaneBrass
import com.muse.app.ui.theme.MuseAmber
import com.muse.app.ui.viewmodel.PlayerViewModel

data class BrowseCategory(
    val title: String,
    val query: String,
    val color: Color
)

data class ExploreReel(
    val hashtag: String,
    val query: String,
    val gradientColors: List<Color>
)

@Composable
fun SearchScreen(
    viewModel: PlayerViewModel,
    onOpenPlayer: () -> Unit,
    showEmbeddedMiniPlayer: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLight = uiState.isLightMode
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        viewModel.onSearchQueryChange("")
        focusManager.clearFocus()
    }

    val topBrowseCategories = listOf(
        BrowseCategory("Musik", "Top Pop Hits 2026", Color(0xFFE11D48)),
        BrowseCategory("Podcast", "Podcast Indonesia Terpopuler", Color(0xFF147259)),
        BrowseCategory("Acara Langsung", "Live Acoustic Concert", Color(0xFF7C3AED)),
        BrowseCategory("K-Pop ON! Hub", "Top K-Pop", Color(0xFF2563EB)),
        BrowseCategory("Dibuat Untuk Kamu", "Lagu Rekomendasi Pilihan", Color(0xFF9D174D)),
        BrowseCategory("Rilis Baru", "Lagu Baru Rilis 2026", Color(0xFFD97706))
    )

    val exploreReels = listOf(
        ExploreReel("#secret admirer", "Lagu Galau Romantis Rahasia", listOf(Color(0xFF4C1D95), Color(0xFF1E1B4B))),
        ExploreReel("#5th gen k-pop", "5th Gen K-Pop", listOf(Color(0xFF0369A1), Color(0xFF082F49))),
        ExploreReel("#acoustic vibes", "Lagu Akustik Santai", listOf(Color(0xFF9A3412), Color(0xFF431407))),
        ExploreReel("#indie folk", "Indie Folk Indonesia", listOf(Color(0xFF065F46), Color(0xFF022C22))),
        ExploreReel("#mic drop", "Hip Hop Rap Energetic", listOf(Color(0xFF831843), Color(0xFF500724)))
    )

    val moreBrowseCategories = listOf(
        BrowseCategory("Rock & Alternatif", "Lagu Rock Pop Alternatif", Color(0xFFEA580C)),
        BrowseCategory("R&B & Hip-Hop", "Top R&B Soul", Color(0xFFDB2777)),
        BrowseCategory("Indie & Folk", "Lagu Indie Pop Indonesia", Color(0xFF059669)),
        BrowseCategory("Nostalgia 90-an", "Lagu Nostalgia Indonesia 90an 2000an", Color(0xFF4F46E5)),
        BrowseCategory("Dangdut & Koplo", "Dangdut Koplo Populer", Color(0xFFCA8A04)),
        BrowseCategory("Anime & Gaming", "Anime OST Gaming Music", Color(0xFF0284C7))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // --- Top App Bar (Avatar with badge, "Cari", Camera Icon) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.size(34.dp)) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF27272A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = IlvaneBrass,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE11D48))
                                .align(Alignment.TopEnd)
                        )
                    }

                    Text(
                        text = "Cari",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = { /* Visual search */ }) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Scan",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- Spotify-Style High-Contrast White Search Bar ---
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { 
                    viewModel.onSearchQueryChange(it)
                    if (!isSearchActive) isSearchActive = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            isSearchActive = true
                        }
                    },
                placeholder = {
                    Text(
                        text = "Apa yang ingin kamu dengarkan?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280)
                    )
                },
                leadingIcon = {
                    if (isSearchActive) {
                        IconButton(onClick = {
                            isSearchActive = false
                            viewModel.onSearchQueryChange("")
                            focusManager.clearFocus()
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Kembali",
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF1F2937),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Color.Black)
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { 
                        viewModel.onSearchQueryChange(uiState.searchQuery, immediate = true)
                        focusManager.clearFocus()
                    }
                ),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Searching Indicator
            if (uiState.isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = IlvaneBrass,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Mencari audio...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. Active Search Mode: User is preparing to search (isSearchActive && searchQuery.isEmpty())
            // History ONLY appears here when user is actually searching, NEVER by default!
            else if (isSearchActive && uiState.searchQuery.isEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    if (uiState.searchHistory.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pencarian terbaru",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Hapus semua",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFA1A1AA),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { viewModel.clearSearchHistory() }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        items(uiState.searchHistory) { query ->
                            SearchHistoryRow(
                                query = query,
                                onSelect = {
                                    viewModel.onSearchQueryChange(query, immediate = true)
                                },
                                onRemove = {
                                    viewModel.removeSearchHistory(query)
                                }
                            )
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Ketik judul lagu, nama artis, atau album",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 3. Search Results Mode: User typed a query and search has results
            else if (uiState.searchQuery.isNotEmpty() || uiState.searchResults.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(uiState.searchResults, key = { it.videoId }) { track ->
                        SongSearchItem(
                            track = track,
                            isLight = isLight,
                            onPlayClick = {
                                viewModel.playTrackWithVibeRadio(track)
                                onOpenPlayer()
                            }
                        )
                    }
                }
            }

            // 4. Default Clean Browse Mode (!isSearchActive && searchQuery.isEmpty())
            // Completely clean: NO search history, ONLY categories, reels, and browse cards!
            else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    // Top 6 Categories (2 Columns)
                    items(topBrowseCategories.chunked(2)) { rowPair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowPair.forEach { category ->
                                SpotifyCategoryCard(
                                    category = category,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        isSearchActive = true
                                        viewModel.onSearchQueryChange(category.query, immediate = true)
                                    }
                                )
                            }
                        }
                    }

                    // Section: "Temukan sesuatu yang lain" (Reels / Vertical Cards)
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Temukan sesuatu yang lain",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(exploreReels) { reel ->
                                ExploreReelCard(
                                    reel = reel,
                                    onClick = {
                                        isSearchActive = true
                                        viewModel.onSearchQueryChange(reel.query, immediate = true)
                                    }
                                )
                            }
                        }
                    }

                    // Section: "Jelajahi Semua"
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Jelajahi Semua",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    // More Categories (2 Columns)
                    items(moreBrowseCategories.chunked(2)) { rowPair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowPair.forEach { category ->
                                SpotifyCategoryCard(
                                    category = category,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        isSearchActive = true
                                        viewModel.onSearchQueryChange(category.query, immediate = true)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Persistent Mini Player (Floating at bottom if embedded) ---
        if (showEmbeddedMiniPlayer && uiState.isConnected && uiState.title != "No track playing") {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                MiniPlayerCard(
                    title = uiState.title,
                    artist = uiState.artist,
                    artworkUrl = uiState.artworkUri,
                    isPlaying = uiState.isPlaying,
                    progress = if (uiState.durationMs > 0) uiState.currentPositionMs.toFloat() / uiState.durationMs.toFloat() else 0f,
                    isLight = isLight,
                    onCardClick = onOpenPlayer,
                    onPlayPauseClick = { viewModel.togglePlayPause() },
                    onNextClick = { viewModel.playNextTrack() }
                )
            }
        }
    }
}

@Composable
fun SpotifyCategoryCard(
    category: BrowseCategory,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(82.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(category.color)
            .clickable { onClick() }
    ) {
        Text(
            text = category.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            lineHeight = 18.sp,
            maxLines = 2
        )

        // Angled decorative tile in the bottom-right corner (signature Spotify look)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 10.dp, y = 8.dp)
                .rotate(24f)
                .size(52.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun ExploreReelCard(
    reel: ExploreReel,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(118.dp)
            .height(176.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.verticalGradient(reel.gradientColors))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.GraphicEq,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.35f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(18.dp)
        )

        Text(
            text = reel.hashtag,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Composable
fun SongSearchItem(
    track: OnlineTrack,
    isLight: Boolean,
    onPlayClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = Color(0xFF27272A),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onPlayClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF27272A)),
                contentAlignment = Alignment.Center
            ) {
                if (track.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = track.thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = IlvaneBrass
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA1A1AA),
                    maxLines = 1
                )
            }
        }

        IconButton(onClick = onPlayClick) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = IlvaneBrass
            )
        }
    }
}

@Composable
fun MiniPlayerCard(
    title: String,
    artist: String,
    artworkUrl: String?,
    isPlaying: Boolean,
    progress: Float,
    isLight: Boolean,
    onCardClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1E1E24))
            .border(
                width = 1.dp,
                color = Color(0xFF27272A),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onCardClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF27272A)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!artworkUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = artworkUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = "$artist • Smart Cache",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA1A1AA),
                        maxLines = 1
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPlayPauseClick) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(onClick = onNextClick) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // Micro Progress Bar at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color(0xFF27272A))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                    .height(3.dp)
                    .background(IlvaneBrass)
            )
        }
    }
}

@Composable
fun SearchHistoryRow(
    query: String,
    onSelect: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = Color(0xFFA1A1AA),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = query,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1
            )
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Hapus",
                tint = Color(0xFFA1A1AA),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
