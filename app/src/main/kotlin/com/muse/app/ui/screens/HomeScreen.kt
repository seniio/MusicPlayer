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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.muse.app.data.model.OnlineTrack
import com.muse.app.data.repository.CuratedPlaylist
import com.muse.app.data.repository.CuratedPlaylistsRepository
import com.muse.app.ui.theme.IlvaneBrass
import com.muse.app.ui.viewmodel.PlayerViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: PlayerViewModel,
    onOpenPlayer: () -> Unit,
    onNavigateToSearch: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPlaylist by remember { mutableStateOf<CuratedPlaylist?>(null) }
    var activeFilter by remember { mutableStateOf("Semua") }

    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val dayName = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Senin"
        Calendar.TUESDAY -> "Selasa"
        Calendar.WEDNESDAY -> "Rabu"
        Calendar.THURSDAY -> "Kamis"
        Calendar.FRIDAY -> "Jumat"
        Calendar.SATURDAY -> "Sabtu"
        else -> "Minggu"
    }
    val timePeriod = when (hour) {
        in 4..11 -> "pagi"
        in 12..14 -> "siang"
        in 15..18 -> "sore"
        else -> "malam"
    }
    val dynamicSectionTitle = "Pilihan lagu buat $dayName $timePeriod"

    // Live Auto-Updated Playlists with Stale-While-Revalidate Fallback
    val top50IndoTracks = if (uiState.liveTop50Indo.isNotEmpty()) uiState.liveTop50Indo else CuratedPlaylistsRepository.TOP_50_INDONESIA.tracks
    val top50GlobalTracks = if (uiState.liveTop50Global.isNotEmpty()) uiState.liveTop50Global else CuratedPlaylistsRepository.TOP_50_GLOBAL.tracks

    val dynamicTop50Indo = CuratedPlaylist(
        id = "top_50_indo",
        title = "Top 50 Indonesia",
        subtitle = if (uiState.chartsLastUpdatedText.isNotEmpty()) "Tangga Lagu Live • ${uiState.chartsLastUpdatedText}" else "50 Lagu Terpopuler & Paling Banyak Didengarkan di Indonesia",
        trackCountText = "${top50IndoTracks.size} Lagu",
        badge = "LIVE INDO",
        tracks = top50IndoTracks
    )

    val dynamicTop50Global = CuratedPlaylist(
        id = "top_50_global",
        title = "Top 50 Global",
        subtitle = if (uiState.chartsLastUpdatedText.isNotEmpty()) "Tangga Lagu Live • ${uiState.chartsLastUpdatedText}" else "50 Lagu Terbesar & Paling Banyak Diputar di Seluruh Dunia",
        trackCountText = "${top50GlobalTracks.size} Lagu",
        badge = "LIVE GLOBAL",
        tracks = top50GlobalTracks
    )

    // Dynamic user playlists
    val recentlyPlayedPlaylist = CuratedPlaylist(
        id = "recently_played",
        title = "Baru Didengarkan",
        subtitle = "Riwayat 12 Lagu Terakhir yang Anda Putar (FIFO)",
        trackCountText = "${uiState.cachedTracks.size} Lagu",
        badge = "RIWAYAT",
        tracks = uiState.cachedTracks
    )

    val likedPlaylist = CuratedPlaylist(
        id = "liked_songs",
        title = "Lagu yang Disukai",
        subtitle = "Koleksi Lagu Favorit Anda",
        trackCountText = "${uiState.likedTracks.size} Lagu",
        badge = "FAVORIT",
        tracks = uiState.likedTracks
    )

    val localFilesPlaylist = CuratedPlaylist(
        id = "local_files",
        title = "Penyimpanan HP",
        subtitle = "Koleksi Lagu MP3/FLAC di Memori Ponsel",
        trackCountText = "${uiState.localTracks.size} Lagu",
        badge = "LOKAL",
        tracks = uiState.localTracks
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (selectedPlaylist != null) {
            // Keep selectedPlaylist updated with live state if viewing live charts
            val activePlaylist = when (selectedPlaylist!!.id) {
                "top_50_indo" -> dynamicTop50Indo
                "top_50_global" -> dynamicTop50Global
                else -> selectedPlaylist!!
            }

            // --- Playlist Detail View ---
            CuratedPlaylistDetailView(
                playlist = activePlaylist,
                activeTrackTitle = uiState.title,
                isPlaying = uiState.isPlaying,
                isUpdating = uiState.isChartsUpdating,
                lastUpdatedText = if (activePlaylist.id == "top_50_indo" || activePlaylist.id == "top_50_global") {
                    uiState.chartsLastUpdatedText.ifBlank { null }
                } else null,
                onRefresh = if (activePlaylist.id == "top_50_indo" || activePlaylist.id == "top_50_global") {
                    { viewModel.refreshLiveCharts(force = true) }
                } else null,
                onBack = { selectedPlaylist = null },
                onPlayTrack = { track, queue ->
                    viewModel.playOnlineTrack(track, queue = queue)
                },
                onPlayAll = { queue ->
                    if (queue.isNotEmpty()) {
                        viewModel.playOnlineTrack(queue.first(), queue = queue)
                    }
                },
                onShuffleAll = { queue ->
                    if (queue.isNotEmpty()) {
                        val shuffled = queue.shuffled()
                        viewModel.playOnlineTrack(shuffled.first(), queue = shuffled)
                    }
                }
            )
        } else {
            // --- Spotify 2-Column Clean Grid Layout ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Top App Bar with Avatar & Quick Pill Chips (Spotify signature)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Profile Avatar with notification badge
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

                        // Filter Pills (Semua, Musik, Koleksi)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Semua", "Musik", "Koleksi").forEach { chip ->
                                val isSelected = activeFilter == chip
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) IlvaneBrass else Color(0xFF27272A))
                                        .clickable { activeFilter = chip }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = chip,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // --- 2-Column Compact Grid (Signature Spotify 8-Card Grid) ---
                item {
                    val gridItems = listOf(
                        CompactGridItem("Top 50 Indo", Color(0xFFE11D48), Icons.Default.Whatshot) {
                            selectedPlaylist = dynamicTop50Indo
                        },
                        CompactGridItem("Top 50 Global", Color(0xFFD97706), Icons.Default.Public) {
                            selectedPlaylist = dynamicTop50Global
                        },
                        CompactGridItem("Rilis Terbaru", Color(0xFF0284C7), Icons.Default.MusicNote) {
                            selectedPlaylist = CuratedPlaylistsRepository.NEW_RELEASES_PLAYLIST
                        },
                        CompactGridItem("Baru Diputar", Color(0xFF6366F1), Icons.Default.History) {
                            selectedPlaylist = recentlyPlayedPlaylist
                        },
                        CompactGridItem("Daily Mix 1", Color(0xFF8B5CF6), Icons.Default.GraphicEq) {
                            selectedPlaylist = CuratedPlaylistsRepository.DAILY_MIX
                        },
                        CompactGridItem("Akustik Santai", Color(0xFF0D9488), Icons.Default.Radio) {
                            selectedPlaylist = CuratedPlaylistsRepository.AKUSTIK_SANTAI
                        },
                        CompactGridItem("Lagu Disukai", Color(0xFFEC4899), Icons.Default.Favorite) {
                            selectedPlaylist = likedPlaylist
                        },
                        CompactGridItem("Penyimpanan HP", Color(0xFF10B981), Icons.Default.Folder) {
                            selectedPlaylist = localFilesPlaylist
                        }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        gridItems.chunked(2).forEach { rowPair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowPair.forEach { item ->
                                    SpotifyCompactCard(
                                        title = item.title,
                                        accentColor = item.accentColor,
                                        icon = item.icon,
                                        modifier = Modifier.weight(1f),
                                        onClick = item.onClick
                                    )
                                }
                            }
                        }
                    }
                }

                // --- Section 1: Mix teratasmu (Signature Spotify Section) ---
                item {
                    Text(
                        text = "Mix teratasmu",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            SpotifyCoverCard(
                                title = "Top 50 Indonesia",
                                subtitle = "Tulus, Rizky Febian, Sal Priadi, Nadhif, Mahalini",
                                gradientColors = listOf(Color(0xFFE11D48), Color(0xFF881337)),
                                icon = Icons.Default.Whatshot,
                                badgeLabel = "Live Indo",
                                onClick = { selectedPlaylist = dynamicTop50Indo }
                            )
                        }
                        item {
                            SpotifyCoverCard(
                                title = "Top 50 Global",
                                subtitle = "Ella Langley, Tate McRae, Bruno Mars, Taylor Swift",
                                gradientColors = listOf(Color(0xFFD97706), Color(0xFF78350F)),
                                icon = Icons.Default.Public,
                                badgeLabel = "Live Global",
                                onClick = { selectedPlaylist = dynamicTop50Global }
                            )
                        }
                        item {
                            SpotifyCoverCard(
                                title = "Daily Mix 1",
                                subtitle = "Coldplay, The Weeknd, Tenxi, Hindia, Sheila On 7",
                                gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF4C1D95)),
                                icon = Icons.Default.GraphicEq,
                                badgeLabel = "Daily Mix",
                                onClick = { selectedPlaylist = CuratedPlaylistsRepository.DAILY_MIX }
                            )
                        }
                        item {
                            SpotifyCoverCard(
                                title = "Akustik Santai",
                                subtitle = "Pamungkas, Fiersa Besari, Feby Putri, Tulus",
                                gradientColors = listOf(Color(0xFF0D9488), Color(0xFF134E4A)),
                                icon = Icons.Default.Radio,
                                badgeLabel = "Acoustic Mix",
                                onClick = { selectedPlaylist = CuratedPlaylistsRepository.AKUSTIK_SANTAI }
                            )
                        }
                    }
                }

                // --- Section 2: Pilihan lagu buat [Waktu] (Dynamic Greeting Section) ---
                item {
                    Text(
                        text = dynamicSectionTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            SpotifyCoverCard(
                                title = "Rilis Terbaru 2026",
                                subtitle = "Tulus, Bruno Mars, eńau, Taylor Swift, Perunggu",
                                gradientColors = listOf(Color(0xFF0284C7), Color(0xFF0C4A6E)),
                                icon = Icons.Default.MusicNote,
                                badgeLabel = "Rilis 2026",
                                onClick = { selectedPlaylist = CuratedPlaylistsRepository.NEW_RELEASES_PLAYLIST }
                            )
                        }
                        item {
                            SpotifyCoverCard(
                                title = "Baru Didengarkan",
                                subtitle = "${uiState.cachedTracks.size} lagu terakhir diputar (FIFO)",
                                gradientColors = listOf(Color(0xFF6366F1), Color(0xFF312E81)),
                                icon = Icons.Default.History,
                                badgeLabel = "Riwayat",
                                onClick = { selectedPlaylist = recentlyPlayedPlaylist }
                            )
                        }
                        item {
                            SpotifyCoverCard(
                                title = "Lagu yang Disukai",
                                subtitle = "${uiState.likedTracks.size} lagu favorit pilihan hatimu",
                                gradientColors = listOf(Color(0xFFEC4899), Color(0xFF831843)),
                                icon = Icons.Default.Favorite,
                                badgeLabel = "Favorit",
                                onClick = { selectedPlaylist = likedPlaylist }
                            )
                        }
                        item {
                            SpotifyCoverCard(
                                title = "File HP (Lokal)",
                                subtitle = "${uiState.localTracks.size} lagu dari penyimpanan ponsel",
                                gradientColors = listOf(Color(0xFF10B981), Color(0xFF064E3B)),
                                icon = Icons.Default.Folder,
                                badgeLabel = "Offline",
                                onClick = { selectedPlaylist = localFilesPlaylist }
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class CompactGridItem(
    val title: String,
    val accentColor: Color,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun SpotifyCompactCard(
    title: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Icon Artwork Box
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Brush.linearGradient(listOf(accentColor, accentColor.copy(alpha = 0.6f)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
    }
}

@Composable
fun SpotifyCoverCard(
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    icon: ImageVector,
    badgeLabel: String? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(148.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(148.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.verticalGradient(gradientColors)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(52.dp)
            )

            if (!badgeLabel.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFA1A1AA),
            maxLines = 2,
            lineHeight = 14.sp
        )
    }
}

@Composable
fun CuratedPlaylistDetailView(
    playlist: CuratedPlaylist,
    activeTrackTitle: String,
    isPlaying: Boolean,
    isUpdating: Boolean = false,
    lastUpdatedText: String? = null,
    onRefresh: (() -> Unit)? = null,
    onBack: () -> Unit,
    onPlayTrack: (OnlineTrack, List<OnlineTrack>) -> Unit,
    onPlayAll: (List<OnlineTrack>) -> Unit,
    onShuffleAll: (List<OnlineTrack>) -> Unit
) {
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar Back
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = playlist.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            if (onRefresh != null) {
                IconButton(
                    onClick = { if (!isUpdating) onRefresh() }
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = IlvaneBrass
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Perbarui Chart",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = playlist.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = playlist.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${playlist.tracks.size} Lagu • Audio Asli Client-Side",
                        style = MaterialTheme.typography.labelSmall,
                        color = IlvaneBrass,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (lastUpdatedText != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "⚡ $lastUpdatedText",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onPlayAll(playlist.tracks) },
                            colors = ButtonDefaults.buttonColors(containerColor = IlvaneBrass),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Putar Semua", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onShuffleAll(playlist.tracks) },
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F46)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Shuffle, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Acak Lagu", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (playlist.tracks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada lagu di koleksi ini",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                itemsIndexed(playlist.tracks) { index, track ->
                    val isThisPlaying = activeTrackTitle == track.title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isThisPlaying) Color(0xFF1E1E26) else MaterialTheme.colorScheme.surface)
                            .clickable { onPlayTrack(track, playlist.tracks) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isThisPlaying) IlvaneBrass else Color(0xFF71717A),
                            modifier = Modifier.width(28.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isThisPlaying) IlvaneBrass else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = track.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }

                        if (isThisPlaying) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Playing",
                                tint = IlvaneBrass,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
