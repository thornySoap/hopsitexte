package de.thornysoap.hopsitexte.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import de.thornysoap.hopsitexte.model.Hopsitext
import de.thornysoap.hopsitexte.ui.component.Editor
import de.thornysoap.hopsitexte.ui.theme.AppTheme
import de.thornysoap.hopsitexte.util.FileOperations
import hopsitexte.composeapp.generated.resources.Res
import hopsitexte.composeapp.generated.resources.about_app
import hopsitexte.composeapp.generated.resources.app_name
import hopsitexte.composeapp.generated.resources.close
import hopsitexte.composeapp.generated.resources.dark_mode
import hopsitexte.composeapp.generated.resources.display_settings
import hopsitexte.composeapp.generated.resources.download
import hopsitexte.composeapp.generated.resources.export_base
import hopsitexte.composeapp.generated.resources.export_file
import hopsitexte.composeapp.generated.resources.hide_about
import hopsitexte.composeapp.generated.resources.hide_setting
import hopsitexte.composeapp.generated.resources.hide_spoilers
import hopsitexte.composeapp.generated.resources.import_file
import hopsitexte.composeapp.generated.resources.import_title
import hopsitexte.composeapp.generated.resources.light_mode
import hopsitexte.composeapp.generated.resources.lightbulb
import hopsitexte.composeapp.generated.resources.show_about
import hopsitexte.composeapp.generated.resources.show_settings
import hopsitexte.composeapp.generated.resources.show_spoilers
import hopsitexte.composeapp.generated.resources.switch_darkmode
import hopsitexte.composeapp.generated.resources.switch_lightmode
import hopsitexte.composeapp.generated.resources.trophy
import hopsitexte.composeapp.generated.resources.upload
import hopsitexte.composeapp.generated.resources.visibility
import hopsitexte.composeapp.generated.resources.visibility_off
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun App() {
    val coroutineScope = rememberCoroutineScope()

    val systemDarkTheme = isSystemInDarkTheme()
    var darkMode by remember(systemDarkTheme) { mutableStateOf(systemDarkTheme) }

    val hopsitext = rememberSaveable(saver = Hopsitext.Saver) { Hopsitext() }

    AppTheme(darkTheme = darkMode) {
        val colorScheme = MaterialTheme.colorScheme
        val typography = MaterialTheme.typography
        val shapes = MaterialTheme.shapes

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.background,
        ) {
            var loading by remember { mutableStateOf(false) }
            val showAbout = remember { MutableTransitionState(false) }
            var showEditorSettings by remember { mutableStateOf(true) }
            var showEditorSpoilers by remember { mutableStateOf(true) }

            Column {
                FlowRow(
                    modifier = Modifier
                        .background(color = colorScheme.surfaceContainerHighest)
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.app_name),
                        modifier = Modifier
                            .width(192.dp)
                            .align(Alignment.CenterVertically),
                        color = colorScheme.primary,
                        textAlign = TextAlign.Center,
                        style = typography.titleLarge,
                    )

                    Row(modifier = Modifier.align(Alignment.CenterVertically)) {
                        OutlinedButton(onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                loading = true
                                FileOperations.readFile(title = getString(Res.string.import_title))
                                    ?.let {
                                        hopsitext.load(it)
                                        coroutineScope.launch {
                                            hopsitext.performHopsiCalculation(fromLine = 0, minimumToLines = null)
                                        }
                                    }
                                    ?: throw CancellationException("No file selected")
                            }.invokeOnCompletion { loading = false }
                        }) {
                            Icon(
                                painter = painterResource(Res.drawable.upload),
                                contentDescription = null,
                            )
                            Text(
                                text = stringResource(Res.string.import_file),
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    loading = true
                                    FileOperations.saveFile(
                                        text = hopsitext.save(),
                                        baseName = getString(Res.string.export_base),
                                    )
                                }.invokeOnCompletion { loading = false }
                            },
                            modifier = Modifier.padding(start = 12.dp),
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.download),
                                contentDescription = null,
                            )
                            Text(
                                text = stringResource(Res.string.export_file),
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }

                    Row(modifier = Modifier.align(Alignment.CenterVertically)) {
                        OptionToggleButton(
                            state = showEditorSpoilers,
                            onToggle = { showEditorSpoilers = !showEditorSpoilers },
                            enabledIcon = Res.drawable.trophy,
                            disabledIcon = Res.drawable.visibility_off,
                            enabledDescription = Res.string.hide_spoilers,
                            disabledDescription = Res.string.show_spoilers,
                        )

                        OptionToggleButton(
                            state = showEditorSettings,
                            onToggle = { showEditorSettings = !showEditorSettings },
                            enabledIcon = Res.drawable.display_settings,
                            disabledIcon = Res.drawable.visibility,
                            enabledDescription = Res.string.show_settings,
                            disabledDescription = Res.string.hide_setting,
                        )

                        OptionToggleButton(
                            state = darkMode,
                            onToggle = { darkMode = !darkMode },
                            enabledIcon = Res.drawable.dark_mode,
                            disabledIcon = Res.drawable.light_mode,
                            enabledDescription = Res.string.switch_lightmode,
                            disabledDescription = Res.string.switch_darkmode,
                        )

                        OptionToggleButton(
                            state = !showAbout.targetState,
                            onToggle = { showAbout.targetState = !showAbout.targetState },
                            enabledIcon = Res.drawable.lightbulb,
                            disabledIcon = Res.drawable.close,
                            enabledDescription = Res.string.show_about,
                            disabledDescription = Res.string.hide_about,
                        )
                    }
                }

                Editor(
                    hopsitext = hopsitext,
                    modifier = Modifier.fillMaxSize(),
                    showSettings = showEditorSettings,
                    showSpoilers = showEditorSpoilers,
                    coroutineScope = coroutineScope,
                )
            }

            AnimatedVisibility(
                visible = loading || showAbout.targetState,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .background(color = colorScheme.background.copy(alpha = 0.5f))
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { },
                        ),
                ) {
                    if (loading)
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }

            if (showAbout.currentState || !showAbout.isIdle)
                Popup(
                    alignment = Alignment.Center,
                    onDismissRequest = { showAbout.targetState = false },
                    properties = PopupProperties(focusable = true),
                ) {
                    AnimatedVisibility(
                        visibleState = showAbout,
                        enter = fadeIn() + slideInVertically { -it / 4 },
                        exit = fadeOut() + slideOutVertically { -it / 4 },
                    ) {
                        Surface(
                            modifier = Modifier
                                .padding(16.dp)
                                .width(600.dp)
                                .sizeIn(maxHeight = 600.dp),
                            shape = shapes.large,
                            color = colorScheme.surfaceVariant,
                        ) {
                            Column(
                                modifier = Modifier
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = stringResource(Res.string.app_name),
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    color = colorScheme.primary,
                                    style = typography.titleLarge,
                                )

                                Text(
                                    text = stringResource(Res.string.about_app),
                                    style = typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
        }
    }
}

@Composable
private fun OptionToggleButton(
    state: Boolean,
    onToggle: () -> Unit,
    enabledIcon: DrawableResource,
    disabledIcon: DrawableResource,
    enabledDescription: StringResource,
    disabledDescription: StringResource,
) {
    IconButton(onClick = onToggle) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                (slideIntoContainer(
                    if (targetState) AnimatedContentTransitionScope.SlideDirection.Up
                    else AnimatedContentTransitionScope.SlideDirection.Down,
                ) { (it * 1.5).roundToInt() } togetherWith slideOutOfContainer(
                    if (targetState) AnimatedContentTransitionScope.SlideDirection.Up
                    else AnimatedContentTransitionScope.SlideDirection.Down,
                ) { (it * 1.5).roundToInt() }).using(SizeTransform(clip = false))
            },
        ) { enabled ->
            Icon(
                painter = painterResource(if (enabled) enabledIcon else disabledIcon),
                contentDescription = stringResource(if (enabled) enabledDescription else disabledDescription),
            )
        }
    }
}
