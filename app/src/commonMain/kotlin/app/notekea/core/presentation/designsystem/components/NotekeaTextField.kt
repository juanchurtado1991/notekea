package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

private const val PREVIEW_ERROR = "Choose a name without characters like / or :"
private const val PREVIEW_INVALID = "a/b"
private const val PREVIEW_PLACEHOLDER = "Search notes…"
private const val PREVIEW_VALUE = "pricing"
private val FIELD_PADDING = PaddingValues(
    horizontal = NotekeaDimensions.SpacingM,
    vertical = NotekeaDimensions.SpacingXs
)
private val SUBMIT_KEYS = setOf(Key.Enter, Key.NumPadEnter)
private val TEXT_MIN_HEIGHT = NotekeaDimensions.ControlHeight - NotekeaDimensions.SpacingXs * 2

/**
 * Single- or multi-line text input, one line as tall as the app's other controls ([NotekeaDimensions.ControlHeight]).
 * [errorText] shows a validation message under the field while keeping the typed value. [onSubmit] runs on Enter in a
 * single-line field, as a key event so it behaves the same on every platform; [isSecret] shows dots for a token.
 */
@Composable
fun NotekeaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    errorText: String? = null,
    isSecret: Boolean = false,
    onSubmit: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme

    val interaction = remember { MutableInteractionSource() }

    val shown = if (isSecret) PasswordVisualTransformation() else VisualTransformation.None

    val isError = errorText != null

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .defaultMinSize(minWidth = OutlinedTextFieldDefaults.MinWidth)
            .onPreviewKeyEvent { event ->
                val isEnterDown = event.type == KeyEventType.KeyDown && event.key in SUBMIT_KEYS

                val isSubmit =
                    singleLine &&
                        onSubmit != null &&
                        isEnterDown

                if (isSubmit) onSubmit()
                isSubmit
            },
        textStyle = MaterialTheme
            .typography
            .bodyMedium
            .copy(color = colors.onSurface),
        cursorBrush = SolidColor(value = if (isError) colors.error else colors.primary),
        singleLine = singleLine,
        visualTransformation = shown,
        interactionSource = interaction
    ) { innerField ->
        FieldDecoration(
            value = value,
            singleLine = singleLine,
            isError = isError,
            interaction = interaction,
            placeholder = placeholder,
            errorText = errorText,
            innerField = innerField
        )
    }
}

/** The outline, placeholder and error of the Material text field, around [innerField], with compact padding. */
@Composable
private fun FieldDecoration(
    value: String,
    singleLine: Boolean,
    isError: Boolean,
    interaction: MutableInteractionSource,
    placeholder: String?,
    errorText: String?,
    innerField: @Composable () -> Unit
) {
    OutlinedTextFieldDefaults.DecorationBox(
        value = value,
        innerTextField = {
            Box(
                modifier = Modifier.heightIn(min = TEXT_MIN_HEIGHT),
                contentAlignment = Alignment.CenterStart
            ) { innerField() }
        },
        enabled = true,
        singleLine = singleLine,
        visualTransformation = VisualTransformation.None,
        interactionSource = interaction,
        isError = isError,
        placeholder = placeholder?.let { text ->
            {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        supportingText = errorText?.let { text ->
            {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        contentPadding = FIELD_PADDING,
        container = {
            OutlinedTextFieldDefaults.Container(
                enabled = true,
                isError = isError,
                interactionSource = interaction,
                shape = MaterialTheme.shapes.small
            )
        }
    )
}

@Preview
@Composable
private fun NotekeaTextFieldPreview() {
    NotekeaPreviewThemes {
        Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
            NotekeaTextField(
                value = "",
                onValueChange = {},
                placeholder = PREVIEW_PLACEHOLDER
            )

            NotekeaTextField(
                value = PREVIEW_VALUE,
                onValueChange = {},
                placeholder = PREVIEW_PLACEHOLDER
            )

            NotekeaTextField(
                value = PREVIEW_INVALID,
                onValueChange = {},
                errorText = PREVIEW_ERROR
            )
        }
    }
}
