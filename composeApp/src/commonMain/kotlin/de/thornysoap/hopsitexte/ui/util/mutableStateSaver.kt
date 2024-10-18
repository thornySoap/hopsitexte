package de.thornysoap.hopsitexte.ui.util

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.snapshots.SnapshotMutableState

// Copied from https://github.com/JetBrains/compose-multiplatform-core/blob/jb-main/compose/runtime/runtime-saveable/src/commonMain/kotlin/androidx/compose/runtime/saveable/RememberSaveable.kt

/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@Suppress("UNCHECKED_CAST")
internal fun <T> mutableStateSaver(inner: Saver<T, out Any>) =
    with(inner as Saver<T, Any>) {
        Saver<MutableState<T>, MutableState<Any?>>(
            save = { state ->
                require(state is SnapshotMutableState<T>) {
                    "If you use a custom MutableState implementation you have to write a custom " +
                            "Saver and pass it as a saver param to rememberSaveable()"
                }
                val saved = save(state.value)
                if (saved != null) {
                    mutableStateOf(saved, state.policy as SnapshotMutationPolicy<Any?>)
                } else {
                    // if the inner saver returned null we need to return null as well so the
                    // user's init lambda will be used instead of restoring mutableStateOf(null)
                    null
                }
            },
            restore =
            @Suppress("UNCHECKED_CAST", "ExceptionMessage") {
                require(it is SnapshotMutableState<Any?>)
                mutableStateOf(
                    if (it.value != null) restore(it.value!!) else null,
                    it.policy as SnapshotMutationPolicy<T?>
                )
                        as MutableState<T>
            }
        )
    }
