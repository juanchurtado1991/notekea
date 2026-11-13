package app.notekea.core.presentation.designsystem.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.NotekeaCitationChip
import app.notekea.core.presentation.designsystem.components.fields.NotekeaPromptField
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

private const val CHAT_TITLE = "Chat"
private const val CHAT_ANSWER = "The dough takes two and a half cups of water"
private const val CHAT_SNIPPET =
    "Mix 2½ cups of warm water with the salt, then add the corn flour little by little."
private const val CHAT_PLACEHOLDER = "Ask your notes…"
private const val CHAT_SEND = "Send"
private const val CHAT_STOP = "Stop"
private const val CHAT_CITATIONS = 2
private const val PROMPT_WIDTH_DP = 360

/** The chat's pieces: an answer's citation chips, and the question field, which sends and, while running, stops. */
@Composable
internal fun ChatSection() {
    Text(
        text = CHAT_TITLE,
        style = MaterialTheme.typography.titleMedium
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = CHAT_ANSWER)

        (1..CHAT_CITATIONS).forEach {
            NotekeaCitationChip(
                number = it,
                snippet = CHAT_SNIPPET,
                onClick = {}
            )
        }
    }

    var question by remember { mutableStateOf(value = "") }
    var isRunning by remember { mutableStateOf(value = false) }

    NotekeaPromptField(
        value = question,
        onValueChange = { question = it },
        placeholder = CHAT_PLACEHOLDER,
        sendLabel = CHAT_SEND,
        stopLabel = CHAT_STOP,
        isRunning = isRunning,
        canSend = question.isNotBlank(),
        onSend = { isRunning = true },
        onStop = { isRunning = false },
        modifier = Modifier.width(width = PROMPT_WIDTH_DP.dp)
    )
}
