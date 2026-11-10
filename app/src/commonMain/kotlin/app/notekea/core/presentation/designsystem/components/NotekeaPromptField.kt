package app.notekea.core.presentation.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.core.presentation.designsystem.theme.motionTween

private const val MAX_LINES = 6
private const val BUTTON_DP = 26
private const val DISABLED_ALPHA = 0.38f
private const val PREVIEW_WIDTH_DP = 300
private const val PREVIEW_PLACEHOLDER = "Ask about your notes…"

/**
 * A field for a question to a model: several lines, `Enter` sends ([onSend], when [canSend]) and `Shift+Enter` breaks
 * the line; while [isRunning] its button stops the reply ([onStop], as `Esc` does). Its border takes the theme's color
 * while it has the keyboard, and [bottom] holds what goes under the text (a scope, a mode).
 */
@Composable
fun NotekeaPromptField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    sendLabel: String,
    stopLabel: String,
    isRunning: Boolean,
    canSend: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    bottom: @Composable () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val isFocused by interaction.collectIsFocusedAsState()

    val spec = motionTween<Color>(durationMillis = NotekeaDimensions.MOTION_SHORT_MILLIS)

    val border by animateColorAsState(
        targetValue = if (isFocused) colors.primary else colors.outlineVariant,
        animationSpec = spec
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = colors.surface,
                shape = MaterialTheme.shapes.medium
            )
            .border(
                width = NotekeaDimensions.BorderWidth,
                color = border,
                shape = MaterialTheme.shapes.medium
            )
            .padding(all = NotekeaDimensions.SpacingS)
    ) {
        Box {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = placeholder }
                    .onPreviewKeyEvent { event ->
                        isPromptKey(
                            event = event,
                            canSend = canSend,
                            isRunning = isRunning,
                            onSend = onSend,
                            onStop = onStop
                        )
                    },
                textStyle = MaterialTheme
                    .typography
                    .bodyMedium
                    .copy(color = colors.onSurface),
                cursorBrush = SolidColor(value = colors.primary),
                maxLines = MAX_LINES,
                interactionSource = interaction
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = NotekeaDimensions.SpacingS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(weight = 1f)) { bottom() }
            Box(modifier = Modifier.width(width = NotekeaDimensions.SpacingS))

            PromptButton(
                isRunning = isRunning,
                canSend = canSend,
                label = if (isRunning) stopLabel else sendLabel,
                onClick = if (isRunning) onStop else onSend
            )
        }
    }
}

/** `Enter` sends (when [canSend]) and `Esc` stops while [isRunning]: whether [event] was one of them, and handled. */
private fun isPromptKey(
    event: KeyEvent,
    canSend: Boolean,
    isRunning: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit
): Boolean {
    val isEnter = event.key == Key.Enter || event.key == Key.NumPadEnter
    val isDown = event.type == KeyEventType.KeyDown

    val isSend =
        isDown &&
            isEnter &&
            !event.isShiftPressed

    val isStop =
        isDown &&
            event.key == Key.Escape &&
            isRunning

    if (isSend && canSend) onSend()
    if (isStop) onStop()

    return isSend || isStop
}

/** Send, filled in the theme's color when there is something to send, turning into Stop while a reply runs. */
@Composable
private fun PromptButton(
    isRunning: Boolean,
    canSend: Boolean,
    label: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val isEnabled = isRunning || canSend
    val background = if (isEnabled) colors.primary else colors.primary.copy(alpha = DISABLED_ALPHA)
    val hoverIcon = if (isEnabled) PointerIcon.Hand else PointerIcon.Default
    val fade = motionTween<Float>(durationMillis = NotekeaDimensions.MOTION_SHORT_MILLIS)

    NotekeaTooltip(label = label) {
        Box(
            modifier = Modifier
                .size(size = BUTTON_DP.dp)
                .background(
                    color = background,
                    shape = CircleShape
                )
                .focusProperties { canFocus = false }
                .pointerHoverIcon(icon = hoverIcon)
                .clickable(
                    enabled = isEnabled,
                    onClickLabel = label,
                    role = Role.Button,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = isRunning,
                transitionSpec = {
                    val enter = fadeIn(animationSpec = fade) + scaleIn(animationSpec = fade)
                    val exit = fadeOut(animationSpec = fade) + scaleOut(animationSpec = fade)

                    enter.togetherWith(exit)
                }
            ) { running ->
                Icon(
                    imageVector = if (running) NotekeaIcons.Stop else NotekeaIcons.Send,
                    contentDescription = label,
                    modifier = Modifier.size(size = NotekeaDimensions.IconSize),
                    tint = colors.onPrimary
                )
            }
        }
    }
}

@Preview
@Composable
private fun NotekeaPromptFieldPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NotekeaPromptField(
                value = "",
                onValueChange = {},
                placeholder = PREVIEW_PLACEHOLDER,
                sendLabel = "Send",
                stopLabel = "Stop",
                isRunning = false,
                canSend = false,
                onSend = {},
                onStop = {}
            )

            NotekeaPromptField(
                value = "How much water goes into the dough?",
                onValueChange = {},
                placeholder = PREVIEW_PLACEHOLDER,
                sendLabel = "Send",
                stopLabel = "Stop",
                isRunning = true,
                canSend = false,
                onSend = {},
                onStop = {}
            )
        }
    }
}
