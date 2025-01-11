package com.example.course

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val date: Long,
    val type: MediaType,
    var duration: String? = null
)