package com.muse.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.muse.app.data.model.OnlineTrack
import org.json.JSONArray
import org.json.JSONObject

/**
 * Sovereign Session Persistence Manager.
 * Remembers the last played track, playback position, queue, repeat/shuffle modes,
 * and theme across app restarts (Zero "Amnesia").
 */
class SessionRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("muse_playback_session", Context.MODE_PRIVATE)

    data class SavedSession(
        val videoId: String?,
        val title: String?,
        val artist: String?,
        val artworkUrl: String?,
        val positionMs: Long,
        val durationMs: Long,
        val streamUrl: String?,
        val queue: List<OnlineTrack>,
        val queueIndex: Int,
        val isShuffle: Boolean,
        val repeatMode: Int,
        val isLightMode: Boolean
    )

    fun saveSession(
        videoId: String?,
        title: String?,
        artist: String?,
        artworkUrl: String?,
        positionMs: Long,
        durationMs: Long,
        streamUrl: String?,
        queue: List<OnlineTrack>,
        queueIndex: Int,
        isShuffle: Boolean,
        repeatMode: Int,
        isLightMode: Boolean
    ) {
        val queueArray = JSONArray()
        queue.take(50).forEach { track ->
            val obj = JSONObject().apply {
                put("videoId", track.videoId)
                put("title", track.title)
                put("artist", track.artist)
                put("thumbnailUrl", track.thumbnailUrl)
                put("durationText", track.durationText)
            }
            queueArray.put(obj)
        }

        prefs.edit()
            .putString(KEY_VIDEO_ID, videoId)
            .putString(KEY_TITLE, title)
            .putString(KEY_ARTIST, artist)
            .putString(KEY_ARTWORK, artworkUrl)
            .putLong(KEY_POS, positionMs)
            .putLong(KEY_DUR, durationMs)
            .putString(KEY_STREAM_URL, streamUrl)
            .putString(KEY_QUEUE, queueArray.toString())
            .putInt(KEY_QUEUE_IDX, queueIndex)
            .putBoolean(KEY_SHUFFLE, isShuffle)
            .putInt(KEY_REPEAT, repeatMode)
            .putBoolean(KEY_LIGHT, isLightMode)
            .apply()
    }

    fun loadSession(): SavedSession? {
        val title = prefs.getString(KEY_TITLE, null) ?: return null
        val artist = prefs.getString(KEY_ARTIST, "Unknown Artist") ?: "Unknown Artist"
        val videoId = prefs.getString(KEY_VIDEO_ID, null)
        val artworkUrl = prefs.getString(KEY_ARTWORK, null)
        val pos = prefs.getLong(KEY_POS, 0L)
        val dur = prefs.getLong(KEY_DUR, 0L)
        val streamUrl = prefs.getString(KEY_STREAM_URL, null)
        val queueJson = prefs.getString(KEY_QUEUE, null)
        val queueIdx = prefs.getInt(KEY_QUEUE_IDX, 0)
        val isShuffle = prefs.getBoolean(KEY_SHUFFLE, false)
        val repeatMode = prefs.getInt(KEY_REPEAT, 0)
        val isLight = prefs.getBoolean(KEY_LIGHT, false)

        val queueList = mutableListOf<OnlineTrack>()
        if (!queueJson.isNullOrEmpty()) {
            try {
                val array = JSONArray(queueJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    queueList.add(
                        OnlineTrack(
                            videoId = obj.optString("videoId"),
                            title = obj.optString("title"),
                            artist = obj.optString("artist"),
                            thumbnailUrl = obj.optString("thumbnailUrl"),
                            durationText = obj.optString("durationText")
                        )
                    )
                }
            } catch (_: Exception) {
                // Ignore parse errors
            }
        }

        return SavedSession(
            videoId = videoId,
            title = title,
            artist = artist,
            artworkUrl = artworkUrl,
            positionMs = pos,
            durationMs = dur,
            streamUrl = streamUrl,
            queue = queueList,
            queueIndex = queueIdx,
            isShuffle = isShuffle,
            repeatMode = repeatMode,
            isLightMode = isLight
        )
    }

    // --- Sovereign Library Persistence: Liked (Favorites) & Cache ---

    fun getLikedTracks(): List<OnlineTrack> {
        return loadTrackList(KEY_LIKED_TRACKS)
    }

    fun isTrackLiked(videoId: String): Boolean {
        if (videoId.isBlank()) return false
        val liked = getLikedTracks()
        return liked.any { it.videoId == videoId }
    }

    fun toggleLikeTrack(track: OnlineTrack): Boolean {
        if (track.videoId.isBlank()) return false
        val current = getLikedTracks().toMutableList()
        val existingIndex = current.indexOfFirst { it.videoId == track.videoId }
        val nowLiked: Boolean
        if (existingIndex >= 0) {
            current.removeAt(existingIndex)
            nowLiked = false
        } else {
            current.add(0, track)
            nowLiked = true
        }
        saveTrackList(KEY_LIKED_TRACKS, current)
        return nowLiked
    }

    fun getCachedTracks(): List<OnlineTrack> {
        return loadTrackList(KEY_CACHED_TRACKS)
    }

    fun addCachedTrack(track: OnlineTrack) {
        if (track.videoId.isBlank()) return
        val current = getCachedTracks().toMutableList()
        current.removeAll { it.videoId == track.videoId }
        current.add(0, track) // Most recent on top (index 0)
        // FIFO: Max 12 tracks, oldest at the bottom are dropped first
        val trimmed = if (current.size > 12) current.take(12) else current
        saveTrackList(KEY_CACHED_TRACKS, trimmed)
    }

    private fun loadTrackList(prefKey: String): List<OnlineTrack> {
        val json = prefs.getString(prefKey, null) ?: return emptyList()
        val list = mutableListOf<OnlineTrack>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    OnlineTrack(
                        videoId = obj.optString("videoId"),
                        title = obj.optString("title"),
                        artist = obj.optString("artist"),
                        thumbnailUrl = obj.optString("thumbnailUrl"),
                        durationText = obj.optString("durationText")
                    )
                )
            }
        } catch (_: Exception) {
            // Ignore corrupted JSON
        }
        return list
    }

    private fun saveTrackList(prefKey: String, list: List<OnlineTrack>) {
        val array = JSONArray()
        list.forEach { track ->
            val obj = JSONObject().apply {
                put("videoId", track.videoId)
                put("title", track.title)
                put("artist", track.artist)
                put("thumbnailUrl", track.thumbnailUrl)
                put("durationText", track.durationText)
            }
            array.put(obj)
        }
        prefs.edit().putString(prefKey, array.toString()).apply()
    }

    // --- Search Query History (FIFO, Max 15) ---

    fun getSearchHistory(): List<String> {
        val json = prefs.getString(KEY_SEARCH_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val q = array.getString(i)
                if (q.isNotBlank()) list.add(q)
            }
        } catch (_: Exception) {}
        return list
    }

    fun addSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        val current = getSearchHistory().toMutableList()
        current.removeAll { it.equals(trimmed, ignoreCase = true) }
        current.add(0, trimmed) // Most recent first
        val capped = current.take(15)
        val array = JSONArray()
        capped.forEach { array.put(it) }
        prefs.edit().putString(KEY_SEARCH_HISTORY, array.toString()).apply()
    }

    fun removeSearchQuery(query: String) {
        val current = getSearchHistory().toMutableList()
        current.removeAll { it.equals(query.trim(), ignoreCase = true) }
        val array = JSONArray()
        current.forEach { array.put(it) }
        prefs.edit().putString(KEY_SEARCH_HISTORY, array.toString()).apply()
    }

    fun clearSearchHistory() {
        prefs.edit().remove(KEY_SEARCH_HISTORY).apply()
    }

    // --- Dynamic Live Chart Cache Persistence ---

    fun saveLiveChart(chartKey: String, tracks: List<OnlineTrack>) {
        saveTrackList(chartKey, tracks)
        val timestampKey = "${chartKey}_timestamp"
        prefs.edit().putLong(timestampKey, System.currentTimeMillis()).apply()
    }

    fun getLiveChart(chartKey: String): List<OnlineTrack> {
        return loadTrackList(chartKey)
    }

    fun getLiveChartLastUpdated(chartKey: String): Long {
        val timestampKey = "${chartKey}_timestamp"
        return prefs.getLong(timestampKey, 0L)
    }

    companion object {
        private const val KEY_VIDEO_ID = "key_video_id"
        private const val KEY_TITLE = "key_title"
        private const val KEY_ARTIST = "key_artist"
        private const val KEY_ARTWORK = "key_artwork"
        private const val KEY_POS = "key_pos"
        private const val KEY_DUR = "key_dur"
        private const val KEY_STREAM_URL = "key_stream_url"
        private const val KEY_QUEUE = "key_queue"
        private const val KEY_QUEUE_IDX = "key_queue_idx"
        private const val KEY_SHUFFLE = "key_shuffle"
        private const val KEY_REPEAT = "key_repeat"
        private const val KEY_LIGHT = "key_light"
        private const val KEY_LIKED_TRACKS = "key_liked_tracks"
        private const val KEY_CACHED_TRACKS = "key_cached_tracks"
        private const val KEY_SEARCH_HISTORY = "key_search_history"
        const val KEY_CHART_INDO = "key_live_chart_indo"
        const val KEY_CHART_GLOBAL = "key_live_chart_global"
    }
}
