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

    // A plan listing a whole folder note by note repeated names until it was cut (the chat probe, 2026-11-11).
    private const val MAX_NOTES = 12
    private const val LONG_REQUEST = 1_200
    private const val RETRIED = "Notekea can do this request: plan it, do not refuse it."
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
            "$FIELDS,\"notes\":{\"type\":\"array\",\"maxItems\":$MAX_NOTES,\"items\":{\"type\":\"string\"}}}," +
            "\"required\":[\"action\"]}}},\"required\":[\"steps\"]}"

    private const val INSTRUCTIONS =
        """You turn a request to a notes app into a plan of 1 to 5 steps, as JSON. Use as few steps as the request
needs: never add a step to find or open a note before acting on it; name the note in the step that acts.
Actions (fill only the fields named; a note is named by its title, a folder by its path):
- answer: any question about what the notes say, or a request to explain, summarize or list what a note says, in the
  chat. request: the question rewritten to stand alone, naming what "it", "that" or "the trip" is from the chat so
  far. When the request is a question, the plan is only answer.
- chat: a greeting, thanks, small talk, or a request for your opinion, advice or ideas, not for what the notes say;
  also a request to say your last reply again another way (in another language, shorter, simpler). note: the note
  it is about, if any ("this note" for the open one).
- help: how to use Notekea or what you can do: "how do I…", "how can I…", "what can you do", or a complaint that
  your last reply did not do or explain it right. Never create, change or open anything for these. request: what
  they want to know how to do, in English, standing alone.
- overview: what the notes or the vault hold as a whole ("what is in my notes?", "explain my notes").
- find_notes: a list of notes chosen by the values of their properties (rating, status, finished, dates, author),
  never a question about a topic or what a note says. request: the request.
- show_tasks: show tasks. folder, tag, period (overdue, today, this week, later, no date, done): optional.
- list_untagged: list notes without tags. list_broken_links: list links to notes that do not exist.
- open_note: note, and text: a heading to go to (optional). open_today: today's daily note.
- search: text, the words to find in the notes.
- show_panel: panel: files, outline, related, backlinks, chat, graph, tasks, query or bookmarks.
- split_pane, close_other_tabs, toggle_source (the note as written in Markdown). set_theme: theme.
- create_note: title, folder (optional), template (optional, a template's name), text: what to write in it (optional).
- write_from_notes: a new note written from other notes (a summary, a comparison, a list, a plan). title, text: what
  to write, and the notes to read: notes, or folder, or tag, or note ("this note" for the open one).
- make_cards: study cards (flashcards) made from a note and added to it. note ("this note" for the open one).
- clip_url: url: a web address (http or https) written in the request, to save that page as a note with notes
  about it. Only an address the request writes, never one you know or guess.
- notes_from_text: the request holds a pasted text to turn into notes. folder: a short name for them, in the
  language of the text.
- append_text: note (empty for today's daily note), text: the text to add.
- set_property: property, value, and note, or notes, or folder.
- add_tag: tag (a tag, never a property), and note, or notes, or folder.
- add_task: text, note (empty for today's daily note), due (YYYY-MM-DD, optional). complete_task: note, text.
- link_notes: note, target: the note to link to. bookmark: note or folder.
- create_folder: folder. rename: note or folder, title: the new name. move: note, notes or folder, target: the
  folder to move to. trash: note, notes or folder.
- refuse: only when the request asks for one of these, which Notekea cannot do: deleting for good (a plain
  "delete" or "borrar" is trash, never refused), Settings,
  models, files outside the vault, writing code, sending emails or messages, reminders, alarms or notifications,
  the calendar, searching the web, the weather or the news. Never for a question about the notes, a follow-up, or
  any action above. text: in one sentence in the person's language, that Notekea cannot do it and what it can do
  instead (a task with a due date for a reminder, the trash for deleting, a pasted address to clip a page).
A request about every note of a folder ("all my recipes") names the folder, never its notes one by one.
Use "this note" for the open note. Dates are YYYY-MM-DD. Keep names, tags and quoted text exactly as the request
writes them, never translated.
Examples:
"How long does the dough rest?" -> [answer request="How long does the arepa dough rest?"]
"and the water?" after a question about the arepas -> [answer request="How much water do the arepas take?"]
"when is it due?" after a question about the Notekea launch -> [answer request="When is the Notekea launch due?"]
"hola" -> [chat]
"Notekea es una porquería, no sirve para nada" -> [chat]
"what's 2 + 2?" -> [chat]
"¿qué día es hoy?" -> [chat]
"escribe un poema sobre mis recetas" -> [answer request="Escribe un poema sobre mis recetas"]
"tell me a joke about my notes" -> [answer request="Tell me a joke about my notes"]
"delete the note Pancakes" -> [trash note="Pancakes"]
"what can't you do?" -> [help request="What can't Notekea do?"]
"how many notes do I have?" -> [overview]
"en español" -> [chat]
"¿cómo creo una nota nueva?" -> [help request="how do I create a note?"]
"what can you do?" -> [help request="what can you do?"]
"así no se crea una nota" -> [help request="how do I create a note?"]
"how do I see my tasks?" -> [help request="how do I see my tasks?"]
"¿qué hay en mis notas?" -> [overview]
"what do you think of this note?" -> [chat note="this note"]
"¿qué opinas del viaje a Lisboa?" -> [chat note="Lisbon trip"]
"summarize this note" -> [answer]
"list the ingredients of the soup" -> [answer]
"summarize what my notes say about sleep" -> [answer]
"when is the trip?" -> [answer]
"explain the reading challenge" -> [answer request="explain the reading challenge"]
"what books are in my reading list?" -> [answer request="what books are in my reading list?"]
"books I rated 4 or more" -> [find_notes request="books I rated 4 or more"]
"what is due this week?" -> [show_tasks period="this week"]
"what tasks are open?" -> [show_tasks]
"open Dune" -> [open_note note="Dune"]
"open today's note" -> [open_today]
"show the backlinks" -> [show_panel panel="backlinks"]
"show my bookmarks" -> [show_panel panel="bookmarks"]
"find my notes about Lisbon" -> [search text="Lisbon"]
"which notes link to Dune?" -> [search text="Dune"]
"¿cuál de mis proyectos está más atrasado?" -> [find_notes request="projects by due date"]
"create a note Ideas in Projects" -> [create_note title="Ideas" folder="Projects"]
"create a note Call with Ana in Meetings from the Meeting template" -> [create_note title="Call with Ana"
  folder="Meetings" template="Meeting"]
"make a note in Recipes called Flan with a simple recipe" -> [create_note title="Flan" folder="Recipes"
  text="a simple recipe"]
"create a note Budget in Work, status todo" -> [create_note title="Budget" folder="Work"], [set_property
  note="Budget" property="status" value="todo"]
"write a note Plan from this note" -> [write_from_notes title="Plan" text="a plan" note="this note"]
"summarize my meetings in a note Meetings summary" -> [write_from_notes title="Meetings summary" text="a summary"
  folder="Meetings"]
"add 'call Ana' to today's note" -> [append_text note="" text="call Ana"]
"make study cards from this note" -> [make_cards note="this note"]
"hazme tarjetas de estudio de Sapiens" -> [make_cards note="Sapiens"]
"clip https://example.com/sleep-and-memory" -> [clip_url url="https://example.com/sleep-and-memory"]
"guarda este artículo: https://example.org/pan" -> [clip_url url="https://example.org/pan"]
"tag Dune and Sapiens as favorite" -> [add_tag tag="favorite" notes=["Dune","Sapiens"]]
"remind me tomorrow to call Ana" -> [refuse text="Notekea cannot send reminders; I can add a task 'call Ana' due
  tomorrow, which shows in Today."]
"mark Dune as finished" -> [set_property note="Dune" property="finished" value="true"]
"add the task 'paint' to Kitchen for 2026-12-01" -> [add_task note="Kitchen" text="paint" due="2026-12-01"]
"mark the task 'buy flour' in Arepas as done" -> [complete_task note="Arepas" text="buy flour"]
"bookmark Home" -> [bookmark note="Home"]
"bookmark the folder Projects" -> [bookmark folder="Projects"]
"añade la etiqueta viaje a Lisboa" -> [add_tag tag="viaje" note="Lisboa"]
"rename the folder Ideas to Drafts" -> [rename folder="Ideas" title="Drafts"]
"move every note in Guides to Archive" -> [move folder="Guides" target="Archive"]
"tag every recipe with food and move them all to Archive" -> [add_tag folder="Recipes" tag="food"], [move
  folder="Recipes" target="Archive"]
"pon todas las recetas en una carpeta Cocina y etiquetalas con cocina" -> [add_tag folder="Recipes" tag="cocina"],
  [move folder="Recipes" target="Cocina"]"""

    /**
     * The plan asked for [request]; [isRetried] says Notekea can do it, after a refusal of something it can do. Both
     * the person's language and that are said before the request, so neither is read as part of it (the chat probe,
     * 2026-11-11: "and the water?" was answered as "Write a refusal explaining why…").
     */
    fun messages(
        request: String,
        context: ActionContext,
        isRetried: Boolean = false
    ): List<ChatMessage> = listOf(
        ChatMessage(
            role = ChatRole.SYSTEM,
            text = INSTRUCTIONS
        ),
        ChatMessage(
            role = ChatRole.USER,
            text = listOfNotNull(
                context.described(),
                context.language?.let { "The person writes in $it." },
                RETRIED.takeIf { isRetried },
                "Request: ${shortened(request = request)}"
            ).joinToString(separator = "\n\n")
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
