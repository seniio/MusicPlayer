package com.muse.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.muse.app.data.model.LyricLine
import com.muse.app.data.model.OnlineTrack
import com.muse.app.ui.theme.MuseAmber
import com.muse.app.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.withTimeout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLight = uiState.isLightMode

    // Hardware / Gesture Back closes lyrics page first if open
    BackHandler(enabled = uiState.isLyricsActive) {
        viewModel.setLyricsViewActive(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isLight) {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF1F5F9), Color(0xFFFFFFFF), Color(0xFFF8FAFC))
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1B1B22), Color(0xFF0F0F13), Color(0xFF0B0B0E))
                    )
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Tapping outside lyrics card minimizes it back to player
                if (uiState.isLyricsActive) {
                    viewModel.setLyricsViewActive(false)
                }
            }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- 1. Top Bar: Back Button, Title & Actions (Lyrics, Queue, Theme) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Kembali ke Cari",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.size(4.dp))
                Column {
                    Text(
                        text = "PLAYING FROM SEARCH",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isConnected) Color(0xFF10B981) else Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.size(5.dp))
                        Text(
                            text = if (uiState.isConnected) "Sovereign Engine" else "Connecting...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Synced Lyrics Toggle Button (Pilar 3)
                IconButton(
                    onClick = { viewModel.toggleLyricsView() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (uiState.isLyricsActive) MuseAmber.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Lirik Lagu Sinkron",
                        tint = if (uiState.isLyricsActive) MuseAmber else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Queue Sheet Toggle Button (Pilar 2)
                IconButton(
                    onClick = { viewModel.setQueueSheetVisible(true) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Antrean Musik",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 3-Dots Settings Dropdown Menu (Theme, Audio Quality, Cache)
                var menuExpanded by remember { mutableStateOf(false) }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Pengaturan Pemutar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier
                            .background(if (isLight) Color.White else Color(0xFF1E1E26))
                            .border(
                                width = 1.dp,
                                color = if (isLight) Color(0xFFE2E8F0) else Color(0xFF33333D),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        // 1. Theme Toggle
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isLight) "Mode Gelap" else "Mode Terang",
                                    color = if (isLight) Color(0xFF0F172A) else Color.White,
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isLight) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = MuseAmber
                                )
                            },
                            onClick = {
                                viewModel.toggleLightMode()
                                menuExpanded = false
                            }
                        )

                        // 2. Audio Quality Info
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Kualitas: Studio (256kbps Opus/AAC)",
                                    color = if (isLight) Color(0xFF0F172A) else Color.White,
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // --- 2. Center: Toggleable Album Art vs Spotify-Grade Synced Lyrics ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLyricsActive) {
                // Pilar 3: Spotify-Grade Real-Time Synced Lyrics View (Tap line to seek, tap outside or swipe down to minimize)
                SyncedLyricsView(
                    lyrics = uiState.lyrics,
                    isLoading = uiState.isLyricsLoading,
                    currentIndex = uiState.currentLyricIndex,
                    isLight = isLight,
                    onSeekTo = { viewModel.seekTo(it) },
                    onDismiss = { viewModel.setLyricsViewActive(false) }
                )
            } else {
                // Resized Album Art + Single-Line Live Synced Lyrics Ticker
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(225.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = if (isLight) Color(0xFFE2E8F0) else Color(0xFF27272A),
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!uiState.artworkUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = uiState.artworkUri,
                                contentDescription = uiState.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "MUSE",
                                    color = MuseAmber,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 6.sp
                                )
                                Text(
                                    text = "SOVEREIGN PLAYER",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Single-Line Live Synced Lyrics (Fixed height prevents album art shifting)
                    SingleLineLiveLyricsStrip(
                        lyrics = uiState.lyrics,
                        isLoading = uiState.isLyricsLoading,
                        currentIndex = uiState.currentLyricIndex,
                        isLight = isLight,
                        onClick = { viewModel.setLyricsViewActive(true) }
                    )
                }
            }
        }

        // --- 3. Track Metadata ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1
                    )
                    Text(
                        text = uiState.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                IconButton(onClick = { viewModel.toggleLikeCurrentTrack() }) {
                    Icon(
                        imageVector = if (uiState.isCurrentTrackLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (uiState.isCurrentTrackLiked) MuseAmber else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 4. Scrubbing Seekbar ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = uiState.currentPositionMs.toFloat(),
                onValueChange = { viewModel.seekTo(it.toLong()) },
                valueRange = 0f..uiState.durationMs.coerceAtLeast(1L).toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = if (isLight) Color(0xFF0F172A) else Color.White,
                    activeTrackColor = if (isLight) Color(0xFF0F172A) else Color.White,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(uiState.currentPositionMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = formatTime(uiState.durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- 5. Main Playback Controls (Shuffle, Prev, Play/Pause, Next, Repeat) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle Button
            IconButton(onClick = { viewModel.toggleShuffleMode() }) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (uiState.isShuffleEnabled) MuseAmber else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(26.dp)
                )
            }

            IconButton(onClick = { viewModel.playPreviousTrack() }) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Track",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(38.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(if (isLight) Color(0xFF0F172A) else Color.White)
                    .clickable { viewModel.togglePlayPause() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = if (isLight) Color.White else Color.Black,
                    modifier = Modifier.size(38.dp)
                )
            }

            IconButton(onClick = { viewModel.playNextTrack() }) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Track",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(38.dp)
                )
            }

            // Repeat Button (OFF -> ALL -> ONE)
            IconButton(onClick = { viewModel.toggleRepeatMode() }) {
                val (repeatIcon, repeatTint) = when (uiState.repeatMode) {
                    Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne to MuseAmber
                    Player.REPEAT_MODE_ALL -> Icons.Default.Repeat to MuseAmber
                    else -> Icons.Default.Repeat to MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                }
                Icon(
                    imageVector = repeatIcon,
                    contentDescription = "Repeat",
                    tint = repeatTint,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }

    // --- 6. Pilar 2: Modal Bottom Sheet for Queue ("Up Next") ---
    if (uiState.isQueueSheetVisible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.setQueueSheetVisible(false) },
            sheetState = sheetState,
            containerColor = if (isLight) Color.White else Color(0xFF18181D)
        ) {
            QueueSheetContent(
                queue = uiState.queue,
                currentIndex = uiState.currentQueueIndex,
                currentTitle = uiState.title,
                currentArtist = uiState.artist,
                currentArtwork = uiState.artworkUri,
                isLight = isLight,
                onTrackClick = { idx ->
                    viewModel.jumpToQueueIndex(idx)
                    viewModel.setQueueSheetVisible(false)
                },
                onRemoveTrack = { idx ->
                    viewModel.removeFromQueue(idx)
                },
                onClose = { viewModel.setQueueSheetVisible(false) }
            )
        }
    }
}

@Composable
fun SingleLineLiveLyricsStrip(
    lyrics: List<LyricLine>,
    isLoading: Boolean,
    currentIndex: Int,
    isLight: Boolean,
    onClick: () -> Unit
) {
    val currentText = when {
        isLoading -> "Memuat lirik sinkron..."
        lyrics.isEmpty() -> "Lirik belum tersedia"
        currentIndex in lyrics.indices -> lyrics[currentIndex].text.ifEmpty { "♪ ♪ ♪" }
        else -> lyrics.firstOrNull()?.text ?: "♪ ♪ ♪"
    }

    // Adaptive White (Dark Mode) or Deep Black (Light Mode)
    val textColor = if (isLight) Color(0xFF0F172A) else Color.White

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = currentText,
            transitionSpec = {
                (slideInVertically(animationSpec = tween(durationMillis = 120)) { height -> height / 2 } +
                    fadeIn(animationSpec = tween(durationMillis = 120)))
                    .togetherWith(
                        slideOutVertically(animationSpec = tween(durationMillis = 120)) { height -> -height / 2 } +
                            fadeOut(animationSpec = tween(durationMillis = 120))
                    )
            },
            label = "LiveLyricLineAnimation",
            modifier = Modifier.fillMaxWidth()
        ) { text ->
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun SyncedLyricsView(
    lyrics: List<LyricLine>,
    isLoading: Boolean,
    currentIndex: Int,
    isLight: Boolean,
    onSeekTo: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    // Nested scroll connection for pull-down / swipe-down to minimize (Spotify standard)
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // available.y > 20f indicates downward overscroll when at the top of lyrics
                if (available.y > 20f) {
                    onDismiss()
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .background(if (isLight) Color(0xFFF1F5F9) else Color(0xFF141418))
            .border(
                width = 1.dp,
                color = if (isLight) Color(0xFFE2E8F0) else Color(0xFF27272A),
                shape = RoundedCornerShape(18.dp)
            )
            .nestedScroll(nestedScrollConnection)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MuseAmber, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Mengambil lirik sinkron...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            lyrics.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onDismiss() },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lirik belum tersedia untuk lagu ini",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            else -> {
                val listState = rememberLazyListState()
                LaunchedEffect(currentIndex) {
                    if (currentIndex >= 0 && currentIndex < lyrics.size) {
                        val target = (currentIndex - 2).coerceAtLeast(0)
                        listState.animateScrollToItem(target)
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Pull-Down Drag Handle Bar (visual affordance & touch target for swipe down)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDismiss() }
                            .padding(top = 2.dp, bottom = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isLight) Color(0xFFCBD5E1) else Color(0xFF3F3F46))
                        )
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(lyrics) { index, line ->
                            val isActive = index == currentIndex
                            Text(
                                text = line.text.ifEmpty { "♪ ♪ ♪" },
                                fontSize = if (isActive) 22.sp else 16.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isActive -> MuseAmber
                                    isLight -> Color(0xFF64748B)
                                    else -> Color(0xFF71717A)
                                },
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSeekTo(line.timeMs) }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QueueSheetContent(
    queue: List<OnlineTrack>,
    currentIndex: Int,
    currentTitle: String,
    currentArtist: String,
    currentArtwork: String?,
    isLight: Boolean,
    onTrackClick: (Int) -> Unit,
    onRemoveTrack: (Int) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Antrean Musik (Up Next)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section: Now Playing
        Text(
            text = "SEDANG DIPUTAR",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MuseAmber,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MuseAmber.copy(alpha = 0.15f))
                .border(1.dp, MuseAmber.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF27272A)),
                contentAlignment = Alignment.Center
            ) {
                if (!currentArtwork.isNullOrEmpty()) {
                    AsyncImage(
                        model = currentArtwork,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = MuseAmber)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1
                )
                Text(
                    text = currentArtist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = "Playing",
                tint = MuseAmber,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Upcoming Tracks in Queue
        Text(
            text = "BERIKUTNYA DI ANTREAN (${(queue.size - currentIndex - 1).coerceAtLeast(0)})",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (queue.isEmpty() || currentIndex >= queue.lastIndex) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak ada lagu lagi di antrean.\nCari lagu baru untuk menambahkannya!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(queue) { idx, track ->
                    if (idx > currentIndex) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { onTrackClick(idx) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${idx - currentIndex}.",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(24.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(6.dp))
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
                                    Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = MuseAmber)
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1
                                )
                                Text(
                                    text = track.artist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }

                            IconButton(onClick = { onRemoveTrack(idx) }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
