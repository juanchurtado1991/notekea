package app.notekea.cards.presentation

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.cards.domain.CardAnswer
import app.notekea.cards.domain.CardChoices
import app.notekea.cards.domain.StudyCard
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.readableDate
import app.notekea.resources.Res
import app.notekea.resources.study_done
import app.notekea.resources.study_empty
import app.notekea.resources.study_next
import app.notekea.resources.study_title
import org.jetbrains.compose.resources.stringResource

private const val CARD_MAX_WIDTH_DP = 620
private val ANSWER_KEYS =
    mapOf(Key.One to CardAnswer.HARD, Key.Two to CardAnswer.GOOD, Key.Three to CardAnswer.EASY)
private val CHOICE_KEYS = listOf(Key.One, Key.Two, Key.Three, Key.Four)
private val NEXT_KEYS = setOf(Key.Enter, Key.NumPadEnter, Key.Spacebar)

/**
 * The Study tab (4.6): the card due first ([StudyCardView]), its options chosen with 1 to 4 and Enter for the next, or
 * Show answer (Space) then Hard, Good, Easy (1, 2, 3); with none due, when the next comes back or how to make some.
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

                current != null -> StudyCardView(
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
    val intent = when {
        state.current == null -> null

        state.chosen != null -> StudyIntent.OnNext.takeIf {
            key in NEXT_KEYS
        }

        state.choices != null ->
            CHOICE_KEYS
                .indexOf(element = key)
                .takeIf {
                    it >= 0
                }
                ?.let { StudyIntent.OnChoose(index = it) }

        state.isShown -> ANSWER_KEYS[key]?.let { StudyIntent.OnAnswer(answer = it) }

        key == Key.Spacebar -> StudyIntent.OnReveal

        else -> null
    } ?: return false

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
                reviewed = 2,
                isShown = true,
                total = 6,
                choices = CardChoices(
                    options = listOf("Line 15", "Line 28", "Line 12", "Line 24"),
                    correct = 1
                ),
                chosen = 2
            ),
            onIntent = {}
        )
    }
}
