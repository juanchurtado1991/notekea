package app.notekea.shell.presentation.follow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import app.notekea.chat.domain.context.WorkContext
import app.notekea.chat.domain.context.WorkPlace
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.chat.presentation.viewmodel.ChatViewModel
import app.notekea.core.notes.domain.OutlineHeading
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.platform.WindowDialog
import app.notekea.editor.presentation.viewmodel.EditorViewModel
import app.notekea.shell.presentation.tabs.TabKind
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

// A selection is told once the person stops moving it, not at each key.
private const val SETTLE_MILLIS = 300L
private const val MARKDOWN = ".md"

private val PLACES = mapOf(
    TabKind.NOTE to WorkPlace.NOTE,
    TabKind.FILE to WorkPlace.FILE,
    TabKind.FOLDER to WorkPlace.FOLDER,
    TabKind.GRAPH to WorkPlace.GRAPH,
    TabKind.QUERY to WorkPlace.QUERY,
    TabKind.TASKS to WorkPlace.TASKS,
    TabKind.STUDY to WorkPlace.STUDY,
    TabKind.YOUTUBE to WorkPlace.YOUTUBE
)

private val SETTINGS = mapOf(
    WindowDialog.SETTINGS to "",
    WindowDialog.AI_MODEL to "AI model",
    WindowDialog.SYNC to "Sync"
)

/**
 * Where the person is, as the chat is told (the developer's choice of 2026-11-13): Settings when it is open, else the
 * active tab's [kind] at [path]; in a note the heading of [outline] the [cursor] is under, and the text [selected].
 */
internal fun workContextOf(
    outline: List<OutlineHeading>,
    cursor: Int,
    kind: TabKind?,
    path: RelativePath?,
    dialog: WindowDialog?,
    selected: String?
): WorkContext? {
    val settings = dialog?.let {
        SETTINGS[it]
    }

    if (settings != null) {
        return WorkContext(
            place = WorkPlace.SETTINGS,
            name = settings
        )
    }

    val place = kind?.let {
        PLACES[it]
    } ?: return null

    val isNote = place == WorkPlace.NOTE
    val isView = place !in setOf(WorkPlace.NOTE, WorkPlace.FILE, WorkPlace.FOLDER)

    val name = if (isView) {
        ""
    } else {
        path
            ?.value
            ?.removeSuffix(suffix = MARKDOWN)
            .orEmpty()
    }

    val heading = outline
        .lastOrNull {
            it.offset <= cursor
        }
        ?.text

    // The note's own title as its first heading says nothing more than its name.
    val section = heading?.takeIf {
        isNote && it != path?.nameWithoutExtension
    }

    return WorkContext(
        place = place,
        name = name,
        section = section,
        selected = selected
            ?.takeIf {
                isNote && it.isNotBlank()
            }
            ?.take(n = WorkContext.MAX_SELECTED)
    )
}

/** The chat follows where the person is: the active tab ([kind] at [path]), Settings ([dialog]), the [editor]'s
 * cursor and selection. */
@OptIn(FlowPreview::class)
@Composable
internal fun FollowWork(
    chat: ChatViewModel,
    editor: EditorViewModel,
    outline: List<OutlineHeading>,
    kind: TabKind?,
    path: RelativePath?,
    dialog: WindowDialog?
) {
    LaunchedEffect(chat, editor, kind, path, dialog, outline) {
        snapshotFlow {
            val selection = editor.buffer.selection

            val selected = editor
                .buffer
                .text
                .subSequence(selection.min, selection.max)
                .toString()

            workContextOf(
                outline = outline,
                cursor = selection.start,
                kind = kind,
                path = path,
                dialog = dialog,
                selected = selected
            )
        }
            .debounce(timeoutMillis = SETTLE_MILLIS)
            .distinctUntilChanged()
            .collect { chat.onIntent(intent = ChatIntent.OnWorkChange(working = it)) }
    }
}
