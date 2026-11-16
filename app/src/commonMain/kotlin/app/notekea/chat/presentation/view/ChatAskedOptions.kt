package app.notekea.chat.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.chat.domain.agent.Remembering
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.fields.NotekeaCheckbox
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.chat_remember_choice
import app.notekea.resources.chat_remember_where

/**
 * The answers to a question back (`ask_user`), a button each, the one clicked sent as the next message ([onChoose]).
 * When it asked which note a name means ([remembering]), a checkbox keeps the answer in the chat's memory
 * ([onRemember]), the tick its approval (`docs/agent-memory.md`).
 */
@Composable
internal fun ChatAskedOptions(
    options: List<String>,
    onChoose: (String) -> Unit,
    onRemember: (String) -> Unit,
    remembering: Remembering?,
    modifier: Modifier = Modifier
) {
    // Ticked only while the question waits for its answer: a view's own state, never the chat's.
    var isRemembered by remember(remembering) { mutableStateOf(value = false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
        ) {
            options.forEach { option ->
                // A remembered answer is kept before it is asked, so the next turn already reads it.
                fun choose() {
                    val line = remembering?.lineFor(option = option)
                    if (isRemembered && line != null) onRemember(line)
                    onChoose(option)
                }

                NotekeaButton(
                    text = option,
                    onClick = { choose() },
                    style = NotekeaButtonStyle.SECONDARY
                )
            }
        }

        if (remembering != null) {
            NotekeaCheckbox(
                checked = isRemembered,
                onClick = { isRemembered = !isRemembered },
                label = replyString(resource = Res.string.chat_remember_choice)
            )

            Text(
                text = replyString(Res.string.chat_remember_where, remembering.said),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
private fun ChatAskedOptionsPreview() {
    NotekeaPreviewThemes {
        ChatAskedOptions(
            options = listOf("Laura Castro", "Laura Méndez"),
            remembering = Remembering(
                said = "Laura",
                notes = listOf(RelativePath(value = "People/Laura Castro.md"))
            ),
            onChoose = {},
            onRemember = {}
        )
    }
}
