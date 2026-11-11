package app.notekea.chat.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.LocalReduceMotion
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.motionTween
import app.notekea.resources.Res
import app.notekea.resources.chat_step_composing
import app.notekea.resources.chat_step_drafting
import app.notekea.resources.chat_step_further
import app.notekea.resources.chat_step_planning
import app.notekea.resources.chat_step_reading
import app.notekea.resources.chat_step_reading_one
import app.notekea.resources.chat_step_searching
import app.notekea.resources.chat_step_writing
import org.jetbrains.compose.resources.stringResource

private const val DOTS = 3
private const val DOT_DP = 5
private const val DOT_STAGGER_MILLIS = 160
private const val DOT_PULSE_MILLIS = 480
private const val DIM_ALPHA = 0.25f

/**
 * Where an answer stands, in plain words ("Searching your notes…", "Reading 6 notes…"), after three dots in the
 * theme's color that pulse one after another, so a wait never looks frozen.
 */
@Composable
internal fun ChatStepLine(
    step: ChatStep,
    readNotes: Int,
    modifier: Modifier = Modifier
) {
    val fade = motionTween<Float>(durationMillis = NotekeaDimensions.MOTION_SHORT_MILLIS)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PulsingDots()

        AnimatedContent(
            targetState = step to readNotes,
            transitionSpec = {
                fadeIn(animationSpec = fade).togetherWith(fadeOut(animationSpec = fade))
            }
        ) { (shownStep, notes) ->
            Text(
                text = stepText(
                    step = shownStep,
                    notes = notes
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun stepText(
    step: ChatStep,
    notes: Int
): String = when {
    step == ChatStep.SEARCHING -> replyString(resource = Res.string.chat_step_searching)

    step == ChatStep.PLANNING -> replyString(resource = Res.string.chat_step_planning)

    step == ChatStep.LOOKING_FURTHER -> replyString(resource = Res.string.chat_step_further)

    step == ChatStep.COMPOSING -> replyString(resource = Res.string.chat_step_composing)

    step == ChatStep.READING && notes == 1 ->
        replyString(resource = Res.string.chat_step_reading_one)

    step == ChatStep.READING -> replyString(Res.string.chat_step_reading, notes)

    step == ChatStep.DRAFTING -> replyString(resource = Res.string.chat_step_drafting)

    else -> replyString(resource = Res.string.chat_step_writing)
}

@Composable
private fun PulsingDots() {
    val reduceMotion = LocalReduceMotion.current
    val pulse = rememberInfiniteTransition()

    Row(horizontalArrangement = Arrangement.spacedBy(space = 3.dp)) {
        repeat(times = DOTS) { index ->
            val alpha by pulse.animateFloat(
                initialValue = if (reduceMotion) 1f else DIM_ALPHA,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = DOT_PULSE_MILLIS,
                        delayMillis = index * DOT_STAGGER_MILLIS
                    ),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Row(
                modifier = Modifier
                    .size(size = DOT_DP.dp)
                    .alpha(alpha = alpha)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            ) {}
        }
    }
}

@Preview
@Composable
private fun ChatStepLinePreview() {
    NotekeaPreviewThemes {
        ChatStepLine(
            step = ChatStep.READING,
            readNotes = 6
        )
    }
}
