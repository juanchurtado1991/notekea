package app.notekea.chat.eval

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.agent.ProposalCheck
import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.loop.LoopTools
import app.notekea.chat.domain.loop.RunTurnUseCase
import app.notekea.chat.domain.plan.ActionRunner
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.plan.PageProblem
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.TextEmbedder
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.TextFolder
import app.notekea.core.web.domain.WebSearchProblem
import app.notekea.search.eval.EvalVault
import kotlin.time.Clock

/** The one loop (`docs/agent-loop.md`, step 5) over an evaluation vault, with the real models. */
internal object LoopEval {
    fun loopOf(
        engine: LlmEngine,
        embedder: TextEmbedder,
        indexed: EvalVault,
        runner: ActionRunner
    ): RunTurnUseCase {
        val cache = NoteTextCache(
            contentStore = indexed.store,
            textFolder = TextFolder()
        )

        val tools = LoopTools(
            llmEngine = engine,
            answerFromNotes = AnswerFromNotesUseCase(embedder, indexed.index, cache, engine),
            noteTextCache = cache,
            actionRunner = runner,
            pageReader = { Outcome.Failure(error = PageProblem.OFFLINE) },
            linkFinder = JetBrainsMarkdownLinkFinder(),
            webSearch = { _, _ -> Outcome.Failure(error = WebSearchProblem.OFFLINE) }
        )

        return RunTurnUseCase(
            llmEngine = engine,
            toolsets = tools.toolsets(),
            vaultGuide = VaultGuide(noteTextCache = cache),
            proposal = ProposalCheck(
                checkPlan = CheckPlanUseCase(),
                composeNotes = ComposeNotesUseCase(engine, cache, GhostActionPlanReader())
            ),
            clock = Clock.System
        )
    }
}
