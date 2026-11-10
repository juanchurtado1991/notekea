package app.notekea.chat.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.chat.domain.AnswerSource
import app.notekea.chat.domain.ChatError
import app.notekea.chat.domain.TurnOutcome
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.NotekeaLazyScrollArea
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.LocalReduceMotion
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.core.presentation.designsystem.theme.motionTween
import app.notekea.resources.Res
import app.notekea.resources.chat_acted
import app.notekea.resources.chat_failed
import app.notekea.resources.chat_failed_search
import app.notekea.resources.chat_not_found
import app.notekea.resources.chat_retry
import app.notekea.resources.chat_small_hint
import app.notekea.resources.chat_stopped
import org.jetbrains.compose.resources.stringResource

private const val ANSWER_ALPHA = 0.14f
private const val ANSWER_INSET_DP = 24
private const val BUBBLE_RADIUS_DP = 12
private const val BUBBLE_TAIL_DP = 4
private const val BUBBLE_INSET_DP = 40
private const val RISE_DP = 12f
private const val ENTRY = "entry"

/**
 * The chat's questions and answers, oldest first, the newest kept in view while it is written. With the small model
 * ([isSmall]) an answer's sources come before it, with a line asking to check them (decided on 2026-11-08).
 */
@Composable
internal fun ChatMessages(
    entries: List<ChatEntry>,
    isSmall: Boolean,
    onIntent: (ChatIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val list = rememberLazyListState()
    val last = entries.lastOrNull()

    LaunchedEffect(entries.size, last?.answer?.length, last?.step) {
        if (entries.isNotEmpty()) {
            list.scrollToItem(
                index = entries.lastIndex,
                scrollOffset = Int.MAX_VALUE
            )
        }
    }

    NotekeaLazyScrollArea(
        state = list,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = list,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(all = NotekeaDimensions.SpacingM),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingL)
        ) {
            items(
                items = entries,
                key = { it.id },
                contentType = { ENTRY }
            ) { entry ->
                ChatEntryView(
                    entry = entry,
                    isSmall = isSmall,
                    onIntent = onIntent
                )
            }
        }
    }
}

/** One question and its answer, rising into place when it is new. */
@Composable
private fun ChatEntryView(
    entry: ChatEntry,
    isSmall: Boolean,
    onIntent: (ChatIntent) -> Unit
) {
    val reduceMotion = LocalReduceMotion.current

    val isNew = remember {
        entry.isAnswering && !reduceMotion
    }

    val rise = remember { Animatable(initialValue = if (isNew) 0f else 1f) }
    val spec = motionTween<Float>(durationMillis = NotekeaDimensions.MOTION_PANEL_MILLIS)

    LaunchedEffect(Unit) {
        rise.animateTo(
            targetValue = 1f,
            animationSpec = spec
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = rise.value
                translationY = (1f - rise.value) * RISE_DP * density
            },
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        QuestionBubble(text = entry.question)

        Answer(
            entry = entry,
            isSmall = isSmall,
            onIntent = onIntent
        )
    }
}

@Composable
private fun QuestionBubble(text: String) {
    // As in a messaging app: your own messages on the right, filled in the theme's color, the corner of the tail sharp.
    val shape = RoundedCornerShape(
        topStart = BUBBLE_RADIUS_DP.dp,
        topEnd = BUBBLE_TAIL_DP.dp,
        bottomEnd = BUBBLE_RADIUS_DP.dp,
        bottomStart = BUBBLE_RADIUS_DP.dp
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        // Room on the left, so a long question stays a bubble on the right.
        Box(modifier = Modifier.width(width = BUBBLE_INSET_DP.dp))

        Text(
            text = text,
            modifier = Modifier
                .weight(
                    weight = 1f,
                    fill = false
                )
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = shape
                )
                .padding(
                    horizontal = NotekeaDimensions.SpacingM,
                    vertical = NotekeaDimensions.SpacingS
                ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

/** The answer's bubble, on the left in the theme's accent, tail at the top: the step line, the answer, the sources. */
@Composable
private fun AnswerBubble(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(
        topStart = BUBBLE_TAIL_DP.dp,
        topEnd = BUBBLE_RADIUS_DP.dp,
        bottomEnd = BUBBLE_RADIUS_DP.dp,
        bottomStart = BUBBLE_RADIUS_DP.dp
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .weight(
                    weight = 1f,
                    fill = false
                )
                .background(
                    color = MaterialTheme
                        .colorScheme
                        .tertiary
                        .copy(alpha = ANSWER_ALPHA),
                    shape = shape
                )
                .padding(
                    horizontal = NotekeaDimensions.SpacingM,
                    vertical = NotekeaDimensions.SpacingS
                )
        ) { content() }

        // Room on the right, so the answer reads as the other side of the conversation.
        Box(modifier = Modifier.width(width = ANSWER_INSET_DP.dp))
    }
}

@Composable
private fun Answer(
    entry: ChatEntry,
    isSmall: Boolean,
    onIntent: (ChatIntent) -> Unit
) {
    val onSourceClick = { source: AnswerSource ->
        onIntent(ChatIntent.OnSourceClick(source = source))
    }

    val isWriting = entry.step == ChatStep.WRITING && entry.isAnswering

    AnswerBubble {
        AnswerContent(
            entry = entry,
            isWriting = isWriting,
            isSmall = isSmall,
            onIntent = onIntent,
            onSourceClick = onSourceClick
        )
    }
}

@Composable
private fun AnswerContent(
    entry: ChatEntry,
    isWriting: Boolean,
    isSmall: Boolean,
    onIntent: (ChatIntent) -> Unit,
    onSourceClick: (AnswerSource) -> Unit
) {
    val action = entry.action

    when {
        action != null && !entry.isAnswering -> ChatActionView(
            entryId = entry.id,
            action = action,
            onIntent = onIntent
        )

        entry.outcome == TurnOutcome.ACTED ->
            Quiet(text = stringResource(resource = Res.string.chat_acted))

        entry.isAnswering && !isWriting -> ChatStepLine(
            step = entry.step,
            readNotes = entry.readNotes
        )

        entry.outcome == TurnOutcome.NOT_FOUND -> Quiet(
            text = stringResource(resource = Res.string.chat_not_found)
        )

        entry.outcome == TurnOutcome.FAILED -> Failed(
            isSearch = entry.error == ChatError.Search,
            onRetry = { onIntent(ChatIntent.OnRetry(id = entry.id)) }
        )

        else -> Written(
            entry = entry,
            isWriting = isWriting,
            isSourcesFirst = isSmall,
            onSourceClick = onSourceClick
        )
    }
}

@Composable
private fun Written(
    entry: ChatEntry,
    isWriting: Boolean,
    isSourcesFirst: Boolean,
    onSourceClick: (AnswerSource) -> Unit
) {
    val isDone = !entry.isAnswering && entry.sources.isNotEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
        if (isSourcesFirst && isDone) {
            ChatSources(
                sources = entry.sources,
                onSourceClick = onSourceClick
            )

            Quiet(text = stringResource(resource = Res.string.chat_small_hint))
        }

        ChatAnswerText(
            text = entry.answer,
            sources = entry.sources,
            isWriting = isWriting,
            onSourceClick = onSourceClick
        )

        if (entry.outcome == TurnOutcome.STOPPED) {
            Quiet(text = stringResource(resource = Res.string.chat_stopped))
        }

        AnimatedVisibility(
            visible = !isSourcesFirst && isDone,
            enter = fadeIn(animationSpec = motionTween(NotekeaDimensions.MOTION_PANEL_MILLIS)) +
                expandVertically(animationSpec = motionTween(NotekeaDimensions.MOTION_PANEL_MILLIS))
        ) {
            ChatSources(
                sources = entry.sources,
                onSourceClick = onSourceClick
            )
        }
    }
}

@Composable
private fun Failed(
    isSearch: Boolean,
    onRetry: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = NotekeaIcons.ChatError,
                contentDescription = null,
                modifier = Modifier.size(size = NotekeaDimensions.IconSize),
                tint = MaterialTheme.colorScheme.error
            )

            val failed = if (isSearch) Res.string.chat_failed_search else Res.string.chat_failed

            Text(
                text = stringResource(resource = failed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        NotekeaButton(
            text = stringResource(resource = Res.string.chat_retry),
            onClick = onRetry,
            style = NotekeaButtonStyle.TEXT
        )
    }
}

@Composable
private fun Quiet(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview
@Composable
private fun ChatMessagesPreview() {
    NotekeaPreviewThemes {
        Box(
            modifier = Modifier.size(
                width = 320.dp,
                height = 420.dp
            )
        ) {
            ChatMessages(
                entries = ChatPreviewData.state.entries,
                isSmall = false,
                onIntent = {}
            )
        }
    }
}
