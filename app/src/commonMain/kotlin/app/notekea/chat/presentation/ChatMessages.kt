package app.notekea.chat.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import app.notekea.resources.chat_do_it_for_me
import app.notekea.resources.chat_failed
import app.notekea.resources.chat_failed_search
import app.notekea.resources.chat_not_found
import app.notekea.resources.chat_retry
import app.notekea.resources.chat_small_hint
import app.notekea.resources.chat_stopped
import org.jetbrains.compose.resources.stringResource

private const val BUBBLE_RADIUS_DP = 12

// As tall as a line of the answer, so the mark sits beside its first line.
private const val AVATAR_DP = 20
private const val AVATAR_ICON_DP = 12
private const val AVATAR_ALPHA = 0.14f
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

        CompositionLocalProvider(LocalReplyLanguage provides entry.language) {
            Answer(
                entry = entry,
                isSmall = isSmall,
                onIntent = onIntent
            )
        }
    }
}

@Composable
private fun QuestionBubble(text: String) {
    // As in a messaging app: your own messages on the right in a quiet bubble, the corner of the tail sharp.
    val shape = RoundedCornerShape(
        topStart = BUBBLE_RADIUS_DP.dp,
        topEnd = BUBBLE_RADIUS_DP.dp,
        bottomEnd = BUBBLE_TAIL_DP.dp,
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
                    color = MaterialTheme.colorScheme.surface,
                    shape = shape
                )
                .border(
                    width = NotekeaDimensions.BorderWidth,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = shape
                )
                .padding(
                    horizontal = NotekeaDimensions.SpacingM,
                    vertical = NotekeaDimensions.SpacingS
                ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** The answer on the page itself, beside the assistant's small mark: the step line, the answer, its sources. */
@Composable
private fun AnswerBubble(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        Box(
            modifier = Modifier
                .size(size = AVATAR_DP.dp)
                .background(
                    color = MaterialTheme
                        .colorScheme
                        .primary
                        .copy(alpha = AVATAR_ALPHA),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = NotekeaIcons.ChatAssistant,
                contentDescription = null,
                modifier = Modifier.size(size = AVATAR_ICON_DP.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Box(modifier = Modifier.weight(weight = 1f)) { content() }
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
        Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
            AgentMoves(moves = entry.moves)

            AnswerContent(
                entry = entry,
                isWriting = isWriting,
                isSmall = isSmall,
                onIntent = onIntent,
                onSourceClick = onSourceClick
            )
        }
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
            Quiet(text = replyString(resource = Res.string.chat_acted))

        entry.isAnswering && !isWriting -> ChatStepLine(
            step = entry.step,
            readNotes = entry.readNotes
        )

        entry.outcome == TurnOutcome.NOT_FOUND -> Quiet(
            text = replyString(resource = Res.string.chat_not_found)
        )

        entry.outcome == TurnOutcome.FAILED -> Failed(
            isSearch = entry.error == ChatError.Search,
            onRetry = { onIntent(ChatIntent.OnRetry(id = entry.id)) }
        )

        entry.sectionAsked != null -> ChatSectionQuestion(
            section = entry.sectionAsked,
            onChoose = { isWhole ->
                onIntent(
                    ChatIntent.OnSectionChoose(
                        id = entry.id,
                        isWhole = isWhole
                    )
                )
            }
        )

        else -> Written(
            entry = entry,
            isWriting = isWriting,
            isSourcesFirst = isSmall,
            onSourceClick = onSourceClick,
            onOfferedClick = { onIntent(ChatIntent.OnOfferedClick(id = entry.id)) }
        )
    }
}

@Composable
private fun Written(
    entry: ChatEntry,
    isWriting: Boolean,
    isSourcesFirst: Boolean,
    onSourceClick: (AnswerSource) -> Unit,
    onOfferedClick: () -> Unit
) {
    val isDone = !entry.isAnswering && entry.sources.isNotEmpty()
    val isOffered = !entry.isAnswering && entry.offered != null

    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
        if (isSourcesFirst && isDone) {
            ChatSources(
                sources = entry.sources,
                onSourceClick = onSourceClick
            )

            Quiet(text = replyString(resource = Res.string.chat_small_hint))
        }

        ChatAnswerText(
            text = entry.answer,
            sources = entry.sources,
            isWriting = isWriting,
            onSourceClick = onSourceClick
        )

        if (entry.outcome == TurnOutcome.STOPPED) {
            Quiet(text = replyString(resource = Res.string.chat_stopped))
        }

        if (isOffered) {
            NotekeaButton(
                text = replyString(resource = Res.string.chat_do_it_for_me),
                onClick = onOfferedClick,
                style = NotekeaButtonStyle.TEXT
            )
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
                text = replyString(resource = failed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        NotekeaButton(
            text = replyString(resource = Res.string.chat_retry),
            onClick = onRetry,
            style = NotekeaButtonStyle.TEXT
        )
    }
}

@Composable
private fun Quiet(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
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
