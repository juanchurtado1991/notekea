package app.notekea.chat.eval

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.data.SqliteStepMemory
import app.notekea.chat.domain.agent.ProposalCheck
import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.loop.FrameSource
import app.notekea.chat.domain.loop.LoopTools
import app.notekea.chat.domain.loop.RunTurnUseCase
import app.notekea.chat.domain.loop.StepRecall
import app.notekea.chat.domain.loop.TaskFramer
import app.notekea.chat.domain.loop.TurnFrames
import app.notekea.chat.domain.plan.ActionRunner
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.plan.PageProblem
import app.notekea.chat.domain.plan.PageReader
import app.notekea.chat.domain.plan.WebPageClipper
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.TextEmbedder
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.TextFolder
import app.notekea.core.web.domain.WebSearchProblem
import app.notekea.core.web.domain.WebSearchProvider
import app.notekea.search.eval.EvalVault
import kotlin.time.Clock

/** The one loop (`docs/agent-loop.md`, step 5) over an evaluation vault, with the real models; offline by default. */
internal object LoopEval {
    fun loopOf(
        engine: LlmEngine,
        embedder: TextEmbedder,
        indexed: EvalVault,
        runner: ActionRunner,
        pageReader: PageReader = OFFLINE_PAGES,
        webSearch: WebSearchProvider = OFFLINE_SEARCH,
        clipper: WebPageClipper? = null,
        trace: EvalTrace? = null
    ): RunTurnUseCase {
        val cache = NoteTextCache(
            contentStore = indexed.store,
            textFolder = TextFolder
        )

        val answerFromNotes = AnswerFromNotesUseCase(embedder, indexed.index, cache, engine)
        val vaultGuide = VaultGuide(noteTextCache = cache)

        val tools = LoopTools(
            llmEngine = engine,
            answerFromNotes = answerFromNotes,
            noteTextCache = cache,
            actionRunner = runner,
            pageReader = pageReader,
            linkFinder = JetBrainsMarkdownLinkFinder(),
            webSearch = webSearch
        )

        return RunTurnUseCase(
            llmEngine = engine,
            toolsets = tools.toolsets(),
            vaultGuide = vaultGuide,
            proposal = ProposalCheck(
                checkPlan = CheckPlanUseCase(),
                composeNotes = ComposeNotesUseCase(engine, cache, GhostActionPlanReader(), clipper)
            ),
            clock = Clock.System,
            stepMemory = SqliteStepMemory().takeIf {
                System.getProperty("notekea.stepmemory") != "off"
            },
            // -PstepMemory=fold shows only the last results whole, to measure folding again; it is off
            // (docs/agent-step-memory.md).
            wholeLast = StepRecall.WHOLE_LAST.takeIf {
                System.getProperty("notekea.stepmemory") == "fold"
            },
            frames = framesOf(engine, answerFromNotes, vaultGuide, trace)
        )
    }

    // The app's frames, kept by the trace when there is one so a report can say each task's frame.
    private fun framesOf(
        engine: LlmEngine,
        answerFromNotes: AnswerFromNotesUseCase,
        vaultGuide: VaultGuide,
        trace: EvalTrace?
    ): FrameSource {
        val frames = TurnFrames(
            framer = TaskFramer(llmEngine = engine),
            answerFromNotes = answerFromNotes,
            vaultGuide = vaultGuide
        )

        return trace?.framed(source = frames) ?: frames
    }

    private val OFFLINE_PAGES = PageReader { Outcome.Failure(error = PageProblem.OFFLINE) }
    private val OFFLINE_SEARCH = WebSearchProvider { _, _ ->
        Outcome.Failure(error = WebSearchProblem.OFFLINE)
    }
}
