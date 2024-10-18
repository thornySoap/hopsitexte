package de.thornysoap.hopsitexte.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import kotlin.reflect.KProperty

/**
 * Has the same function as [MutableState] but doesn't recompose on schedule
 */
data class ValueWrapper<T>(var value: T) {

    operator fun getValue(obj: Any?, property: KProperty<*>): T = value

    operator fun setValue(obj: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}


@Composable
fun <T> rememberValueWrapper(initial: T) = remember { ValueWrapper(initial) }
