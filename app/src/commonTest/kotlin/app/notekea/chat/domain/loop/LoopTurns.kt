package app.notekea.chat.domain.loop

import app.notekea.chat.data.GhostModelJson
import app.notekea.chat.data.GhostTextNotesReader
import app.notekea.chat.domain.agent.ProposalCheck
import app.notekea.chat.domain.context.StepMemory
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.plan.WebPageClipper
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.notes.domain.NoteTextCache
import kotlin.time.Clock

/**
 * The loop as its tests run it: [engine] answers, [cache] holds the notes and [toolsets] are the test's own; the
 * guide, the proposal's checks and the clock are the app's. What a test varies besides is named: [frames],
 * [stepMemory], [wholeLast], and the [clipper] a proposal's clips are read with.
 */
fun loopTurn(
    engine: LlmEngine,
    cache: NoteTextCache,
    toolsets: Toolsets,
    frames: FrameSource? = null,
    stepMemory: StepMemory? = null,
    wholeLast: Int? = null,
    clipper: WebPageClipper? = null
): RunTurnUseCase = RunTurnUseCase(
    llmEngine = engine,
    toolsets = toolsets,
    vaultGuide = VaultGuide(noteTextCache = cache),
    proposal = ProposalCheck(
        checkPlan = CheckPlanUseCase(),
        composeNotes = ComposeNotesUseCase(
            llmEngine = engine,
            noteTextCache = cache,
            notesReader = GhostTextNotesReader(),
            clipper = clipper
        )
    ),
    clock = Clock.System,
    modelJson = GhostModelJson(),
    stepMemory = stepMemory,
    wholeLast = wholeLast,
    frames = frames
)
