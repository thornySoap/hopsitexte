package de.thornysoap.hopsitexte.ui.util

import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun VerticalScrollbar(
    listState: LazyListState,
    modifier: Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    androidx.compose.foundation.VerticalScrollbar(
        adapter = rememberScrollbarAdapter(listState),
        modifier = modifier,
        style = defaultScrollbarStyle().copy(
            unhoverColor = colorScheme.secondary.copy(alpha = 0.5f),
            hoverColor = colorScheme.primary.copy(alpha = 0.5f),
        ),
    )
}
