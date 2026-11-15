package com.muse.app.service

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

object MuseAudioCache {
    private var instance: SimpleCache? = null

    @OptIn(UnstableApi::class)
    @Synchronized
    fun getInstance(context: Context): SimpleCache {
        if (instance == null) {
            val cacheDir = File(context.cacheDir, "muse_audio_cache")
            val evictor = LeastRecentlyUsedCacheEvictor(1024L * 1024L * 1024L) // 1 GB Smart Cache
            val databaseProvider = StandaloneDatabaseProvider(context)
            instance = SimpleCache(cacheDir, evictor, databaseProvider)
        }
        return instance!!
    }
}
