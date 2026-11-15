package com.muse.app.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.muse.app.R
import com.muse.app.data.model.LyricLine
import com.muse.app.data.model.OnlineTrack
import com.muse.app.data.repository.LocalAudioRepository
import com.muse.app.data.repository.LyricsRepository
import com.muse.app.data.repository.SessionRepository
import com.muse.app.data.repository.YouTubeMusicRepository
import com.muse.app.service.PlaybackService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
    val isConnected: Boolean = false,
    val isPlaying: Boolean = false,
    val title: String = "No track playing",
    val artist: String = "MUSE Audio Engine",
    val artworkUri: String? = null,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isLightMode: Boolean = false,
    val playbackStatus: String = "Menghubungkan ke Audio Service...",
    val isError: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<OnlineTrack> = emptyList(),
    val isSearching: Boolean = false,
    val activeScreen: String = "search", // "search" or "player"
    val queue: List<OnlineTrack> = emptyList(),
    val currentQueueIndex: Int = -1,
    val isQueueSheetVisible: Boolean = false,
    val lyrics: List<LyricLine> = emptyList(),
    val isLyricsLoading: Boolean = false,
    val currentLyricIndex: Int = -1,
    val isLyricsActive: Boolean = false,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: Int = 0, // 0 = Player.REPEAT_MODE_OFF, 1 = ONE, 2 = ALL
    val localTracks: List<OnlineTrack> = emptyList(),
    val likedTracks: List<OnlineTrack> = emptyList(),
    val cachedTracks: List<OnlineTrack> = emptyList(),
    val isCurrentTrackLiked: Boolean = false,
    val libraryFilter: String = "ALL", // "ALL", "LOCAL", "CACHE", "FAVORITE"
    val hasStoragePermission: Boolean = false,
    val searchHistory: List<String> = emptyList(),
    val liveTop50Indo: List<OnlineTrack> = emptyList(),
    val liveTop50Global: List<OnlineTrack> = emptyList(),
    val isChartsUpdating: Boolean = false,
    val chartsLastUpdatedText: String = ""
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var lastErrorMessage: String? = null

    private val ytmRepository = YouTubeMusicRepository()
    private val lyricsRepository = LyricsRepository()
    private val sessionRepository = SessionRepository(application)
    private val localAudioRepository = LocalAudioRepository(application)

    private var searchJob: Job? = null
    private var lyricsJob: Job? = null
    private var prefetchJob: Job? = null

    private var currentQueue: List<OnlineTrack> = emptyList()
    private var currentTrackIndex: Int = -1
    private var currentTrackVideoId: String? = null
    private var currentStreamUrl: String? = null
    private var pendingSeekPositionMs: Long = 0L
    private var progressTickCounter = 0

    init {
        loadLibraryData()
        restoreSavedSession()
        initializeMediaController()
        startProgressTracker()

        // Auto-update charts if cache is empty or older than 12 hours
        val now = System.currentTimeMillis()
        val lastUpdated = sessionRepository.getLiveChartLastUpdated(SessionRepository.KEY_CHART_INDO)
        if (lastUpdated == 0L || (now - lastUpdated) > 12 * 60 * 60 * 1000L) {
            refreshLiveCharts(force = false)
        }
    }

    private fun loadLibraryData() {
        val cachedIndo = sessionRepository.getLiveChart(SessionRepository.KEY_CHART_INDO)
        val cachedGlobal = sessionRepository.getLiveChart(SessionRepository.KEY_CHART_GLOBAL)
        val lastUpdated = sessionRepository.getLiveChartLastUpdated(SessionRepository.KEY_CHART_INDO)
        val lastUpdatedText = if (lastUpdated > 0L) {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale("id", "ID"))
            "Diperbarui otomatis ${sdf.format(java.util.Date(lastUpdated))}"
        } else ""

        _uiState.update {
            it.copy(
                likedTracks = sessionRepository.getLikedTracks(),
                cachedTracks = sessionRepository.getCachedTracks(),
                searchHistory = sessionRepository.getSearchHistory(),
                liveTop50Indo = cachedIndo,
                liveTop50Global = cachedGlobal,
                chartsLastUpdatedText = lastUpdatedText
            )
        }
    }

    fun refreshLiveCharts(force: Boolean = true) {
        if (_uiState.value.isChartsUpdating) return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isChartsUpdating = true) }
            try {
                Log.d("MUSE_PLAYER", "Fetching live real-time charts from YouTube Music...")
                val indoResult = ytmRepository.fetchLiveChart("ID")
                val globalResult = ytmRepository.fetchLiveChart("US")

                val freshIndo = indoResult.getOrNull()
                val freshGlobal = globalResult.getOrNull()

                if (!freshIndo.isNullOrEmpty()) {
                    sessionRepository.saveLiveChart(SessionRepository.KEY_CHART_INDO, freshIndo)
                }
                if (!freshGlobal.isNullOrEmpty()) {
                    sessionRepository.saveLiveChart(SessionRepository.KEY_CHART_GLOBAL, freshGlobal)
                }

                val updatedIndo = freshIndo ?: sessionRepository.getLiveChart(SessionRepository.KEY_CHART_INDO)
                val updatedGlobal = freshGlobal ?: sessionRepository.getLiveChart(SessionRepository.KEY_CHART_GLOBAL)

                val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale("id", "ID"))
                val dateStr = sdf.format(java.util.Date())

                _uiState.update {
                    it.copy(
                        liveTop50Indo = updatedIndo,
                        liveTop50Global = updatedGlobal,
                        isChartsUpdating = false,
                        chartsLastUpdatedText = "Diperbarui otomatis $dateStr"
                    )
                }
                Log.d("MUSE_PLAYER", "Live charts auto-update complete! Indo=${updatedIndo.size}, Global=${updatedGlobal.size}")
            } catch (e: Exception) {
                Log.e("MUSE_PLAYER", "Failed to auto-update charts: ${e.message}")
                _uiState.update { it.copy(isChartsUpdating = false) }
            }
        }
    }

    private fun restoreSavedSession() {
        val session = sessionRepository.loadSession() ?: return
        Log.d("MUSE_PLAYER", "Restoring saved session: ${session.title} - ${session.artist} at ${session.positionMs} ms")
        currentQueue = session.queue
        currentTrackIndex = session.queueIndex
        currentTrackVideoId = session.videoId
        currentStreamUrl = session.streamUrl
        pendingSeekPositionMs = session.positionMs

        _uiState.update {
            it.copy(
                title = session.title ?: "No track playing",
                artist = session.artist ?: "MUSE Audio Engine",
                artworkUri = session.artworkUrl,
                currentPositionMs = session.positionMs,
                durationMs = session.durationMs,
                queue = session.queue,
                currentQueueIndex = session.queueIndex,
                isShuffleEnabled = session.isShuffle,
                repeatMode = session.repeatMode,
                isLightMode = session.isLightMode,
                playbackStatus = "Lanjut mendengarkan (Siap diputar)"
            )
        }

        // Seed stream cache with saved URL for instant 0ms playback upon resume
        if (!session.videoId.isNullOrEmpty() && !session.streamUrl.isNullOrEmpty()) {
            ytmRepository.seedStreamCache(session.videoId, session.streamUrl)
        }

        // Prefetch lyrics & audio stream proactively in background
        if (!session.title.isNullOrEmpty()) {
            fetchLyrics(session.title, session.artist ?: "")
        }
        if (!session.videoId.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                ytmRepository.prefetchStream(session.videoId, session.title ?: "", session.artist ?: "")
            }
        }
    }

    private fun initializeMediaController() {
        val context = getApplication<Application>()
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                val controller = controllerFuture?.get()
                mediaController = controller
                
                // Sync repeat & shuffle modes to controller
                controller?.repeatMode = _uiState.value.repeatMode
                controller?.shuffleModeEnabled = _uiState.value.isShuffleEnabled

                controller?.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        Log.d("MUSE_PLAYER", "onIsPlayingChanged: $isPlaying")
                        _uiState.update { 
                            it.copy(
                                isPlaying = isPlaying,
                                playbackStatus = if (isPlaying) "Memutar audio..." else "Dijeda (Paused)",
                                isError = false
                            ) 
                        }
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        val meta = mediaItem?.mediaMetadata
                        Log.d("MUSE_PLAYER", "onMediaItemTransition: title=${meta?.title}")
                        _uiState.update {
                            it.copy(
                                title = meta?.title?.toString() ?: "Unknown Title",
                                artist = meta?.artist?.toString() ?: "Unknown Artist",
                                artworkUri = meta?.artworkUri?.toString()
                            )
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        val duration = controller.duration.coerceAtLeast(0L)
                        val statusText: String
                        val isErr: Boolean

                        if (lastErrorMessage != null) {
                            statusText = lastErrorMessage!!
                            isErr = true
                        } else {
                            isErr = false
                            statusText = when (playbackState) {
                                Player.STATE_IDLE -> "Idle (Siap)"
                                Player.STATE_BUFFERING -> "Buffering audio (Memuat cepat)..."
                                Player.STATE_READY -> if (controller.isPlaying) "Memutar audio..." else "Siap diputar"
                                Player.STATE_ENDED -> {
                                    if (_uiState.value.repeatMode == Player.REPEAT_MODE_ONE) {
                                        seekTo(0)
                                        mediaController?.play()
                                        "Mengulang lagu..."
                                    } else {
                                        playNextTrack()
                                        "Memutar lagu berikutnya di antrean..."
                                    }
                                }
                                else -> "Status: $playbackState"
                            }
                        }

                        Log.d("MUSE_PLAYER", "onPlaybackStateChanged: $statusText (State: $playbackState)")
                        _uiState.update { 
                            it.copy(
                                durationMs = duration,
                                playbackStatus = statusText,
                                isError = isErr
                            ) 
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        val detailedError = "Error [${error.errorCodeName}]: ${error.message ?: "Playback error"}"
                        Log.e("MUSE_PLAYER", detailedError, error)
                        lastErrorMessage = detailedError
                        _uiState.update {
                            it.copy(
                                isPlaying = false,
                                playbackStatus = detailedError,
                                isError = true
                            )
                        }
                    }
                })

                _uiState.update {
                    it.copy(
                        isConnected = true,
                        isPlaying = controller?.isPlaying == true,
                        playbackStatus = if (_uiState.value.title != "No track playing") "Siap diputar" else "Audio Service Siap",
                        isError = false
                    )
                }
                Log.d("MUSE_PLAYER", "MediaController connected successfully.")
            } catch (e: Exception) {
                val err = "Gagal menghubungkan controller: ${e.message}"
                Log.e("MUSE_PLAYER", err, e)
                _uiState.update { it.copy(playbackStatus = err, isError = true) }
            }
        }, MoreExecutors.directExecutor())
    }

    private fun startProgressTracker() {
        viewModelScope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        val pos = controller.currentPosition.coerceAtLeast(0L)
                        val dur = controller.duration.coerceAtLeast(0L)
                        val lyrics = _uiState.value.lyrics
                        // 400ms anticipatory lead time (100ms faster) for seamless sing-along (karaoke mode)
                        val leadTimeMs = 400L
                        val activeIdx = if (lyrics.isNotEmpty()) {
                            lyrics.indexOfLast { it.timeMs <= pos + leadTimeMs }
                        } else {
                            -1
                        }

                        _uiState.update {
                            it.copy(
                                currentPositionMs = pos,
                                durationMs = dur,
                                currentLyricIndex = activeIdx
                            )
                        }

                        // Periodically persist playback position every ~5 seconds (50 ticks)
                        if (progressTickCounter++ % 50 == 0) {
                            persistCurrentSession()
                        }
                    }
                }
                delay(100) // 100ms high-precision interval for instant lyric responsiveness
            }
        }
    }

    fun onSearchQueryChange(newQuery: String, immediate: Boolean = false) {
        _uiState.update { it.copy(searchQuery = newQuery) }
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        if (immediate) {
            recordSearchHistory(newQuery)
        }

        searchJob = viewModelScope.launch {
            if (!immediate) {
                delay(200) // Highly responsive 200ms debounce (cut down from 400ms)
            }
            _uiState.update { it.copy(isSearching = true) }
            val result = ytmRepository.search(newQuery)
            result.onSuccess { tracks ->
                _uiState.update { it.copy(searchResults = tracks, isSearching = false) }
                // Proactive 0ms Playback: prefetch the #1 top candidate while user is looking at results
                if (tracks.isNotEmpty()) {
                    val topTrack = tracks[0]
                    viewModelScope.launch(Dispatchers.IO) {
                        ytmRepository.prefetchStream(topTrack.videoId, topTrack.title, topTrack.artist)
                    }
                }
            }.onFailure { err ->
                Log.e("MUSE_PLAYER", "Search failed: ${err.message}")
                _uiState.update { it.copy(isSearching = false) }
            }
        }
    }

    fun recordSearchHistory(query: String) {
        if (query.isBlank()) return
        sessionRepository.addSearchQuery(query)
        _uiState.update { it.copy(searchHistory = sessionRepository.getSearchHistory()) }
    }

    fun removeSearchHistory(query: String) {
        sessionRepository.removeSearchQuery(query)
        _uiState.update { it.copy(searchHistory = sessionRepository.getSearchHistory()) }
    }

    fun clearSearchHistory() {
        sessionRepository.clearSearchHistory()
        _uiState.update { it.copy(searchHistory = emptyList()) }
    }

    fun playTrackWithVibeRadio(seedTrack: OnlineTrack) {
        if (_uiState.value.searchQuery.isNotBlank()) {
            recordSearchHistory(_uiState.value.searchQuery)
        }

        // 1. Instantly start playing the selected song (0ms queue latency)
        playOnlineTrack(seedTrack, queue = listOf(seedTrack))

        // 2. Asynchronously fetch similar-vibe songs in the background and attach to queue
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("MUSE_PLAYER", "Generating similar-vibe queue for '${seedTrack.title}' by '${seedTrack.artist}'")
                val similarTracks = ytmRepository.getSimilarTracks(seedTrack)
                if (similarTracks.isNotEmpty()) {
                    val fullQueue = listOf(seedTrack) + similarTracks
                    currentQueue = fullQueue
                    _uiState.update { it.copy(queue = fullQueue) }
                    Log.d("MUSE_PLAYER", "Similar-vibe queue populated with ${fullQueue.size} tracks")

                    // Look-ahead prefetch for next similar song
                    val nextTrack = similarTracks.first()
                    ytmRepository.prefetchStream(nextTrack.videoId, nextTrack.title, nextTrack.artist)
                }
            } catch (e: Exception) {
                Log.w("MUSE_PLAYER", "Error creating similar vibe radio: ${e.message}")
            }
        }
    }

    fun playOnlineTrack(
        track: OnlineTrack, 
        queue: List<OnlineTrack>? = null,
        initialSeekMs: Long = 0L
    ) {
        if (_uiState.value.searchQuery.isNotBlank()) {
            recordSearchHistory(_uiState.value.searchQuery)
        }

        if (!queue.isNullOrEmpty()) {
            currentQueue = queue
            currentTrackIndex = queue.indexOfFirst { it.videoId == track.videoId }
        } else if (currentQueue.isEmpty()) {
            currentQueue = _uiState.value.searchResults
            currentTrackIndex = currentQueue.indexOfFirst { it.videoId == track.videoId }
        } else {
            val foundIdx = currentQueue.indexOfFirst { it.videoId == track.videoId }
            if (foundIdx != -1) {
                currentTrackIndex = foundIdx
            } else {
                currentQueue = currentQueue + track
                currentTrackIndex = currentQueue.lastIndex
            }
        }

        currentTrackVideoId = track.videoId
        val isLiked = sessionRepository.isTrackLiked(track.videoId)

        _uiState.update {
            it.copy(
                queue = currentQueue,
                currentQueueIndex = currentTrackIndex,
                isCurrentTrackLiked = isLiked
            )
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    title = track.title,
                    artist = track.artist,
                    artworkUri = track.thumbnailUrl,
                    playbackStatus = if (track.videoId.startsWith("local_")) "Memutar audio lokal..." else "Mengambil stream audio...",
                    isError = false
                )
            }

            // Immediately fetch lyrics in background while audio stream is resolving
            fetchLyrics(track.title, track.artist)

            // Local audio file instant playback via Content URI
            if (track.videoId.startsWith("local_")) {
                val contentUri = track.videoId.removePrefix("local_")
                currentStreamUrl = contentUri
                playTrack(
                    mediaUri = contentUri,
                    title = track.title,
                    artist = track.artist,
                    artworkUrl = track.thumbnailUrl,
                    startPositionMs = initialSeekMs
                )
                scheduleNextTrackPrefetch()
                persistCurrentSession()
                return@launch
            }

            val result = ytmRepository.getStreamUrl(
                videoId = track.videoId,
                title = track.title,
                artist = track.artist
            )
            result.onSuccess { streamUrl ->
                currentStreamUrl = streamUrl
                // Record to cached list
                sessionRepository.addCachedTrack(track)
                _uiState.update { it.copy(cachedTracks = sessionRepository.getCachedTracks()) }

                playTrack(
                    mediaUri = streamUrl,
                    title = track.title,
                    artist = track.artist,
                    artworkUrl = track.thumbnailUrl,
                    startPositionMs = initialSeekMs
                )
                // Schedule background prefetch for the next track in queue for instant 0ms transitions!
                scheduleNextTrackPrefetch()
                persistCurrentSession()
            }.onFailure { err ->
                val errorMsg = "Gagal memutar lagu: ${err.message}"
                Log.e("ILVANE_PLAYER", errorMsg)
                _uiState.update { it.copy(playbackStatus = errorMsg, isError = true) }
            }
        }
    }

    // --- Library & Local Audio Management ---

    fun scanLocalAudio() {
        viewModelScope.launch(Dispatchers.IO) {
            val locals = localAudioRepository.scanLocalAudio()
            _uiState.update { it.copy(localTracks = locals, hasStoragePermission = true) }
        }
    }

    fun setStoragePermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(hasStoragePermission = granted) }
        if (granted) {
            scanLocalAudio()
        }
    }

    fun setLibraryFilter(filter: String) {
        _uiState.update { it.copy(libraryFilter = filter) }
    }

    fun toggleLikeCurrentTrack() {
        val track = currentQueue.getOrNull(currentTrackIndex) ?: run {
            val s = _uiState.value
            if (s.title != "No track playing" && currentTrackVideoId != null) {
                OnlineTrack(
                    videoId = currentTrackVideoId!!,
                    title = s.title,
                    artist = s.artist,
                    thumbnailUrl = s.artworkUri ?: ""
                )
            } else null
        } ?: return

        val nowLiked = sessionRepository.toggleLikeTrack(track)
        _uiState.update {
            it.copy(
                isCurrentTrackLiked = nowLiked,
                likedTracks = sessionRepository.getLikedTracks()
            )
        }
    }

    fun toggleLikeTrack(track: OnlineTrack) {
        val nowLiked = sessionRepository.toggleLikeTrack(track)
        val isCurrent = track.videoId == currentTrackVideoId
        _uiState.update {
            it.copy(
                isCurrentTrackLiked = if (isCurrent) nowLiked else it.isCurrentTrackLiked,
                likedTracks = sessionRepository.getLikedTracks()
            )
        }
    }

    fun fetchLyrics(title: String, artist: String, durationSeconds: Int = 0) {
        lyricsJob?.cancel()
        _uiState.update { it.copy(lyrics = emptyList(), isLyricsLoading = true, currentLyricIndex = -1) }

        lyricsJob = viewModelScope.launch {
            val result = lyricsRepository.getLyrics(title, artist, durationSeconds)
            result.onSuccess { parsedLyrics ->
                _uiState.update { it.copy(lyrics = parsedLyrics, isLyricsLoading = false) }
            }.onFailure {
                _uiState.update { it.copy(lyrics = emptyList(), isLyricsLoading = false) }
            }
        }
    }

    fun setQueueSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(isQueueSheetVisible = visible) }
    }

    fun toggleLyricsView() {
        _uiState.update { it.copy(isLyricsActive = !it.isLyricsActive) }
    }

    fun setLyricsViewActive(active: Boolean) {
        _uiState.update { it.copy(isLyricsActive = active) }
    }

    fun jumpToQueueIndex(index: Int) {
        if (index in currentQueue.indices) {
            playOnlineTrack(currentQueue[index])
        }
    }

    fun removeFromQueue(index: Int) {
        if (index in currentQueue.indices) {
            val updated = currentQueue.toMutableList().apply { removeAt(index) }
            currentQueue = updated
            val newIdx = when {
                index < currentTrackIndex -> currentTrackIndex - 1
                index == currentTrackIndex -> currentTrackIndex.coerceAtMost(updated.lastIndex)
                else -> currentTrackIndex
            }
            currentTrackIndex = newIdx
            _uiState.update {
                it.copy(
                    queue = updated,
                    currentQueueIndex = newIdx
                )
            }
        }
    }

    private fun scheduleNextTrackPrefetch() {
        prefetchJob?.cancel()
        if (currentQueue.isEmpty() || currentTrackIndex < 0) return

        val nextIndex = currentTrackIndex + 1
        if (nextIndex < currentQueue.size) {
            val nextTrack = currentQueue[nextIndex]
            prefetchJob = viewModelScope.launch(Dispatchers.IO) {
                delay(2000) // Allow current song initial buffering to complete cleanly
                Log.d("MUSE_PLAYER", "Pre-fetching next track in queue: '${nextTrack.title}'")
                ytmRepository.prefetchStream(nextTrack.videoId, nextTrack.title, nextTrack.artist)
            }
        }
    }

    fun playNextTrack() {
        if (currentQueue.isEmpty()) return
        if (_uiState.value.isShuffleEnabled && currentQueue.size > 1) {
            val candidateIndices = currentQueue.indices.filter { it != currentTrackIndex }
            val nextIdx = candidateIndices.random()
            playOnlineTrack(currentQueue[nextIdx])
            return
        }
        val nextIndex = currentTrackIndex + 1
        if (nextIndex < currentQueue.size) {
            playOnlineTrack(currentQueue[nextIndex])
        } else if (_uiState.value.repeatMode == Player.REPEAT_MODE_ALL) {
            // Loop back to start of playlist
            playOnlineTrack(currentQueue[0])
        } else {
            _uiState.update { it.copy(isPlaying = false, playbackStatus = "Antrean selesai") }
            persistCurrentSession()
        }
    }

    fun playPreviousTrack() {
        if (currentQueue.isEmpty()) return
        // If track played > 3s, standard behavior is restart from beginning
        if (_uiState.value.currentPositionMs > 3000L) {
            seekTo(0)
            return
        }
        val prevIndex = currentTrackIndex - 1
        if (prevIndex >= 0) {
            playOnlineTrack(currentQueue[prevIndex])
        } else {
            seekTo(0)
        }
    }

    fun toggleRepeatMode() {
        val newMode = when (_uiState.value.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        mediaController?.repeatMode = newMode
        _uiState.update { it.copy(repeatMode = newMode) }
        persistCurrentSession()
    }

    fun toggleShuffleMode() {
        val newShuffle = !_uiState.value.isShuffleEnabled
        mediaController?.shuffleModeEnabled = newShuffle
        _uiState.update { it.copy(isShuffleEnabled = newShuffle) }
        persistCurrentSession()
    }

    fun persistCurrentSession() {
        val state = _uiState.value
        val posToSave = if (state.currentPositionMs > 0L) {
            state.currentPositionMs
        } else if (pendingSeekPositionMs > 0L) {
            pendingSeekPositionMs
        } else {
            0L
        }
        sessionRepository.saveSession(
            videoId = currentTrackVideoId,
            title = if (state.title != "No track playing") state.title else null,
            artist = state.artist,
            artworkUrl = state.artworkUri,
            positionMs = posToSave,
            durationMs = state.durationMs,
            streamUrl = currentStreamUrl,
            queue = currentQueue,
            queueIndex = currentTrackIndex,
            isShuffle = state.isShuffleEnabled,
            repeatMode = state.repeatMode,
            isLightMode = state.isLightMode
        )
    }

    fun playTrack(
        mediaUri: String, 
        title: String, 
        artist: String, 
        artworkUrl: String? = null,
        startPositionMs: Long = 0L
    ) {
        val controller = mediaController
        if (controller == null) {
            val err = "Error: Audio Service belum tersambung!"
            Log.e("MUSE_PLAYER", err)
            _uiState.update { it.copy(playbackStatus = err, isError = true) }
            return
        }

        lastErrorMessage = null
        val parsedUri = Uri.parse(mediaUri)
        Log.d("MUSE_PLAYER", "playTrack -> Uri: $parsedUri | Title: $title | StartPos: $startPositionMs")
        _uiState.update { it.copy(playbackStatus = "Memuat audio...", isError = false) }

        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .apply {
                artworkUrl?.let { setArtworkUri(Uri.parse(it)) }
            }
            .build()

        val requestMetadata = MediaItem.RequestMetadata.Builder()
            .setMediaUri(parsedUri)
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(mediaUri)
            .setUri(parsedUri)
            .setRequestMetadata(requestMetadata)
            .setMediaMetadata(metadata)
            .build()

        if (startPositionMs > 0L) {
            controller.setMediaItem(mediaItem, startPositionMs)
        } else {
            controller.setMediaItem(mediaItem)
        }
        controller.prepare()
        controller.playWhenReady = true
        controller.play()
    }

    fun playLocalAudio() {
        val context = getApplication<Application>()
        val localUri = "android.resource://${context.packageName}/${R.raw.test_audio}"
        playTrack(
            mediaUri = localUri,
            title = "Acoustic Synth (Local Offline)",
            artist = "MUSE Built-in Audio"
        )
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
            persistCurrentSession()
        } else {
            if (controller.playbackState == Player.STATE_IDLE || controller.currentMediaItem == null) {
                // If we have a track restored from session, resume it directly with saved position!
                if (currentQueue.isNotEmpty() && currentTrackIndex in currentQueue.indices) {
                    val track = currentQueue[currentTrackIndex]
                    val resumePos = if (pendingSeekPositionMs > 0L) pendingSeekPositionMs else _uiState.value.currentPositionMs
                    pendingSeekPositionMs = 0L
                    playOnlineTrack(track, initialSeekMs = resumePos)
                    return
                }
                controller.prepare()
            }
            controller.play()
            persistCurrentSession()
        }
    }

    fun seekTo(positionMs: Long) {
        val controller = mediaController
        if (controller != null && controller.currentMediaItem != null) {
            controller.seekTo(positionMs)
        } else {
            pendingSeekPositionMs = positionMs
        }
        _uiState.update { it.copy(currentPositionMs = positionMs) }
        persistCurrentSession()
    }

    fun toggleLightMode() {
        _uiState.update { it.copy(isLightMode = !it.isLightMode) }
        persistCurrentSession()
    }

    fun switchScreen(screen: String) {
        _uiState.update { it.copy(activeScreen = screen) }
    }

    override fun onCleared() {
        persistCurrentSession()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        super.onCleared()
    }
}
