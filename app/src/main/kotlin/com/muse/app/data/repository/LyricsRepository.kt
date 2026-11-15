package com.muse.app.data.repository

import android.util.Log
import com.muse.app.data.model.LyricLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class LyricsRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val lrcPattern = Pattern.compile("\\[(\\d{1,2}):(\\d{1,2})(?:\\.(\\d{1,3}))?\\](.*)")

    suspend fun getLyrics(
        title: String,
        artist: String,
        durationSeconds: Int = 0
    ): Result<List<LyricLine>> = withContext(Dispatchers.IO) {
        val cleanTitle = cleanTrackTitle(title)
        val cleanArtist = cleanArtistName(artist)

        try {
            // Strategy 1: Exact match query on /api/get
            var lyrics = fetchExactLyrics(cleanTitle, cleanArtist, durationSeconds)
            if (lyrics.isEmpty()) {
                // Strategy 2: Fuzzy search query on /api/search?q=
                lyrics = searchLyrics(cleanTitle, cleanArtist)
            }

            if (lyrics.isNotEmpty()) {
                Log.d("LYRICS_REPO", "Successfully loaded ${lyrics.size} synced lyrics for '$cleanTitle' by '$cleanArtist'")
                Result.success(lyrics)
            } else {
                Log.d("LYRICS_REPO", "No lyrics found for '$cleanTitle' by '$cleanArtist'")
                Result.failure(Exception("Lirik belum tersedia untuk lagu ini."))
            }
        } catch (e: Exception) {
            Log.e("LYRICS_REPO", "Error fetching lyrics for '$cleanTitle': ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun fetchExactLyrics(title: String, artist: String, durationSeconds: Int): List<LyricLine> {
        val encTitle = URLEncoder.encode(title, "UTF-8")
        val encArtist = URLEncoder.encode(artist, "UTF-8")
            var url = "https://lrclib.net/api/get?track_name=$encTitle&artist_name=$encArtist"
        if (durationSeconds > 0) {
            url += "&duration=$durationSeconds"
        }

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "MUSE-Player/1.0.0 (https://github.com/muse/app)")
            .get()
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val body = response.body?.string() ?: return emptyList()
        val json = JSONObject(body)
        val synced = json.optString("syncedLyrics", "")
        if (synced.isNotEmpty()) {
            return parseLrc(synced)
        }

        val plain = json.optString("plainLyrics", "")
        if (plain.isNotEmpty()) {
            return parsePlainLyrics(plain)
        }
        return emptyList()
    }

    private fun searchLyrics(title: String, artist: String): List<LyricLine> {
        val query = "$title $artist".trim()
        val encQuery = URLEncoder.encode(query, "UTF-8")
        val url = "https://lrclib.net/api/search?q=$encQuery"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "MUSE-Player/1.0.0 (https://github.com/muse/app)")
            .get()
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val body = response.body?.string() ?: return emptyList()
        val jsonArray = JSONArray(body)
        if (jsonArray.length() == 0) return emptyList()

        // 1. Look for first entry with syncedLyrics
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val synced = item.optString("syncedLyrics", "")
            if (synced.isNotEmpty()) {
                return parseLrc(synced)
            }
        }

        // 2. Fallback to first entry with plainLyrics
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val plain = item.optString("plainLyrics", "")
            if (plain.isNotEmpty()) {
                return parsePlainLyrics(plain)
            }
        }
        return emptyList()
    }

    fun parseLrc(lrcContent: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        val rawLines = lrcContent.split("\n")

        for (raw in rawLines) {
            val matcher = lrcPattern.matcher(raw.trim())
            if (matcher.matches()) {
                val min = matcher.group(1)?.toLongOrNull() ?: 0L
                val sec = matcher.group(2)?.toLongOrNull() ?: 0L
                val msString = matcher.group(3)
                val ms = when (msString?.length) {
                    1 -> msString.toLong() * 100
                    2 -> msString.toLong() * 10
                    3 -> msString.toLong()
                    else -> 0L
                }

                val timeMs = (min * 60_000) + (sec * 1_000) + ms
                val text = matcher.group(4)?.trim() ?: ""
                lines.add(LyricLine(timeMs, text))
            }
        }
        return lines.sortedBy { it.timeMs }
    }

    private fun parsePlainLyrics(plain: String): List<LyricLine> {
        val rawLines = plain.split("\n")
        return rawLines
            .filter { it.isNotBlank() }
            .mapIndexed { index, line ->
                // Fake proportional timestamps for plain lyrics so they display gracefully
                LyricLine(index * 4000L, line.trim())
            }
    }

    private fun cleanTrackTitle(title: String): String {
        return title
            .replace(Regex("(?i)\\s*\\(official\\s*(music)?\\s*(video|audio|lyric)\\)"), "")
            .replace(Regex("(?i)\\s*\\[official\\s*(music)?\\s*(video|audio|lyric)\\]"), "")
            .replace(Regex("(?i)\\s*\\(lyric\\s*video\\)"), "")
            .replace(Regex("(?i)\\s*\\(audio\\)"), "")
            .replace(Regex("(?i)\\s*\\(visualizer\\)"), "")
            .replace(Regex("(?i)\\s*\\(remastered\\s*\\d*\\)"), "")
            .trim()
    }

    private fun cleanArtistName(artist: String): String {
        // Raw artist from YouTube Music often looks like: "Coldplay • Ghost Stories • 4:28"
        val splitBullet = artist.split("•")
        val mainArtist = splitBullet[0]
        return mainArtist
            .replace(Regex("(?i)\\s*-\\s*topic"), "")
            .replace(Regex("(?i)\\s*vevo"), "")
            .trim()
    }
}
