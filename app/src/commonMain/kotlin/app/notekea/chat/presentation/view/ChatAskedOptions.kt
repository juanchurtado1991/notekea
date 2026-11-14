package app.notekea.chat.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

/**
 * The answers to choose from when the chat asked back in place of guessing (`ask_user`): a button each, and the
 * one clicked is sent as the next message ([onChoose]).
 */
@Composable
internal fun ChatAskedOptions(
    options: List<String>,
    onChoose: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        options.forEach { option ->
            NotekeaButton(
                text = option,
                onClick = { onChoose(option) },
                style = NotekeaButtonStyle.SECONDARY
            )
        }
    }
}

@Preview
@Composable
private fun ChatAskedOptionsPreview() {
    NotekeaPreviewThemes {
        ChatAskedOptions(
            options = listOf("Home/Budget 2026", "Work/Budget 2026"),
            onChoose = {}
        )
    }
}
