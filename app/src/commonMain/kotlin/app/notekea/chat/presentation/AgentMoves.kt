package app.notekea.chat.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.chat.domain.AgentTaskPrompt
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.chat_agent_fetch
import app.notekea.resources.chat_agent_find
import app.notekea.resources.chat_agent_read
import app.notekea.resources.chat_agent_search
import app.notekea.resources.chat_agent_tasks

/** What agent mode did, a quiet line per move, so the person sees how it reached its plan (4.7). */
@Composable
internal fun AgentMoves(moves: List<AgentMoveLine>) {
    if (moves.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
        moves.forEach { line ->
            Text(
                text = "· " + lineOf(line = line),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun lineOf(line: AgentMoveLine): String = when (line.move) {
    AgentTaskPrompt.SEARCH -> replyString(Res.string.chat_agent_search, line.target)
    AgentTaskPrompt.READ -> replyString(Res.string.chat_agent_read, line.target)
    AgentTaskPrompt.FIND -> replyString(Res.string.chat_agent_find, line.target)
    AgentTaskPrompt.FETCH -> replyString(Res.string.chat_agent_fetch, line.target)
    else -> replyString(resource = Res.string.chat_agent_tasks)
}

@Preview
@Composable
private fun AgentMovesPreview() {
    NotekeaPreviewThemes {
        AgentMoves(
            moves = listOf(
                AgentMoveLine(
                    move = AgentTaskPrompt.SEARCH,
                    target = "sleep"
                ),
                AgentMoveLine(
                    move = AgentTaskPrompt.READ,
                    target = "Atomic Habits"
                )
            )
        )
    }
}
