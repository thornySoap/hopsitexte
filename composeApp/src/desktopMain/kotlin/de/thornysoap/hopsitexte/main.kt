package de.thornysoap.hopsitexte

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import de.thornysoap.hopsitexte.ui.App
import hopsitexte.composeapp.generated.resources.Res
import hopsitexte.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource(Res.string.app_name),
        icon = painterResource("mipmap/icon.webp"),
    ) {
        App()
    }
}
