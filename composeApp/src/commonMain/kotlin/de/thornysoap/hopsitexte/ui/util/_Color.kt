package de.thornysoap.hopsitexte.ui.util

import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

fun Color.readableForeground(): Color =
    if (this == Color.Unspecified) Color.Unspecified
    else if (luminance() < 0.4) Color.White
    else Color.Black

fun Color.toHexString(): String = toArgb().toUInt().toString(radix = 16)

val Color.Companion.Saver: Saver<Color, Any>
    get() = Saver(
        save = { if (it.isUnspecified) Unit else it.toArgb() },
        restore = { if (it == Unit) Unspecified else Color(it as Int) },
    )
