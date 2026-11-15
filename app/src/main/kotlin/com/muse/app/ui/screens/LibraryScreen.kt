package com.muse.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.muse.app.data.model.OnlineTrack
import com.muse.app.ui.theme.IlvaneBrass
import com.muse.app.ui.theme.MuseAmber
import com.muse.app.ui.viewmodel.PlayerViewModel

@Composable
fun LibraryScreen(
    viewModel: PlayerViewModel,
    onOpenPlayer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLight = uiState.isLightMode

    // Permission launcher for local storage audio
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setStoragePermissionGranted(isGranted)
    }

    LaunchedEffect(Unit) {
        val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        permissionLauncher.launch(permissionToRequest)
    }

    // Determine tracks based on active filter chip
    val filteredTracks: List<OnlineTrack> = when (uiState.libraryFilter) {
        "LOCAL" -> uiState.localTracks
        "CACHE" -> uiState.cachedTracks
        "FAVORITE" -> uiState.likedTracks
        else -> {
            // "ALL": Combine and deduplicate
            val combined = mutableListOf<OnlineTrack>()
            combined.addAll(uiState.localTracks)
            uiState.cachedTracks.forEach { cached ->
                if (combined.none { it.videoId == cached.videoId }) combined.add(cached)
            }
            uiState.likedTracks.forEach { liked ->
                if (combined.none { it.videoId == liked.videoId }) combined.add(liked)
            }
            combined
        }
    }

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
            // --- Top App Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ILVANE",
                        color = IlvaneBrass,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Koleksi & Audio Lokal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.scanLocalAudio() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Scan Media",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Horizontal Filter Chips ---
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    LibraryFilterChip(
                        label = "Semua (${uiState.localTracks.size + uiState.cachedTracks.size})",
                        isSelected = uiState.libraryFilter == "ALL",
                        onClick = { viewModel.setLibraryFilter("ALL") }
                    )
                }
                item {
                    LibraryFilterChip(
                        label = "File HP (${uiState.localTracks.size})",
                        isSelected = uiState.libraryFilter == "LOCAL",
                        onClick = { viewModel.setLibraryFilter("LOCAL") }
                    )
                }
                item {
                    LibraryFilterChip(
                        label = "Cache (${uiState.cachedTracks.size})",
                        isSelected = uiState.libraryFilter == "CACHE",
                        onClick = { viewModel.setLibraryFilter("CACHE") }
                    )
                }
                item {
                    LibraryFilterChip(
                        label = "Favorit (${uiState.likedTracks.size})",
                        isSelected = uiState.libraryFilter == "FAVORITE",
                        onClick = { viewModel.setLibraryFilter("FAVORITE") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Permission Required Banner ---
            if (!uiState.hasStoragePermission && uiState.libraryFilter == "LOCAL") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, Color(0xFF3F3F46), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = IlvaneBrass,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Akses Musik Lokal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Berikan izin baca agar ILVANE dapat memindai koleksi MP3/FLAC di memori perangkat Anda.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    Manifest.permission.READ_MEDIA_AUDIO
                                } else {
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                }
                                permissionLauncher.launch(perm)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IlvaneBrass)
                        ) {
                            Text(text = "Izinkan Akses", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- Track List ---
            if (filteredTracks.isEmpty() && uiState.hasStoragePermission) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color(0xFF71717A),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when (uiState.libraryFilter) {
                                "LOCAL" -> "Tidak ada file musik di memori HP"
                                "CACHE" -> "Belum ada lagu yang tersimpan di cache"
                                "FAVORITE" -> "Belum ada lagu favorit (ketuk ikon hati)"
                                else -> "Koleksi masih kosong"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTracks, key = { it.videoId }) { track ->
                        val isPlayingThis = uiState.title == track.title && uiState.artist == track.artist
                        LibraryTrackRow(
                            track = track,
                            isPlaying = isPlayingThis,
                            onClick = {
                                viewModel.playOnlineTrack(track, queue = filteredTracks)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) IlvaneBrass else MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = if (isSelected) IlvaneBrass else Color(0xFF27272A),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun LibraryTrackRow(
    track: OnlineTrack,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val isLocal = track.videoId.startsWith("local_")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artwork
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF27272A)),
                contentAlignment = Alignment.Center
            ) {
                if (track.thumbnailUrl.isNotBlank()) {
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
                        tint = if (isPlaying) IlvaneBrass else Color(0xFFA1A1AA)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlaying) IlvaneBrass else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Badge: File HP or Cache
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isLocal) Color(0xFF27272A) else Color(0xFF1E293B))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (isLocal) "File HP" else "Cache",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (isLocal) IlvaneBrass else Color(0xFF38BDF8)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }

        // Duration or playing indicator
        if (track.durationText.isNotBlank()) {
            Text(
                text = track.durationText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
