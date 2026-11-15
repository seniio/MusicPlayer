package com.muse.app.data.model

data class OnlineTrack(
    val videoId: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val durationText: String = "",
    val durationSeconds: Long = 0L,
    val thumbnailUrl: String = ""
)
