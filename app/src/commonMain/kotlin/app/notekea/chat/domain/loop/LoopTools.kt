package app.notekea.chat.domain.loop

import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.plan.ActionRunner
import app.notekea.chat.domain.plan.PageReader
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.app.CalculateTool
import app.notekea.chat.domain.tool.app.CountTool
import app.notekea.chat.domain.tool.app.DateTool
import app.notekea.chat.domain.tool.app.ShowTools
import app.notekea.chat.domain.tool.change.ChangeTools
import app.notekea.chat.domain.tool.change.ProposeTool
import app.notekea.chat.domain.tool.lookup.FetchTool
import app.notekea.chat.domain.tool.lookup.FindTool
import app.notekea.chat.domain.tool.lookup.ReadTool
import app.notekea.chat.domain.tool.lookup.SearchTool
import app.notekea.chat.domain.tool.lookup.TasksTool
import app.notekea.chat.domain.tool.reading.DigestTool
import app.notekea.chat.domain.tool.reading.LinksTool
import app.notekea.chat.domain.tool.reading.ListTool
import app.notekea.chat.domain.tool.reading.OutlineTool
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.chat.domain.tool.work.AskUserTool
import app.notekea.chat.domain.tool.work.HandTool
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.links.LinkFinder

/**
 * The loop's toolsets as the app builds them (`docs/agent-loop.md`, "Toolsets"): what each kind of work is given,
 * every set ending in a way to ask the person and a way to end.
 */
class LoopTools(
    private val llmEngine: LlmEngine,
    private val answerFromNotes: AnswerFromNotesUseCase,
    private val noteTextCache: NoteTextCache,
    private val actionRunner: ActionRunner,
    private val pageReader: PageReader,
    private val linkFinder: LinkFinder
) {
    fun toolsets(): Toolsets {
        val search = SearchTool(answerFromNotes = answerFromNotes)
        val read = ReadTool(noteTextCache = noteTextCache)
        val outline = OutlineTool(noteTextCache = noteTextCache)
        val list = ListTool()
        val find = FindTool(actionRunner = actionRunner)
        val tasks = TasksTool(actionRunner = actionRunner)
        val ask = AskUserTool()
        val answer = AnswerTool()
        val propose = ProposeTool()

        val digest = DigestTool(
            llmEngine = llmEngine,
            noteTextCache = noteTextCache
        )

        val links = LinksTool(
            noteTextCache = noteTextCache,
            linkFinder = linkFinder
        )

        val count = CountTool(noteTextCache = noteTextCache)

        val looking = listOf(search, read, find, tasks, list)
        val shows = ShowTools.of(setTheme = { actionRunner.setTheme(theme = it) })
        val changes = ChangeTools

        return Toolsets(
            byKind = mapOf(
                WorkKind.ASK to
                    looking + listOf(outline, digest, links, CalculateTool(), count, ask, answer),
                WorkKind.WRITE to looking + listOf(
                    outline,
                    changes.writeNote,
                    changes.createNote,
                    changes.append,
                    changes.makeCards,
                    ask,
                    propose
                ),
                WorkKind.ORGANIZE to looking + organizing(
                    ask = ask,
                    ends = listOf(propose)
                ),
                // Shown at once, after looking up the note or the heading the request means.
                WorkKind.APP to looking + shows + listOf(HandTool.help, ask, answer),
                WorkKind.WEB to listOf(
                    FetchTool(pageReader = pageReader),
                    search,
                    read,
                    changes.clip,
                    ask,
                    propose,
                    answer
                )
            )
        )
    }

    // Organizing, with a note to write as it is often asked with the tasks or tags it is about.
    private fun organizing(
        ask: AskUserTool,
        ends: List<ChatTool>
    ): List<ChatTool> = listOf(
        DateTool(),
        ChangeTools.writeNote,
        ChangeTools.createNote,
        ChangeTools.append,
        ChangeTools.addTag,
        ChangeTools.setProperty,
        ChangeTools.addTask,
        ChangeTools.completeTask,
        ChangeTools.link,
        ChangeTools.move,
        ChangeTools.rename,
        ChangeTools.createFolder,
        ChangeTools.trash,
        ChangeTools.bookmark,
        ask
    ) + ends
}
