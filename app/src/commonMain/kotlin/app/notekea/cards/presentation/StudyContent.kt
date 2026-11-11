package app.notekea.cards.presentation

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.cards.domain.CardAnswer
import app.notekea.cards.domain.StudyCard
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.NotekeaCard
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.readableDate
import app.notekea.resources.Res
import app.notekea.resources.study_done
import app.notekea.resources.study_easy
import app.notekea.resources.study_empty
import app.notekea.resources.study_good
import app.notekea.resources.study_hard
import app.notekea.resources.study_next
import app.notekea.resources.study_open_note
import app.notekea.resources.study_progress
import app.notekea.resources.study_show
import app.notekea.resources.study_title
import org.jetbrains.compose.resources.stringResource

private const val CARD_MAX_WIDTH_DP = 620
private val ANSWER_KEYS =
    mapOf(Key.One to CardAnswer.HARD, Key.Two to CardAnswer.GOOD, Key.Three to CardAnswer.EASY)

/**
 * The Study tab (4.6): the card due first, its question, Show answer (Space), then its answer with Hard, Good and Easy
 * (1, 2, 3); how many are left; and, with none due, when the next comes back or how to make some.
 */
@Composable
fun StudyContent(
    state: StudyState,
    onIntent: (StudyIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val focus = remember { FocusRequester() }

    LaunchedEffect(state.current) {
        runCatching { focus.requestFocus() }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester = focus)
            .focusable()
            .onKeyEvent { event ->
                val isDown = event.type == KeyEventType.KeyDown
                isDown && keyOf(key = event.key, state = state, onIntent = onIntent)
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = CARD_MAX_WIDTH_DP.dp)
                .padding(all = NotekeaDimensions.SpacingXl),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingL)
        ) {
            Text(
                text = stringResource(resource = Res.string.study_title),
                style = MaterialTheme.typography.titleLarge
            )

            val current = state.current

            when {
                state.isLoading -> Unit

                current != null -> Card(
                    state = state,
                    card = current,
                    onIntent = onIntent
                )

                else -> Quiet(text = emptyText(state = state))
            }
        }
    }
}

@Composable
private fun Card(
    state: StudyState,
    card: StudyCard,
    onIntent: (StudyIntent) -> Unit
) {
    val progress =
        stringResource(Res.string.study_progress, state.due.size, card.note.nameWithoutExtension)

    Quiet(text = progress)

    NotekeaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(all = NotekeaDimensions.SpacingXl),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingL),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = card.question,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            if (state.isShown) {
                HorizontalDivider()

                Text(
                    text = card.answer,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = NotekeaDimensions.SpacingS,
            alignment = Alignment.CenterHorizontally
        )
    ) {
        if (state.isShown) {
            NotekeaButton(
                text = stringResource(resource = Res.string.study_hard),
                onClick = { onIntent(StudyIntent.OnAnswer(CardAnswer.HARD)) },
                style = NotekeaButtonStyle.SECONDARY,
                shortcutHint = "1"
            )

            NotekeaButton(
                text = stringResource(resource = Res.string.study_good),
                onClick = { onIntent(StudyIntent.OnAnswer(CardAnswer.GOOD)) },
                shortcutHint = "2"
            )

            NotekeaButton(
                text = stringResource(resource = Res.string.study_easy),
                onClick = { onIntent(StudyIntent.OnAnswer(CardAnswer.EASY)) },
                style = NotekeaButtonStyle.SECONDARY,
                shortcutHint = "3"
            )
        } else {
            NotekeaButton(
                text = stringResource(resource = Res.string.study_show),
                onClick = { onIntent(StudyIntent.OnReveal) },
                shortcutHint = "Space"
            )
        }

        NotekeaButton(
            text = stringResource(resource = Res.string.study_open_note),
            onClick = { onIntent(StudyIntent.OnOpenNote) },
            style = NotekeaButtonStyle.TEXT
        )
    }
}

@Composable
private fun emptyText(state: StudyState): String {
    val next = state.nextDate

    return when {
        state.total == 0 -> stringResource(resource = Res.string.study_empty)

        // A date as people read it, not the date's code form (bug hunt, 2026-11-11).
        next != null -> stringResource(
            Res.string.study_next,
            state.reviewed,
            readableDate(date = next)
        )

        else -> stringResource(resource = Res.string.study_done, state.reviewed)
    }
}

private fun keyOf(
    key: Key,
    state: StudyState,
    onIntent: (StudyIntent) -> Unit
): Boolean {
    val answer = ANSWER_KEYS[key]

    val intent = when {
        key == Key.Spacebar &&
            !state.isShown &&
            state.current != null -> StudyIntent.OnReveal

        answer != null && state.isShown -> StudyIntent.OnAnswer(answer = answer)

        else -> return false
    }

    onIntent(intent)

    return true
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
private fun StudyContentPreview() {
    NotekeaPreviewThemes {
        StudyContent(
            state = StudyState(
                isLoading = false,
                due = listOf(
                    StudyCard(
                        note = RelativePath(value = "Lisbon trip.md"),
                        line = 4,
                        question = "Which tram climbs to Alfama?",
                        answer = "Line 28"
                    )
                ),
                isShown = true,
                total = 6
            ),
            onIntent = {}
        )
    }
}
