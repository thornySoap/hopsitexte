package de.thornysoap.hopsitexte.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import de.thornysoap.hopsitexte.model.Hopsitext
import de.thornysoap.hopsitexte.ui.theme.AmiraDefault
import de.thornysoap.hopsitexte.ui.theme.BelaDefault
import de.thornysoap.hopsitexte.ui.theme.UnionDefault
import de.thornysoap.hopsitexte.ui.util.Saver
import de.thornysoap.hopsitexte.ui.util.VerticalScrollbar
import de.thornysoap.hopsitexte.ui.util.mutableStateSaver
import de.thornysoap.hopsitexte.ui.util.readableForeground
import de.thornysoap.hopsitexte.ui.util.toHexString
import hopsitexte.composeapp.generated.resources.Res
import hopsitexte.composeapp.generated.resources.amira
import hopsitexte.composeapp.generated.resources.bela
import hopsitexte.composeapp.generated.resources.bottom_panel_close
import hopsitexte.composeapp.generated.resources.change_width
import hopsitexte.composeapp.generated.resources.color_prefix
import hopsitexte.composeapp.generated.resources.description
import hopsitexte.composeapp.generated.resources.edit
import hopsitexte.composeapp.generated.resources.edit_color
import hopsitexte.composeapp.generated.resources.font_placeholder
import hopsitexte.composeapp.generated.resources.is_hopsitext
import hopsitexte.composeapp.generated.resources.new_line_placeholder
import hopsitexte.composeapp.generated.resources.new_text_placeholder
import hopsitexte.composeapp.generated.resources.no_hopsitext_with_line
import hopsitexte.composeapp.generated.resources.no_winner
import hopsitexte.composeapp.generated.resources.resulting_winner
import hopsitexte.composeapp.generated.resources.show_text_information
import hopsitexte.composeapp.generated.resources.spoiler_placeholder
import hopsitexte.composeapp.generated.resources.total_characters
import hopsitexte.composeapp.generated.resources.total_jumps_with_name
import hopsitexte.composeapp.generated.resources.union
import hopsitexte.composeapp.generated.resources.width_arrow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Editor(
    hopsitext: Hopsitext,
    modifier: Modifier = Modifier,
    showSettings: Boolean = true,
    showSpoilers: Boolean = true,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
) {
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shapes = MaterialTheme.shapes

    BoxWithConstraints(modifier = modifier) {
        val fontStyles = listOf(typography.bodySmall, typography.bodyMedium, typography.bodyLarge)

        val windowWidth = constraints.minWidth
        var editorWidth by rememberSaveable { mutableFloatStateOf(windowWidth.toFloat()) }
        var fontStyle by rememberSaveable { mutableIntStateOf(1) }
        var belaColor by rememberSaveable(saver = mutableStateSaver(Color.Saver)) { mutableStateOf(BelaDefault) }
        var amiraColor by rememberSaveable(saver = mutableStateSaver(Color.Saver)) { mutableStateOf(AmiraDefault) }
        var unionColor by rememberSaveable(saver = mutableStateSaver(Color.Saver)) { mutableStateOf(UnionDefault) }

        val listState = rememberLazyListState()
        val cursorDirective = remember { CursorDirective() }
        var showInformationFloating by remember { mutableStateOf<Boolean?>(null) }

        LaunchedEffect(Unit) {
            snapshotFlow { listState.layoutInfo }
                .collect { layoutInfo ->
                    if (layoutInfo.visibleItemsInfo.find { it.index == layoutInfo.totalItemsCount - 1 } != null)
                        showInformationFloating = null
                    else if (showInformationFloating == null)
                        showInformationFloating = false
                }
        }

        CompositionLocalProvider(LocalTextStyle provides fontStyles[fontStyle]) {
            LazyColumn(
                modifier = Modifier
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown()
                            focusManager.clearFocus()
                        }
                    }
                    .focusGroup()
                    .windowInsetsPadding(WindowInsets.ime)
                    .width(with(density) { windowWidth.toDp() })
                    .height(with(density) { constraints.minHeight.toDp() }),
                state = listState,
                contentPadding = PaddingValues(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                itemsIndexed(hopsitext.lines, key = { _, l -> l.identifier }) { lineI, line ->
                    HopsiLine(
                        line = line,
                        onEdit = { text ->
                            val textLines = text.split(Regex("\\r?\\n"))
                            hopsitext.addNewLines(lineI, textLines)

                            if (textLines.size > 1)
                                cursorDirective.jumpTo = if (textLines.size > 2) line.text.length + 1 else 0

                            coroutineScope.launch {
                                hopsitext.performHopsiCalculation(
                                    fromLine = (lineI - 1).coerceAtLeast(0),
                                    minimumToLines = lineI + textLines.size,
                                )
                            }
                        },
                        onDeleteLine = {
                            if (lineI == 0) return@HopsiLine

                            val beforeCursor = hopsitext.lines[lineI - 1].text.length
                            hopsitext.deleteLineBreak(lineI)

                            cursorDirective.jumpTo = beforeCursor

                            coroutineScope.launch {
                                hopsitext.performHopsiCalculation(fromLine = lineI - 1, minimumToLines = lineI)
                            }
                        },
                        onMoveCursorUp = {
                            if (lineI > 0)
                                coroutineScope.launch {
                                    cursorDirective.jumpTo = hopsitext.lines[lineI - 1].text.length + 1
                                    focusManager.moveFocus(FocusDirection.Previous)
                                }
                        },
                        onMoveCursorDown = {
                            if (lineI < hopsitext.lines.size - 1)
                                coroutineScope.launch {
                                    cursorDirective.jumpTo = 0
                                    focusManager.moveFocus(FocusDirection.Next)
                                }
                        },
                        modifier = Modifier
                            .width(with(density) { editorWidth.toDp() }),
                        cursorDirective = cursorDirective,
                        placeholder = {
                            Text(
                                text = stringResource(
                                    if (hopsitext.lines.size == 1) Res.string.new_text_placeholder
                                    else Res.string.new_line_placeholder,
                                )
                            )
                        },
                        belaColor = if (showSpoilers) belaColor else Color.Unspecified,
                        amiraColor = if (showSpoilers) amiraColor else Color.Unspecified,
                        unionColor = unionColor,
                    )
                }

                item {
                    CompositionLocalProvider(LocalTextStyle provides typography.titleMedium) {
                        HopsiInformation(
                            information = hopsitext.information,
                            showSpoilers = showSpoilers,
                            onScrollToUnionLine = {
                                hopsitext.information.unionSourceLine?.let {
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(index = it, scrollOffset = -50)
                                    }
                                }
                            },
                            belaColor = belaColor,
                            amiraColor = amiraColor,
                            unionColor = unionColor,
                            modifier = Modifier
                                .padding(vertical = 60.dp)
                                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
                                .width(500.dp)
                                .border(
                                    width = 2.dp,
                                    color = colorScheme.outline,
                                    shape = shapes.extraLarge,
                                )
                                .padding(16.dp),
                        )
                    }
                }
            }
        }

        VerticalScrollbar(
            listState = listState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .height(with(density) { constraints.minHeight.toDp() }),
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            showInformationFloating?.let { show ->
                Surface(
                    onClick = { showInformationFloating = !show },
                    shape = RoundedCornerShape(100, 100, 0, 0),
                    color = colorScheme.surfaceContainerHighest,
                    shadowElevation = 8.dp,
                ) {
                    AnimatedContent(
                        targetState = show,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                    ) {
                        Icon(
                            painter = painterResource(
                                if (show) Res.drawable.bottom_panel_close
                                else Res.drawable.description,
                            ),
                            contentDescription = stringResource(Res.string.show_text_information),
                            modifier = Modifier.padding(12.dp),
                            tint =
                            if (hopsitext.information.unionSourceLine != null) colorScheme.error
                            else LocalContentColor.current,
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showInformationFloating == true,
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top),
            ) {
                Surface(
                    modifier = Modifier.width(480.dp),
                    shape = shapes.large.copy(bottomEnd = CornerSize(0f), bottomStart = CornerSize(0f)),
                    color = colorScheme.surfaceContainerHighest.copy(0.95f),
                ) {
                    HopsiInformation(
                        information = hopsitext.information,
                        showSpoilers = showSpoilers,
                        onScrollToUnionLine = {
                            hopsitext.information.unionSourceLine?.let {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(index = it, scrollOffset = -50)
                                }
                            }
                        },
                        belaColor = belaColor,
                        amiraColor = amiraColor,
                        unionColor = unionColor,
                        modifier = Modifier
                            .padding(20.dp)
                            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = hopsitext.calculationJob != null,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        AnimatedVisibility(
            visible = showSettings,
            modifier = Modifier.align(Alignment.BottomEnd),
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it },
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .width(IntrinsicSize.Min)
                    .windowInsetsPadding(insets = WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
                    .background(
                        color = colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
                        shape = shapes.large,
                    )
                    .padding(start = 8.dp, top = 8.dp, end = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row {
                    fontStyles.forEachIndexed { index, style ->
                        StyleButton(
                            style = style,
                            selected = fontStyle == index,
                            onSelect = { fontStyle = index },
                        )
                    }
                }

                val interactionSource = remember { MutableInteractionSource() }

                Slider(
                    value = editorWidth / windowWidth,
                    onValueChange = { frac ->
                        editorWidth = windowWidth * frac
                    },
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth(),
                    interactionSource = interactionSource,
                    thumb = {
                        Box(contentAlignment = Alignment.Center) {
                            SliderDefaults.Thumb(interactionSource = interactionSource)
                            Icon(
                                painter = painterResource(Res.drawable.width_arrow),
                                contentDescription = stringResource(Res.string.change_width),
                                tint = colorScheme.run { contentColorFor(primary) },
                            )
                        }
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = showSettings,
            modifier = Modifier.align(Alignment.BottomStart),
            enter = slideInHorizontally { -it },
            exit = slideOutHorizontally { -it },
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .windowInsetsPadding(insets = WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
                    .background(
                        color = colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
                        shape = shapes.large,
                    )
                    .padding(8.dp),
            ) {
                ColorSelect(
                    name = stringResource(Res.string.bela),
                    color = belaColor,
                    onChangeColor = { belaColor = it },
                )

                ColorSelect(
                    name = stringResource(Res.string.amira),
                    color = amiraColor,
                    onChangeColor = { amiraColor = it },
                )

                ColorSelect(
                    name = stringResource(Res.string.union),
                    color = unionColor,
                    onChangeColor = { unionColor = it },
                )
            }
        }
    }
}

@Composable
private fun HopsiInformation(
    information: Hopsitext.Information,
    showSpoilers: Boolean,
    onScrollToUnionLine: () -> Unit,
    belaColor: Color,
    amiraColor: Color,
    unionColor: Color,
    modifier: Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.total_characters),
                modifier = Modifier
                    .padding(end = 12.dp)
                    .weight(1f),
            )
            Text(
                text = information.totalCharacters.toString(),
                fontWeight = FontWeight.Bold,
            )
        }

        @Composable
        fun PlayerInfo(
            name: String,
            jumps: Int,
            color: Color,
        ) {
            Row(
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .fillMaxWidth()
                    .background(color = color),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.total_jumps_with_name).format(name),
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .weight(1f),
                    color = color.readableForeground(),
                )
                Text(
                    text =
                    if (showSpoilers) jumps.toString()
                    else stringResource(Res.string.spoiler_placeholder),
                    color = color.readableForeground(),
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        PlayerInfo(
            name = stringResource(Res.string.bela),
            jumps = information.belaTotalJumps,
            color = belaColor,
        )

        PlayerInfo(
            name = stringResource(Res.string.amira),
            jumps = information.amiraTotalJumps,
            color = amiraColor,
        )

        Row(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val winner =
                if (information.belaTotalJumps < information.amiraTotalJumps)
                    stringResource(Res.string.bela)
                else if (information.amiraTotalJumps < information.belaTotalJumps)
                    stringResource(Res.string.amira)
                else null

            Text(
                text = stringResource(
                    if (winner != null || !showSpoilers) Res.string.resulting_winner
                    else Res.string.no_winner,
                ),
                modifier = Modifier
                    .padding(end = 12.dp)
                    .weight(1f),
            )
            if (winner != null || !showSpoilers) Text(
                text =
                if (showSpoilers) winner!!
                else stringResource(Res.string.spoiler_placeholder),
                fontWeight = FontWeight.Bold,
            )
        }

        if (information.unionSourceLine != null)
            Text(
                text = stringResource(Res.string.no_hopsitext_with_line).format(information.unionSourceLine + 1),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(unionColor)
                    .clickable(onClick = onScrollToUnionLine),
                color = unionColor.readableForeground(),
                textDecoration = TextDecoration.Underline,
            )
        else
            Text(
                text = stringResource(Res.string.is_hopsitext),
                modifier = Modifier.fillMaxWidth()
            )
    }
}

@Composable
private fun StyleButton(
    style: TextStyle,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    TextButton(
        onClick = onSelect,
        colors = ButtonDefaults.textButtonColors(
            containerColor =
            if (selected) colorScheme.primary
            else Color.Transparent,
            contentColor =
            if (selected) colorScheme.contentColorFor(colorScheme.primary)
            else colorScheme.secondary,
        ),
    ) {
        Text(
            text = stringResource(Res.string.font_placeholder),
            style = style,
        )
    }
}

@Composable
private fun ColorSelect(
    name: String,
    color: Color,
    onChangeColor: (Color) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shapes = MaterialTheme.shapes

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val showPicker = remember { MutableTransitionState(false) }

        Text(
            text = name,
            style = typography.labelMedium,
        )

        FilledIconButton(
            onClick = { showPicker.targetState = true },
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = color,
                contentColor = color.readableForeground(),
            ),
        ) {
            Icon(
                painter = painterResource(Res.drawable.edit),
                contentDescription = stringResource(Res.string.edit_color),
            )
        }

        if (showPicker.currentState || !showPicker.isIdle) {
            Popup(
                alignment = Alignment.BottomCenter,
                onDismissRequest = { showPicker.targetState = false },
                properties = PopupProperties(focusable = true),
            ) {
                AnimatedVisibility(
                    visibleState = showPicker,
                    enter = fadeIn() + slideInVertically { it / 4 },
                    exit = fadeOut() + slideOutVertically { it / 4 },
                ) {
                    Surface(
                        modifier = Modifier.padding(8.dp),
                        shape = shapes.large,
                        color = colorScheme.surfaceContainerHighest,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .width(IntrinsicSize.Min),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            val colorPickerController = rememberColorPickerController()

                            var hexString by remember { mutableStateOf(color.toHexString()) }

                            SideEffect {
                                if (colorPickerController.selectedColor.value != color)
                                    colorPickerController.selectByColor(color, fromUser = false)
                            }

                            LaunchedEffect(Unit) {
                                colorPickerController.getColorFlow().collect {
                                    onChangeColor(it.color)
                                    if (it.fromUser)
                                        hexString = it.color.toHexString()
                                }
                            }

                            Text(
                                text = name,
                                style = typography.labelLarge,
                            )

                            HsvColorPicker(
                                modifier = Modifier.size(200.dp),
                                controller = colorPickerController,
                                initialColor = color,
                            )

                            BrightnessSlider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp),
                                controller = colorPickerController,
                                initialColor = color,
                            )

                            AlphaSlider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp),
                                controller = colorPickerController,
                                initialColor = color,
                            )

                            OutlinedTextField(
                                value = hexString,
                                onValueChange = { changed ->
                                    hexString = changed
                                    changed.toUIntOrNull(16)?.let {
                                        // Add opacity if not specified
                                        val argb = if (changed.length <= 6) it or 0xFF000000u else it
                                        onChangeColor(Color(argb.toInt()))
                                    }
                                },
                                modifier = Modifier.width(180.dp),
                                textStyle = TextStyle(fontFamily = FontFamily.Monospace),
                                prefix = { Text(text = stringResource(Res.string.color_prefix)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedPrefixColor = color,
                                    unfocusedPrefixColor = color,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}
