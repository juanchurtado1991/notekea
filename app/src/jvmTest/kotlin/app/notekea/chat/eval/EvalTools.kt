package app.notekea.chat.eval

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionField.DUE
import app.notekea.chat.domain.plan.ActionField.FOLDER
import app.notekea.chat.domain.plan.ActionField.NOTE
import app.notekea.chat.domain.plan.ActionField.NOTES
import app.notekea.chat.domain.plan.ActionField.PANEL
import app.notekea.chat.domain.plan.ActionField.PERIOD
import app.notekea.chat.domain.plan.ActionField.PROPERTY
import app.notekea.chat.domain.plan.ActionField.REQUEST
import app.notekea.chat.domain.plan.ActionField.TAG
import app.notekea.chat.domain.plan.ActionField.TARGET
import app.notekea.chat.domain.plan.ActionField.TEMPLATE
import app.notekea.chat.domain.plan.ActionField.TEXT
import app.notekea.chat.domain.plan.ActionField.THEME
import app.notekea.chat.domain.plan.ActionField.TITLE
import app.notekea.chat.domain.plan.ActionField.URL
import app.notekea.chat.domain.plan.ActionField.VALUE
import app.notekea.chat.domain.plan.ActionKind

/**
 * An action of today's planner written as the loop's tools will be (`docs/agent-loop.md`): a name, a description
 * and the [fields] it takes, so step 0 can ask the model for the same choice in each format.
 */
internal data class EvalTool(
    val kind: ActionKind,
    val description: String,
    val fields: List<ActionField> = emptyList()
) {
    val name: String get() = kind.written

    /** The JSON schema of a call: the [fields] and no other, after [leading] (the tool's name, in our JSON). */
    fun schema(leading: String? = null): String {
        val properties = fields.map {
            if (it == NOTES) NOTES_SCHEMA else "\"${it.written}\":{\"type\":\"string\"}"
        }

        val named = listOfNotNull(leading?.let { "\"${EvalTools.NAMED}\"" })
        val required = named + kind.needs.map { "\"${it.written}\"" }

        return "{\"type\":\"object\",\"properties\":{" +
            (listOfNotNull(leading) + properties).joinToString(separator = ",") +
            "},\"required\":[" +
            required.joinToString(separator = ",") +
            "],\"additionalProperties\":false}"
    }

    private companion object {
        const val NOTES_SCHEMA =
            "\"notes\":{\"type\":\"array\",\"maxItems\":12,\"items\":{\"type\":\"string\"}}"
    }
}

/** Today's 33 actions as tools, and the ways to give a set of them to the model. */
internal object EvalTools {
    val ALL = listOf(
        EvalTool(
            kind = ActionKind.ANSWER,
            description = "Answer in the chat a question about what the notes say, or explain, summarize or " +
                "list what a note says. request: the question, standing alone.",
            fields = listOf(REQUEST)
        ),
        EvalTool(
            kind = ActionKind.FIND_NOTES,
            description = "List notes chosen by the values of their properties (rating, status, finished, " +
                "dates, author), never by topic. request: the request.",
            fields = listOf(REQUEST)
        ),
        EvalTool(
            kind = ActionKind.WRITE_FROM_NOTES,
            description = "Write a new note from other notes or documents (a summary, a comparison, a table, " +
                "a plan). title; text: what to write; the notes to read: notes, folder, tag or note.",
            fields = listOf(TITLE, TEXT, NOTES, FOLDER, TAG, NOTE)
        ),
        EvalTool(
            kind = ActionKind.HELP,
            description = "Explain how to use Notekea or what it can do (\"how do I…\", \"what can you do\"). " +
                "Never does the thing. request: what they want to know how to do, in English.",
            fields = listOf(REQUEST)
        ),
        EvalTool(
            kind = ActionKind.SHOW_TASKS,
            description = "Show tasks. folder, tag and period (overdue, today, this week, later, no date, " +
                "done) are optional.",
            fields = listOf(FOLDER, TAG, PERIOD)
        ),
        EvalTool(
            kind = ActionKind.CHAT,
            description = "Reply to a greeting, thanks, small talk, or a request for your opinion, advice or " +
                "ideas; also to say your last reply another way, or a question about what you did. note: the " +
                "note it is about, if any.",
            fields = listOf(NOTE)
        ),
        EvalTool(
            kind = ActionKind.OVERVIEW,
            description = "Say what the notes or the vault hold as a whole (\"what is in my notes?\", \"how " +
                "many notes do I have?\")."
        ),
        EvalTool(
            kind = ActionKind.LIST_UNTAGGED,
            description = "List the notes without tags."
        ),
        EvalTool(
            kind = ActionKind.LIST_BROKEN_LINKS,
            description = "List the links to notes that do not exist."
        ),
        EvalTool(
            kind = ActionKind.OPEN_NOTE,
            description = "Open a note. text: a heading to go to, optional.",
            fields = listOf(NOTE, TEXT)
        ),
        EvalTool(
            kind = ActionKind.OPEN_TODAY,
            description = "Open today's daily note."
        ),
        EvalTool(
            kind = ActionKind.SEARCH,
            description = "Find words in the notes, the notes about a topic or the notes linking to a note. " +
                "text: the words to find.",
            fields = listOf(TEXT)
        ),
        EvalTool(
            kind = ActionKind.SHOW_PANEL,
            description = "Show a panel: files, outline, related, backlinks, chat, graph, tasks, query or " +
                "bookmarks.",
            fields = listOf(PANEL)
        ),
        EvalTool(
            kind = ActionKind.SPLIT_PANE,
            description = "Split the editor in two panes."
        ),
        EvalTool(
            kind = ActionKind.CLOSE_OTHER_TABS,
            description = "Close every tab but the current one."
        ),
        EvalTool(
            kind = ActionKind.TOGGLE_SOURCE,
            description = "Show the note as written in Markdown, or back."
        ),
        EvalTool(
            kind = ActionKind.SET_THEME,
            description = "Change the color theme.",
            fields = listOf(THEME)
        ),
        EvalTool(
            kind = ActionKind.CREATE_NOTE,
            description = "Create a note. title; folder, template and text (what to write in it) are optional.",
            fields = listOf(TITLE, FOLDER, TEMPLATE, TEXT)
        ),
        EvalTool(
            kind = ActionKind.MAKE_CARDS,
            description = "Make study cards (flashcards) from a note and add them to it.",
            fields = listOf(NOTE)
        ),
        EvalTool(
            kind = ActionKind.CLIP_URL,
            description = "Save a web page as a note. url: an http or https address the request writes.",
            fields = listOf(URL)
        )
    ) + EvalChangeTools.ALL

    /** The name of a call in our JSON, as today's plan names it. */
    const val NAMED = "action"

    const val SYSTEM =
        "You work in Notekea, a notes app. Call the one tool that does what the person asks. A note is named " +
            "by its title, a folder by its path; use \"this note\" for the open note. Dates are YYYY-MM-DD. " +
            "Keep names, tags and quoted text exactly as the request writes them, never translated."

    /** [tools] as the model's own format takes them (`tools` of the request). */
    fun native(tools: List<EvalTool>): String = tools
        .joinToString(
            separator = ",",
            prefix = "[",
            postfix = "]"
        ) {
            "{\"type\":\"function\",\"function\":{\"name\":\"${it.name}\",\"description\":" +
                "\"${it.description.replace(oldValue = "\"", newValue = "\\\"")}\",\"parameters\":${it.schema()}}}"
        }

    /** One call in our JSON: the name of one of [tools], then only that tool's fields. */
    fun call(tools: List<EvalTool>): String = tools.joinToString(
        separator = ",",
        prefix = "{\"anyOf\":[",
        postfix = "]}"
    ) { it.schema(leading = "\"$NAMED\":{\"const\":\"${it.name}\"}") }

    /** [tools] as lines of a prompt, for our JSON: what the model's own format writes by itself. */
    fun listed(tools: List<EvalTool>): String = tools
        .joinToString(
            separator = "\n",
            prefix = "Reply with JSON: \"$NAMED\", the tool, and its fields.\nTools:\n"
        ) { tool ->
            val fields = tool.fields.joinToString {
                it.written + if (it in tool.kind.needs) "" else "?"
            }

            "- ${tool.name}($fields): ${tool.description}"
        }
}

/** The tools that change the vault, and the refusal. */
private object EvalChangeTools {
    val ALL = listOf(
        EvalTool(
            kind = ActionKind.NOTES_FROM_TEXT,
            description = "Turn a long text pasted in the request into notes. folder: a short name for them.",
            fields = listOf(FOLDER)
        ),
        EvalTool(
            kind = ActionKind.APPEND_TEXT,
            description = "Add text to the end of a note. note: empty for today's daily note.",
            fields = listOf(NOTE, TEXT)
        ),
        EvalTool(
            kind = ActionKind.SET_PROPERTY,
            description = "Set a property of a note, of several notes or of every note in a folder.",
            fields = listOf(PROPERTY, VALUE, NOTE, NOTES, FOLDER)
        ),
        EvalTool(
            kind = ActionKind.ADD_TAG,
            description = "Add a tag (never a property) to a note, to several notes or to every note in a folder.",
            fields = listOf(TAG, NOTE, NOTES, FOLDER)
        ),
        EvalTool(
            kind = ActionKind.ADD_TASK,
            description = "Add a task to a note (empty for today's daily note). due: YYYY-MM-DD, optional.",
            fields = listOf(TEXT, NOTE, DUE)
        ),
        EvalTool(
            kind = ActionKind.COMPLETE_TASK,
            description = "Mark a task of a note as done. text: the task.",
            fields = listOf(NOTE, TEXT)
        ),
        EvalTool(
            kind = ActionKind.LINK_NOTES,
            description = "Link a note to another. target: the note to link to.",
            fields = listOf(NOTE, TARGET)
        ),
        EvalTool(
            kind = ActionKind.BOOKMARK,
            description = "Bookmark a note or a folder.",
            fields = listOf(NOTE, FOLDER)
        ),
        EvalTool(
            kind = ActionKind.CREATE_FOLDER,
            description = "Create a folder.",
            fields = listOf(FOLDER)
        ),
        EvalTool(
            kind = ActionKind.RENAME,
            description = "Rename a note or a folder. title: the new name.",
            fields = listOf(NOTE, FOLDER, TITLE)
        ),
        EvalTool(
            kind = ActionKind.MOVE,
            description = "Move a note, several notes or a folder's notes. target: the folder to move to.",
            fields = listOf(NOTE, NOTES, FOLDER, TARGET)
        ),
        EvalTool(
            kind = ActionKind.TRASH,
            description = "Move a note, several notes or a folder to the trash; a plain \"delete\" is this.",
            fields = listOf(NOTE, NOTES, FOLDER)
        ),
        EvalTool(
            kind = ActionKind.REFUSE,
            description = "Only for what Notekea cannot do: deleting for good, settings, models, files " +
                "outside the vault, code, emails, reminders, the calendar, the web, the weather, the news. " +
                "text: one sentence saying so and what it can do instead.",
            fields = listOf(TEXT)
        )
    )
}
