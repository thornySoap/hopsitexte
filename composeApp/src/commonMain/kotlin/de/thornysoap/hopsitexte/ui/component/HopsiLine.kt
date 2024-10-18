package de.thornysoap.hopsitexte.ui.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import de.thornysoap.hopsitexte.model.Hopsitext
import de.thornysoap.hopsitexte.ui.theme.AmiraDefault
import de.thornysoap.hopsitexte.ui.theme.BelaDefault
import de.thornysoap.hopsitexte.ui.theme.UnionDefault
import de.thornysoap.hopsitexte.ui.util.readableForeground
import de.thornysoap.hopsitexte.ui.util.rememberCompositionAwareKeys
import de.thornysoap.hopsitexte.ui.util.rememberValueWrapper

@Composable
fun HopsiLine(
    line: Hopsitext.Line,
    onEdit: (value: String) -> Unit,
    onDeleteLine: () -> Unit,
    onMoveCursorUp: () -> Unit,
    onMoveCursorDown: () -> Unit,
    modifier: Modifier = Modifier,
    cursorDirective: CursorDirective = CursorDirective(),
    placeholder: (@Composable () -> Unit)? = null,
    belaColor: Color = BelaDefault,
    amiraColor: Color = AmiraDefault,
    unionColor: Color = UnionDefault,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes
    val compositionAwareKeys = rememberCompositionAwareKeys()
    val hoverInteractionSource = remember { MutableInteractionSource() }

    var focussed by remember { mutableStateOf(false) }

    var lastCursorPosition by rememberValueWrapper(0)
    var textFieldState by remember(cursorDirective.jumpTo) {
        mutableStateOf(
            // Respect `cursorDirective`
            TextFieldValue(line.text).copy(selection = TextRange(
                cursorDirective.jumpTo?.takeIf { focussed }
                    ?.also { cursorDirective.jumpTo = null }
                    ?: lastCursorPosition
            )),
        )
    }
    SideEffect { lastCursorPosition = textFieldState.selection.start }
    val annotatedText = remember(line, belaColor, amiraColor, unionColor) {
        line.annotatedText(belaColor, amiraColor, unionColor)
    }
    // Joining the state (i.e. cursor position etc.) with the colored text
    val textFieldValue = textFieldState.copy(annotatedString = annotatedText)

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = {
            textFieldState = it
            if (it.text != textFieldValue.text)
                onEdit(it.text)
        },
        modifier = modifier
            .onFocusChanged { focussed = it.hasFocus }
            .onPreviewKeyEvent {
                if (it.key == Key.Backspace &&
                    it.type == KeyEventType.KeyDown &&
                    textFieldState.selection.max == 0
                )
                    onDeleteLine()
                else if ((it.key == Key.DirectionUp || it.key == compositionAwareKeys.DirectionStart) &&
                    it.type == KeyEventType.KeyDown &&
                    textFieldState.selection.start == 0
                )
                    onMoveCursorUp()
                else if ((it.key == Key.DirectionDown || it.key == compositionAwareKeys.DirectionEnd) &&
                    it.type == KeyEventType.KeyDown &&
                    textFieldState.selection.start == textFieldState.text.length
                )
                    onMoveCursorDown()
                else return@onPreviewKeyEvent false
                return@onPreviewKeyEvent true
            }
            .clip(shape = shapes.medium)
            .hoverable(interactionSource = hoverInteractionSource)
            .indication(
                interactionSource = hoverInteractionSource,
                indication = if (!focussed) LocalIndication.current else null,
            ),
        placeholder = placeholder,
        shape = shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedPlaceholderColor = Color.Transparent,
            unfocusedPlaceholderColor = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            focusedBorderColor = if (line.unionSource) colorScheme.error else Color.Unspecified,
            unfocusedBorderColor = if (line.unionSource) colorScheme.error else Color.Transparent,
        ),
    )
}

private fun Hopsitext.Line.annotatedText(
    belaColor: Color,
    amiraColor: Color,
    unionColor: Color,
): AnnotatedString {
    val belaStyles = belaJumps
        .filter { it in text.indices }
        .mapTo(mutableListOf()) { index ->
            AnnotatedString.Range(
                SpanStyle(
                    color = belaColor.readableForeground(),
                    background = belaColor,
                ),
                index, index + 1,
            )
        }
    val amiraStyles = amiraJumps
        .filter { it in text.indices }
        .mapTo(mutableListOf()) { index ->
            AnnotatedString.Range(
                SpanStyle(
                    color = amiraColor.readableForeground(),
                    background = amiraColor,
                ),
                index, index + 1,
            )
        }
    val unionStyles = unionJumps
        .filter { it in text.indices }
        .map { index ->
            AnnotatedString.Range(
                SpanStyle(
                    color = unionColor.readableForeground(),
                    background = unionColor,
                ),
                index, index + 1,
            )
        }

    if (unionSource) {
        belaStyles.removeLastOrNull()?.let {
            belaStyles.add(it.copy(item = it.item.copy(fontWeight = FontWeight.Bold)))
        }
        amiraStyles.removeLastOrNull()?.let {
            amiraStyles.add(it.copy(item = it.item.copy(fontWeight = FontWeight.Bold)))
        }
    }

    return AnnotatedString(text, spanStyles = belaStyles + amiraStyles + unionStyles)
}

/**
 * @property jumpTo set cursor to specified index
 */
class CursorDirective {
    var jumpTo: Int? by mutableStateOf(null)
}
