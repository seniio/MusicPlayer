package com.muse.app.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.muse.app.data.model.OnlineTrack

/**
 * Sovereign Local Storage Audio Scanner.
 * Queries MediaStore.Audio for on-device tracks (MP3/FLAC/M4A),
 * filtering out voice notes, ringtones, and notification sound effects.
 */
class LocalAudioRepository(private val context: Context) {

    fun scanLocalAudio(): List<OnlineTrack> {
        val tracks = mutableListOf<OnlineTrack>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )

        // Strict filters: Only music files, and duration >= 30 seconds
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 30000"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val title = c.getString(titleCol) ?: "Unknown Track"
                    val rawArtist = c.getString(artistCol) ?: "Unknown Artist"
                    val artist = if (rawArtist == "<unknown>" || rawArtist.isBlank()) "Local Artist" else rawArtist
                    val album = c.getString(albumCol) ?: ""
                    val durationMs = c.getLong(durationCol)
                    val albumId = c.getLong(albumIdCol)

                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val artworkUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    )

                    val minutes = (durationMs / 1000) / 60
                    val seconds = (durationMs / 1000) % 60
                    val durText = String.format("%d:%02d", minutes, seconds)

                    tracks.add(
                        OnlineTrack(
                            videoId = "local_${contentUri}",
                            title = title,
                            artist = artist,
                            album = album,
                            durationText = durText,
                            durationSeconds = durationMs / 1000,
                            thumbnailUrl = artworkUri.toString()
                        )
                    )
                }
            }
            Log.d("ILVANE_LOCAL", "Scanned ${tracks.size} local tracks successfully.")
        } catch (e: Exception) {
            Log.e("ILVANE_LOCAL", "Error scanning local media: ${e.message}", e)
        }

        return tracks
    }
}
