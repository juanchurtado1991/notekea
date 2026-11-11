package app.notekea.chat.domain

import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole

/**
 * What the model is given to turn a request into a plan (3.9): the fixed list of actions, what the vault is made of
 * ([ActionContext], names only, never the notes' text, so a note can never give orders) and the request; the reply is
 * JSON shaped by [SCHEMA].
 */
object ActionPrompt {
    const val MAX_STEPS = 5

    private const val LONG_REQUEST = 1_200
    private const val REQUEST_START = 500

    private val FIELDS = ActionField
        .entries
        .filter {
            it != ActionField.NOTES
        }
        .joinToString(separator = ",") { "\"${it.written}\":{\"type\":\"string\"}" }

    private val ACTIONS = ActionKind.entries.joinToString(separator = ",") { "\"${it.written}\"" }

    /** The plan's JSON: up to [MAX_STEPS] steps, each an action of the list and the fields it fills. */
    val SCHEMA =
        "{\"type\":\"object\",\"properties\":{\"steps\":{\"type\":\"array\",\"minItems\":1,\"maxItems\":$MAX_STEPS," +
            "\"items\":{\"type\":\"object\",\"properties\":{\"action\":{\"type\":\"string\",\"enum\":[$ACTIONS]}," +
            "$FIELDS,\"notes\":{\"type\":\"array\",\"maxItems\":50,\"items\":{\"type\":\"string\"}}}," +
            "\"required\":[\"action\"]}}},\"required\":[\"steps\"]}"

    private const val INSTRUCTIONS =
        """You turn a request to a notes app into a plan of 1 to 5 steps, as JSON. Use as few steps as the request
needs: never add a step to find or open a note before acting on it; name the note in the step that acts.
Actions (fill only the fields named; a note is named by its title, a folder by its path):
- answer: any question about what the notes say, or a request to explain, summarize or list what a note says, in the
  chat. Fields: none. When the request is a question, the plan is only answer.
- find_notes: a list of notes chosen by the values of their properties (rating, status, finished, dates, author).
  request: the request.
- show_tasks: show tasks. folder, tag, period (overdue, today, this week, later, no date, done): optional.
- list_untagged: list notes without tags. list_broken_links: list links to notes that do not exist.
- open_note: note, and text: a heading to go to (optional). open_today: today's daily note.
- search: text, the words to find in the notes.
- show_panel: panel: files, outline, related, backlinks, chat, graph, tasks, query or bookmarks.
- split_pane, close_other_tabs, toggle_source (the note as written in Markdown). set_theme: theme.
- create_note: title, folder (optional), template (optional, a template's name), text: what to write in it (optional).
- write_from_notes: a new note written from other notes (a summary, a comparison, a list, a plan). title, text: what
  to write, and the notes to read: notes, or folder, or tag, or note ("this note" for the open one).
- notes_from_text: the request holds a pasted text to turn into notes. folder: a short name for them, in the
  language of the text.
- append_text: note (empty for today's daily note), text: the text to add.
- set_property: property, value, and note, or notes, or folder.
- add_tag: tag (a tag, never a property), and note, or notes, or folder.
- add_task: text, note (empty for today's daily note), due (YYYY-MM-DD, optional). complete_task: note, text.
- link_notes: note, target: the note to link to. bookmark: note or folder.
- create_folder: folder. rename: note or folder, title: the new name. move: note, notes or folder, target: the
  folder to move to. trash: note, notes or folder.
- refuse: deleting for good, Settings, models, files outside the vault, or writing code. text: why, in one sentence.
Use "this note" for the open note. Dates are YYYY-MM-DD. Keep names, tags and quoted text exactly as the request
writes them, never translated.
Examples:
"How long does the dough rest?" -> [answer]
"summarize this note" -> [answer]
"list the ingredients of the soup" -> [answer]
"summarize what my notes say about sleep" -> [answer]
"when is the trip?" -> [answer]
"books I rated 4 or more" -> [find_notes request="books I rated 4 or more"]
"what is due this week?" -> [show_tasks period="this week"]
"open Dune" -> [open_note note="Dune"]
"open today's note" -> [open_today]
"show the backlinks" -> [show_panel panel="backlinks"]
"show my bookmarks" -> [show_panel panel="bookmarks"]
"find my notes about Lisbon" -> [search text="Lisbon"]
"create a note Ideas in Projects" -> [create_note title="Ideas" folder="Projects"]
"create a note Call with Ana in Meetings from the Meeting template" -> [create_note title="Call with Ana"
  folder="Meetings" template="Meeting"]
"make a note in Recipes called Flan with a simple recipe" -> [create_note title="Flan" folder="Recipes"
  text="a simple recipe"]
"write a note Plan from this note" -> [write_from_notes title="Plan" text="a plan" note="this note"]
"summarize my meetings in a note Meetings summary" -> [write_from_notes title="Meetings summary" text="a summary"
  folder="Meetings"]
"add 'call Ana' to today's note" -> [append_text note="" text="call Ana"]
"tag Dune and Sapiens as favorite" -> [add_tag tag="favorite" notes=["Dune","Sapiens"]]
"mark Dune as finished" -> [set_property note="Dune" property="finished" value="true"]
"add the task 'paint' to Kitchen for 2026-12-01" -> [add_task note="Kitchen" text="paint" due="2026-12-01"]
"mark the task 'buy flour' in Arepas as done" -> [complete_task note="Arepas" text="buy flour"]
"bookmark Home" -> [bookmark note="Home"]
"bookmark the folder Projects" -> [bookmark folder="Projects"]
"añade la etiqueta viaje a Lisboa" -> [add_tag tag="viaje" note="Lisboa"]
"rename the folder Ideas to Drafts" -> [rename folder="Ideas" title="Drafts"]
"move every note in Guides to Archive" -> [move folder="Guides" target="Archive"]"""

    fun messages(
        request: String,
        context: ActionContext
    ): List<ChatMessage> = listOf(
        ChatMessage(
            role = ChatRole.SYSTEM,
            text = INSTRUCTIONS
        ),
        ChatMessage(
            role = ChatRole.USER,
            text = "${context.described()}\n\nRequest: ${shortened(request = request)}"
        )
    )

    /**
     * [request] as the plan needs it: a long text pasted with it is cut to its start, said to go on, so the plan has
     * room; the step that writes the notes reads it whole.
     */
    private fun shortened(request: String): String {
        if (request.length <= LONG_REQUEST) return request

        val words = request.split(Regex(pattern = "\\s+")).size

        val rest = " … [the pasted text goes on, $words words in all]"

        return request.take(n = REQUEST_START) + rest
    }
}
