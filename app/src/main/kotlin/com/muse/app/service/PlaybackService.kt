package com.muse.app.service

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.muse.app.MainActivity

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        // 1. Audio Attributes for Music & Audio Focus
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        // 2. Comprehensive DataSource Factory with Chrome Desktop User-Agent
        // Eliminates 403 Forbidden blocks from Google Video CDN
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36")
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)
            .setAllowCrossProtocolRedirects(true)

        val fullDataSourceFactory = DefaultDataSource.Factory(this, httpDataSourceFactory)

        // 3. Connect to Smart Cache (1GB Local Flash Cache for Battery Preservation)
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(MuseAudioCache.getInstance(this))
            .setUpstreamDataSourceFactory(fullDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val mediaSourceFactory = DefaultMediaSourceFactory(this)
            .setDataSourceFactory(cacheDataSourceFactory)

        // 4. Ultra-Fast Buffering LoadControl: Starts playback in just 200ms!
        val lowLatencyLoadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs= */ 8_000,
                /* maxBufferMs= */ 30_000,
                /* bufferForPlaybackMs= */ 200, // Starts playing after 200ms buffer!
                /* bufferForPlaybackAfterRebufferMs= */ 800
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        // 5. Initialize ExoPlayer with Cache & Low-Latency
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
            .setHandleAudioBecomingNoisy(true) // Auto-pause on TWS disconnect
            .setWakeMode(C.WAKE_MODE_LOCAL)    // Play with screen locked
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(lowLatencyLoadControl)
            .build()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                val stateName = when (state) {
                    Player.STATE_IDLE -> "STATE_IDLE"
                    Player.STATE_BUFFERING -> "STATE_BUFFERING"
                    Player.STATE_READY -> "STATE_READY (Playing: ${player.isPlaying})"
                    Player.STATE_ENDED -> "STATE_ENDED"
                    else -> "STATE_$state"
                }
                Log.d("MUSE_SERVICE", "Service ExoPlayer State: $stateName (playWhenReady: ${player.playWhenReady})")
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("MUSE_SERVICE", "Service ExoPlayer ERROR: ${error.errorCodeName} - ${error.message}", error)
            }
        })

        // 6. Activity PendingIntent for Notification
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 7. Build MediaSession
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(MediaSessionCallback())
            .build()

        Log.d("MUSE_SERVICE", "PlaybackService & MediaSession successfully created with Cache & Low-Latency LoadControl.")
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private inner class MediaSessionCallback : MediaSession.Callback {

        private fun resolveMediaItem(item: MediaItem): MediaItem {
            val resolvedUri: Uri = item.requestMetadata.mediaUri
                ?: item.localConfiguration?.uri
                ?: try { Uri.parse(item.mediaId) } catch (e: Exception) { Uri.EMPTY }

            Log.d("MUSE_SERVICE", "Resolving MediaItem URI -> $resolvedUri (id: ${item.mediaId})")

            return item.buildUpon()
                .setUri(resolvedUri)
                .build()
        }

        override fun onSetMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
            startIndex: Int,
            startPositionMs: Long
        ): com.google.common.util.concurrent.ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            val resolvedItems = mediaItems.map { resolveMediaItem(it) }.toMutableList()
            Log.d("MUSE_SERVICE", "onSetMediaItems: ${resolvedItems.size} items resolved.")
            return com.google.common.util.concurrent.Futures.immediateFuture(
                MediaSession.MediaItemsWithStartPosition(resolvedItems, startIndex, startPositionMs)
            )
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): com.google.common.util.concurrent.ListenableFuture<MutableList<MediaItem>> {
            val resolvedItems = mediaItems.map { resolveMediaItem(it) }.toMutableList()
            Log.d("MUSE_SERVICE", "onAddMediaItems: ${resolvedItems.size} items resolved.")
            return com.google.common.util.concurrent.Futures.immediateFuture(resolvedItems)
        }
    }
}
