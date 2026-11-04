package com.tds.puzzivo.core.util

import androidx.compose.runtime.saveable.Saver

/** Compact codecs for primitive collections stored in an Android saved-state Bundle. */
val booleanListSaver: Saver<List<Boolean>, String> = Saver(
    save = { values -> values.joinToString(separator = "") { if (it) "1" else "0" } },
    restore = { encoded -> encoded.map { it == '1' } }
)

val intListSaver: Saver<List<Int>, String> = Saver(
    save = { values -> values.joinToString(separator = ",") },
    restore = { encoded ->
        if (encoded.isBlank()) emptyList() else encoded.split(',').mapNotNull(String::toIntOrNull)
    }
)
