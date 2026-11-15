package com.muse.app

import android.app.Application
import android.util.Log
import com.muse.app.data.extractor.OkHttpDownloader
import okhttp3.OkHttpClient
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import java.util.concurrent.TimeUnit

class MuseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build()

            NewPipe.init(
                OkHttpDownloader(okHttpClient),
                Localization.DEFAULT,
                ContentCountry("US")
            )
            Log.d("MUSE_APP", "NewPipeExtractor initialized successfully.")
        } catch (e: Exception) {
            Log.e("MUSE_APP", "Failed to initialize NewPipeExtractor: ${e.message}", e)
        }
    }
}
