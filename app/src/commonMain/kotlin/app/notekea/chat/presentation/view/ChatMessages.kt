package app.notekea.chat.presentation.view

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.chat.domain.AnswerSource
import app.notekea.chat.domain.ChatError
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.chat.presentation.model.ChatPreviewData
import app.notekea.chat.presentation.model.ChatStep
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
            itemsIndexed(
                items = entries,
                key = { _, entry -> entry.id },
                contentType = { _, _ -> ENTRY }
            ) { index, entry ->
                // A thin line between turns, as between a document's sections (7B).
                if (index > 0) TurnDivider()

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
        QuestionHeading(text = entry.question)

        CompositionLocalProvider(LocalReplyLanguage provides entry.language) {
            Answer(
                entry = entry,
                isSmall = isSmall,
                onIntent = onIntent
            )
        }
    }
}

/** The question as the turn's heading, in bold over the answer (macOS redesign 7B): no bubble, no avatar. */
@Composable
private fun QuestionHeading(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme
            .typography
            .bodyMedium
            .copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface
    )
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

    // The answer reads as the note's own text under its question (7B).
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
            onChoose = { isWhole, isEvery ->
                onIntent(
                    ChatIntent.OnSectionChoose(
                        id = entry.id,
                        isWhole = isWhole,
                        isEvery = isEvery
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

@Composable
private fun TurnDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = NotekeaDimensions.SpacingL)
            .height(height = NotekeaDimensions.BorderWidth)
            .background(color = MaterialTheme.colorScheme.outlineVariant)
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
