package com.tds.puzzivo.core.util

fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    val remainingSeconds = seconds % 60

    return when {
        hours > 0 -> "${hours}h ${remainingMinutes}m"
        minutes > 0 -> "${minutes}m ${remainingSeconds}s"
        else -> "${remainingSeconds}s"
    }
}
