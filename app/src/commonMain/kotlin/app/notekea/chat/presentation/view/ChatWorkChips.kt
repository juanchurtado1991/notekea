package app.notekea.chat.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.chat.domain.context.WorkContext
import app.notekea.chat.domain.context.WorkPlace
import app.notekea.chat.presentation.model.WorkPart
import app.notekea.core.presentation.designsystem.components.NotekeaChip
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.chat_work_graph
import app.notekea.resources.chat_work_query
import app.notekea.resources.chat_work_remove
import app.notekea.resources.chat_work_selected
import app.notekea.resources.chat_work_settings
import app.notekea.resources.chat_work_study
import app.notekea.resources.chat_work_tasks
import app.notekea.resources.chat_work_youtube
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private const val SELECTED_CHARS = 24
private const val ELLIPSIS = "…"
private const val PREVIEW_WIDTH_DP = 320

private val VIEW_NAMES: Map<WorkPlace, StringResource> = mapOf(
    WorkPlace.GRAPH to Res.string.chat_work_graph,
    WorkPlace.QUERY to Res.string.chat_work_query,
    WorkPlace.TASKS to Res.string.chat_work_tasks,
    WorkPlace.STUDY to Res.string.chat_work_study,
    WorkPlace.YOUTUBE to Res.string.chat_work_youtube,
    WorkPlace.SETTINGS to Res.string.chat_work_settings
)

/**
 * Above the question field, what the chat is told of where the person is (the developer's choice of 2026-11-13): the
 * place with its section, and the text selected; each chip's × keeps it from the chat until the person moves on.
 */
@Composable
internal fun ChatWorkChips(
    working: WorkContext,
    isPlaceHidden: Boolean,
    isSelectedHidden: Boolean,
    onRemove: (WorkPart) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isPlaceHidden) return

    val removeLabel = stringResource(resource = Res.string.chat_work_remove)
    val selected = working.selected?.takeUnless { isSelectedHidden }

    FlowRow(
        modifier = modifier.padding(bottom = NotekeaDimensions.SpacingXs),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        NotekeaChip(
            label = placeOf(working = working),
            onRemove = { onRemove(WorkPart.PLACE) },
            removeLabel = removeLabel
        )

        if (selected != null) {
            val line = selected
                .lineSequence()
                .first()

            val start = if (line.length > SELECTED_CHARS) {
                line.take(n = SELECTED_CHARS).trimEnd() + ELLIPSIS
            } else {
                line
            }

            NotekeaChip(
                label = stringResource(Res.string.chat_work_selected, start),
                onRemove = { onRemove(WorkPart.SELECTED) },
                removeLabel = removeLabel
            )
        }
    }
}

// A note, a file or a folder by its name, a view by what the app calls it; the section after a dot.
@Composable
private fun placeOf(working: WorkContext): String {
    val view = VIEW_NAMES[working.place]

    val name = if (view != null) {
        stringResource(resource = view)
    } else {
        working.name.substringAfterLast(delimiter = '/')
    }

    return listOfNotNull(name, working.section).joinToString(separator = " · ")
}

@Preview
@Composable
private fun ChatWorkChipsPreview() {
    NotekeaPreviewThemes {
        ChatWorkChips(
            working = WorkContext(
                place = WorkPlace.NOTE,
                name = "Recipes/Arepas",
                section = "Ingredients",
                selected = "2 1/2 cups warm water"
            ),
            isPlaceHidden = false,
            isSelectedHidden = false,
            onRemove = {},
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)
        )
    }
}
