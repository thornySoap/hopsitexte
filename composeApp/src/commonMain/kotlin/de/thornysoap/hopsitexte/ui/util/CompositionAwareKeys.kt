package de.thornysoap.hopsitexte.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

class CompositionAwareKeys(layoutDirection: LayoutDirection) {

    val DirectionStart: Key =
        if (layoutDirection == LayoutDirection.Ltr) Key.DirectionLeft else Key.DirectionRight

    val DirectionEnd: Key =
        if (layoutDirection == LayoutDirection.Ltr) Key.DirectionRight else Key.DirectionLeft
}

@Composable
fun rememberCompositionAwareKeys(): CompositionAwareKeys {
    val layoutDirection = LocalLayoutDirection.current
    return remember { CompositionAwareKeys(layoutDirection) }
}
