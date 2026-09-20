package com.disspear574.swishy.media

data class MediaAsset(
    val id: String,
    val kind: MediaKind,
    val takenAtMillis: Long,
    val sizeBytes: Long,
    val durationMillis: Long?,
)

enum class MediaKind { PHOTO, VIDEO }
