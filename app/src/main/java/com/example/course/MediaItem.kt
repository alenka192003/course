package com.example.course

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import android.net.Uri

@Parcelize
data class MediaItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val date: Long,
    val type: MediaType,
    var duration: String? = null
) : Parcelable