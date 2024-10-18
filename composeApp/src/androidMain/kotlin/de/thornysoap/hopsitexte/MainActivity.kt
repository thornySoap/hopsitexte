package de.thornysoap.hopsitexte

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import de.thornysoap.hopsitexte.ui.App
import io.github.vinceglb.filekit.core.FileKit

class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FileKit.init(this)

        setContent {
            App()
        }
    }
}
