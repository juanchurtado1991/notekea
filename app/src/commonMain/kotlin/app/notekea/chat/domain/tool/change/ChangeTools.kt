package app.notekea.chat.domain.tool.change

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind

/**
 * The changes the loop may propose, each one of 3.9's actions under the name and with the arguments the model is
 * given (`docs/agent-loop.md`, "The tools"): the app carries them out after the card's Do it, as it always did.
 */
object ChangeTools {
    val writeNote = ChangeTool(
        name = "write_note",
        description = "a new note written from notes you read: its title, the notes to write it from, and in " +
            "text one sentence saying what it must hold (a table of…, a summary of…). Never the note's own " +
            "text: the app writes it afterwards from those notes.",
        kind = ActionKind.WRITE_FROM_NOTES,
        fields = listOf(ActionField.TITLE, ActionField.NOTES, ActionField.TEXT, ActionField.FOLDER),
        optional = setOf(ActionField.FOLDER)
    )

    val createNote = ChangeTool(
        name = "create_note",
        description = "a new empty note, or one from a template: its title, and the folder or the template " +
            "when the task names one. Never a note the task says what to hold: that is write_note.",
        kind = ActionKind.CREATE_NOTE,
        fields = listOf(ActionField.TITLE, ActionField.FOLDER, ActionField.TEMPLATE),
        optional = setOf(ActionField.FOLDER, ActionField.TEMPLATE),
        misuse = ChangeMisuse::emptyNote
    )

    val append = ChangeTool(
        name = "append",
        description = "text added at the end of a note: the note and the text, as it will be written. Never a " +
            "task (add_task), a tag (add_tag) or a property (set_property).",
        kind = ActionKind.APPEND_TEXT,
        fields = listOf(ActionField.NOTE, ActionField.TEXT),
        misuse = ChangeMisuse::taskAppended
    )

    val makeCards = ChangeTool(
        name = "make_cards",
        description = "study cards made from a note and added to it: the note.",
        kind = ActionKind.MAKE_CARDS,
        fields = listOf(ActionField.NOTE)
    )

    val addTag = ChangeTool(
        name = "add_tag",
        description = "a tag added to notes: the notes and the tag.",
        kind = ActionKind.ADD_TAG,
        fields = listOf(ActionField.NOTES, ActionField.TAG)
    )

    val setProperty = ChangeTool(
        name = "set_property",
        description = "a property set on notes: the notes, the property as the vault names it, and its value " +
            "(a status, a priority, a date: never complete_task for them).",
        kind = ActionKind.SET_PROPERTY,
        fields = listOf(ActionField.NOTES, ActionField.PROPERTY, ActionField.VALUE),
        misuse = ChangeMisuse::tagAsProperty
    )

    val addTask = ChangeTool(
        name = "add_task",
        description = "one task added to a note: the note, the task's text, and due as a date from the date " +
            "tool when the task has a day. Call it once for each task. Never a task marked done (complete_task).",
        kind = ActionKind.ADD_TASK,
        fields = listOf(ActionField.NOTE, ActionField.TEXT, ActionField.DUE),
        optional = setOf(ActionField.DUE)
    )

    val completeTask = ChangeTool(
        name = "complete_task",
        description = "a task marked done: its note and the task's text as the tasks tool gave it. Never a " +
            "task added (add_task) or a status set (set_property).",
        kind = ActionKind.COMPLETE_TASK,
        fields = listOf(ActionField.NOTE, ActionField.TEXT),
        misuse = ChangeMisuse::doneUnasked
    )

    val link = ChangeTool(
        name = "link",
        description = "a link from one note to another: the note that gets the link, and the target it leads to.",
        kind = ActionKind.LINK_NOTES,
        fields = listOf(ActionField.NOTE, ActionField.TARGET)
    )

    val move = ChangeTool(
        name = "move",
        description = "notes moved into a folder: the notes, and the target folder.",
        kind = ActionKind.MOVE,
        fields = listOf(ActionField.NOTES, ActionField.TARGET)
    )

    val rename = ChangeTool(
        name = "rename",
        description = "a note renamed: the note, and its new title.",
        kind = ActionKind.RENAME,
        fields = listOf(ActionField.NOTE, ActionField.TITLE)
    )

    val createFolder = ChangeTool(
        name = "create_folder",
        description = "a new folder: its name or path.",
        kind = ActionKind.CREATE_FOLDER,
        fields = listOf(ActionField.FOLDER)
    )

    val bookmark = ChangeTool(
        name = "bookmark",
        description = "a note added to the bookmarks: the note.",
        kind = ActionKind.BOOKMARK,
        fields = listOf(ActionField.NOTE)
    )

    val trash = ChangeTool(
        name = "trash",
        description = "notes moved to the trash: the notes.",
        kind = ActionKind.TRASH,
        fields = listOf(ActionField.NOTES)
    )

    val clip = ChangeTool(
        name = "clip",
        description = "a web page saved into the vault's web sources: its address, only one the task itself wrote.",
        kind = ActionKind.CLIP_URL,
        fields = listOf(ActionField.URL)
    )
}
