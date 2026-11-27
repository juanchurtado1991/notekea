package app.notekea.chat.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.chat.domain.tool.lookup.FetchTool
import app.notekea.chat.domain.tool.lookup.FindTool
import app.notekea.chat.domain.tool.lookup.ReadTool
import app.notekea.chat.domain.tool.lookup.SearchTool
import app.notekea.chat.domain.tool.web.WebSearchTool
import app.notekea.chat.presentation.model.AgentMoveLine
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatPreviewData
import app.notekea.chat.presentation.model.ChatStep
import app.notekea.core.memory.domain.MemoryLines
import app.notekea.core.presentation.designsystem.components.feedback.NotekeaStatusRing
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.chat_agent_fetch
import app.notekea.resources.chat_agent_find
import app.notekea.resources.chat_agent_memory
import app.notekea.resources.chat_agent_read
import app.notekea.resources.chat_agent_search
import app.notekea.resources.chat_agent_tasks
import app.notekea.resources.chat_agent_web_search
import app.notekea.resources.chat_step_composing
import app.notekea.resources.chat_step_planning
import app.notekea.resources.chat_step_working
import app.notekea.resources.chat_step_writing

private const val MARK_DP = 12
private const val ELLIPSIS = "…"

/**
 * What an answer did and is doing, as a list of tasks (the developer's smoke of 2026-11-13): each step it left and
 * each move agent mode made with a check, and the step it is on with a turning ring, so a wait never looks frozen.
 */
@Composable
internal fun ChatTasks(
    entry: ChatEntry,
    onMemoryForget: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isGoing = entry.isAnswering && entry.step != ChatStep.DONE

    val hasNone = entry.passed.isEmpty() && entry.moves.isEmpty()
    val isEmpty = hasNone && !isGoing
    if (isEmpty) return

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        entry.passed.forEach { TaskLine(text = textOf(step = it).removeSuffix(suffix = ELLIPSIS)) }

        entry.moves.forEach { line ->
            if (line.move == MemoryLines.MOVE) {
                ChatMemoryStep(
                    text = textOf(line = line),
                    onForget = { onMemoryForget(line.target) }
                )
            } else {
                TaskLine(text = textOf(line = line))
            }
        }

        if (isGoing) {
            TaskLine(
                text = textOf(step = entry.step),
                isDone = false
            )
        }
    }
}

@Composable
private fun TaskLine(
    text: String,
    isDone: Boolean = true
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(size = MARK_DP.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    imageVector = NotekeaIcons.Checked,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                NotekeaStatusRing(fraction = null)
            }
        }

        val color = if (isDone) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onSurface
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun textOf(step: ChatStep): String = when (step) {
    ChatStep.PLANNING -> replyString(resource = Res.string.chat_step_planning)
    ChatStep.COMPOSING -> replyString(resource = Res.string.chat_step_composing)
    ChatStep.WORKING -> replyString(resource = Res.string.chat_step_working)
    ChatStep.WRITING, ChatStep.DONE -> replyString(resource = Res.string.chat_step_writing)
}

@Composable
private fun textOf(line: AgentMoveLine): String = when (line.move) {
    SearchTool.NAME -> replyString(Res.string.chat_agent_search, line.target)
    ReadTool.NAME -> replyString(Res.string.chat_agent_read, line.target)
    FindTool.NAME -> replyString(Res.string.chat_agent_find, line.target)
    FetchTool.NAME -> replyString(Res.string.chat_agent_fetch, line.target)
    MemoryLines.MOVE -> replyString(Res.string.chat_agent_memory, line.target)
    WebSearchTool.NAME -> replyString(Res.string.chat_agent_web_search, line.target)
    else -> replyString(resource = Res.string.chat_agent_tasks)
}

@Preview
@Composable
private fun ChatTasksPreview() {
    NotekeaPreviewThemes {
        ChatTasks(
            entry = ChatPreviewData.writing.copy(
                step = ChatStep.WORKING,
                passed = listOf(ChatStep.PLANNING),
                moves = listOf(
                    AgentMoveLine(
                        move = SearchTool.NAME,
                        target = "sleep"
                    ),
                    AgentMoveLine(
                        move = ReadTool.NAME,
                        target = "Atomic Habits"
                    )
                )
            )
        )
    }
}
