package app.notekea.cards.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.cards.domain.CardAnswer
import app.notekea.cards.domain.CardChoices
import app.notekea.cards.domain.StudyCard
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.components.ChoiceState
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.NotekeaCard
import app.notekea.core.presentation.designsystem.components.NotekeaChoiceRow
import app.notekea.core.presentation.designsystem.components.feedback.NotekeaProgressBar
import app.notekea.core.presentation.designsystem.components.feedback.NotekeaSkeletonRow
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaColors
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.study_continue
import app.notekea.resources.study_easy
import app.notekea.resources.study_good
import app.notekea.resources.study_hard
import app.notekea.resources.study_making
import app.notekea.resources.study_open_note
import app.notekea.resources.study_position
import app.notekea.resources.study_progress_label
import app.notekea.resources.study_right
import app.notekea.resources.study_show
import app.notekea.resources.study_wrong
import org.jetbrains.compose.resources.stringResource

private val SKELETON_WIDTHS = listOf(0.7f, 0.55f, 0.8f, 0.6f)
private const val PREVIEW_WIDTH_DP = 560

private val PREVIEW_CARD = StudyCard(
    note = RelativePath(value = "Lisbon trip.md"),
    line = 4,
    question = "Which tram climbs to Alfama?",
    answer = "Line 28"
)

/**
 * A card in the middle of the Study tab (the developer's design A of 2026-11-13): its deck and place in the sitting
 * over a progress bar, its question, then its options, or its answer on request when it has none.
 */
@Composable
internal fun StudyCardView(
    state: StudyState,
    card: StudyCard,
    onIntent: (StudyIntent) -> Unit
) {
    Header(
        deck = card.note.nameWithoutExtension,
        position = state.position,
        sitting = state.sitting
    )

    NotekeaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(all = NotekeaDimensions.SpacingXl),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingL),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = card.question,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            val choices = state.choices

            when {
                choices != null -> Options(
                    choices = choices,
                    chosen = state.chosen,
                    onChoose = { onIntent(StudyIntent.OnChoose(index = it)) }
                )

                state.isMakingChoices && !state.isShown -> Making()

                state.isShown -> {
                    HorizontalDivider()

                    Text(
                        text = card.answer,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    Buttons(
        state = state,
        onIntent = onIntent
    )
}

@Composable
private fun Header(
    deck: String,
    position: Int,
    sitting: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = deck,
                modifier = Modifier.weight(weight = 1f),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = stringResource(Res.string.study_position, position, sitting),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        NotekeaProgressBar(
            label = stringResource(resource = Res.string.study_progress_label),
            fraction = (position - 1).toFloat() / sitting.coerceAtLeast(minimumValue = 1),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Options(
    choices: CardChoices,
    chosen: Int?,
    onChoose: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
        choices.options.forEachIndexed { index, option ->
            NotekeaChoiceRow(
                letter = "${'A' + index}",
                text = option,
                state = ChoiceState.of(
                    index = index,
                    correct = choices.correct,
                    chosen = chosen
                ),
                onClick = { onChoose(index) }
            )
        }

        if (chosen != null) {
            val isRight = choices.isCorrect(chosen = chosen)

            val said = if (isRight) {
                stringResource(resource = Res.string.study_right)
            } else {
                stringResource(Res.string.study_wrong, choices.options[choices.correct])
            }

            val color = if (isRight) NotekeaColors.success else MaterialTheme.colorScheme.error

            Text(
                text = said,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun Making() {
    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
        SKELETON_WIDTHS.forEach { NotekeaSkeletonRow(widthFraction = it) }

        Text(
            text = stringResource(resource = Res.string.study_making),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Buttons(
    state: StudyState,
    onIntent: (StudyIntent) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = NotekeaDimensions.SpacingS,
            alignment = Alignment.CenterHorizontally
        )
    ) {
        when {
            state.chosen != null -> NotekeaButton(
                text = stringResource(resource = Res.string.study_continue),
                onClick = { onIntent(StudyIntent.OnNext) },
                shortcutHint = "Enter"
            )

            state.choices != null -> Unit

            state.isShown -> Grades(onIntent = onIntent)

            else -> NotekeaButton(
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
private fun Grades(onIntent: (StudyIntent) -> Unit) {
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
}

@Preview
@Composable
private fun StudyCardViewPreview() {
    NotekeaPreviewThemes {
        Column(
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingL)
        ) {
            StudyCardView(
                state = StudyState(
                    isLoading = false,
                    due = listOf(PREVIEW_CARD),
                    isMakingChoices = true
                ),
                card = PREVIEW_CARD,
                onIntent = {}
            )
        }
    }
}
