package de.thornysoap.hopsitexte.ui.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Scrollbar only exists on desktop
@Composable
expect fun VerticalScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier,
)
