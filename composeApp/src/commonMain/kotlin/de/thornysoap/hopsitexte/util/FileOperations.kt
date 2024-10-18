package de.thornysoap.hopsitexte.util

import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.pickFile

object FileOperations {

    suspend fun readFile(title: String): String? = FileKit
        .pickFile(
            type = PickerType.File(extensions = listOf("txt", "md")),
            title = title,
        )?.readBytes()
        ?.let { String(it) }

    suspend fun saveFile(text: String, baseName: String) {
        FileKit.saveFile(
            bytes = text.toByteArray(),
            baseName = baseName,
            extension = "txt",
        )
    }
}
