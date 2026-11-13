package app.notekea.core.presentation.designsystem.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.ChoiceState
import app.notekea.core.presentation.designsystem.components.NotekeaChoiceRow
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

private const val STUDY_TITLE = "Study"
private val STUDY_OPTIONS = listOf("Line 15", "Line 28", "Line 12", "Line 24")
private const val STUDY_CORRECT = 1
private const val STUDY_WIDTH_DP = 360

/** A study card's options: click one to see it marked right or wrong. */
@Composable
internal fun StudySection() {
    Text(
        text = STUDY_TITLE,
        style = MaterialTheme.typography.titleMedium
    )

    var chosen by remember { mutableStateOf<Int?>(value = null) }

    Column(
        modifier = Modifier.width(width = STUDY_WIDTH_DP.dp),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        STUDY_OPTIONS.forEachIndexed { index, option ->
            NotekeaChoiceRow(
                letter = "${'A' + index}",
                text = option,
                state = ChoiceState.of(
                    index = index,
                    correct = STUDY_CORRECT,
                    chosen = chosen
                ),
                onClick = { chosen = index }
            )
        }
    }
}
