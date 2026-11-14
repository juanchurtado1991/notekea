package app.notekea.chat.presentation.viewmodel

import app.notekea.chat.domain.ChatError
import app.notekea.chat.domain.ChatHistoryStore
import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.ChatTurn
import app.notekea.chat.domain.CompactChatUseCase
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.agent.AgentAnswerUseCase
import app.notekea.chat.domain.agent.AgentTaskUseCase
import app.notekea.chat.domain.answer.AnswerCitations
import app.notekea.chat.domain.answer.AnswerFromFoundUseCase
import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.answer.AnswerStep
import app.notekea.chat.domain.answer.ConverseUseCase
import app.notekea.chat.domain.answer.DescribeVaultUseCase
import app.notekea.chat.domain.context.WorkContext
import app.notekea.chat.domain.help.AnswerHelpUseCase
import app.notekea.chat.domain.plan.ActOnRequestUseCase
import app.notekea.chat.domain.plan.ActionRunner
import app.notekea.chat.domain.plan.FoundRow
import app.notekea.chat.presentation.model.ChatEffect
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.chat.presentation.model.ChatState
import app.notekea.chat.presentation.model.ChatStep
import app.notekea.chat.presentation.model.ModelStatus
import app.notekea.chat.presentation.model.at
import app.notekea.chat.presentation.model.without
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.LocalCalendar
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.logging.domain.TextClipboard
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.BaseViewModel
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock

/**
 * The chat of the vault at [vaultPath]: questions answered from its notes ([answerFromNotes]) one at a time, the
 * answer shown as it is written, kept in the vault's history ([historyStore]); the model in use is looked at when the
 * chat shows, so it can offer Settings → AI model instead of a question field that cannot answer.
 */
class ChatViewModel(
    private val vaultPath: String,
    answerFromNotes: AnswerFromNotesUseCase,
    converse: ConverseUseCase,
    agentAnswer: AgentAnswerUseCase,
    answerHelp: AnswerHelpUseCase,
    describeVault: DescribeVaultUseCase,
    answerFromFound: AnswerFromFoundUseCase,
    conventions: PlatformConventions,
    actOnRequest: ActOnRequestUseCase,
    agentTask: AgentTaskUseCase,
    compactChat: CompactChatUseCase,
    actionRunner: ActionRunner,
    localCalendar: LocalCalendar,
    private val clock: Clock,
    private val historyStore: ChatHistoryStore,
    private val clipboard: TextClipboard,
    private val llmEngine: LlmEngine,
    sources: ChatSources
) : BaseViewModel<ChatState, ChatIntent, ChatEffect>(initialState = ChatState()) {
    private val logger = Logger.withTag(tag = LOG_TAG)

    private val host = object : ChatHost {
        override val state: StateFlow<ChatState> get() = this@ChatViewModel.state
        override val notes: Collection<NoteEntry> get() = this@ChatViewModel.notes
        override val activeNote: RelativePath? get() = this@ChatViewModel.activeNote
        override val working: WorkContext? get() = state.value.shared

        override fun reduce(transform: (ChatState) -> ChatState) =
            this@ChatViewModel.reduce(transform = transform)

        override fun emit(effect: ChatEffect) = this@ChatViewModel.emit(effect = effect)

        override fun launch(block: suspend () -> Unit): Job = launchGuarded(
            onError = ::logError,
            block = block
        )

        override fun update(
            id: Long,
            change: (ChatEntry) -> ChatEntry
        ) = this@ChatViewModel.update(
            id = id,
            change = change
        )

        override fun follow(
            id: Long,
            step: AnswerStep
        ) = this@ChatViewModel.follow(
            id = id,
            step = step
        )
    }

    private val replies = ChatReplies(
        vaultPath = vaultPath,
        answerFromNotes = answerFromNotes,
        converse = converse,
        agentAnswer = agentAnswer,
        answerHelp = answerHelp,
        describeVault = describeVault,
        answerFromFound = answerFromFound,
        shortcuts = HelpShortcuts.of(conventions = conventions),
        today = { actions.today() },
        todayIn = { actions.todayIn(notes = it) },
        working = { state.value.shared }
    )

    private val actions = ChatActions(
        vaultPath = vaultPath,
        actOnRequest = actOnRequest,
        actionRunner = actionRunner,
        localCalendar = localCalendar,
        clock = clock,
        host = host
    )

    private val memory = ChatMemory(compactChat = compactChat)

    private val siteSearch = ChatSiteSearch(
        vaultPath = vaultPath,
        sources = sources,
        host = host,
        onAnswered = { keeping.save() }
    )

    private val keeping = ChatKeeping(
        vaultPath = vaultPath,
        historyStore = historyStore,
        memory = memory,
        clock = clock,
        host = host
    )

    private val agent = ChatAgent(
        vaultPath = vaultPath,
        agentTask = agentTask,
        actions = actions,
        host = host
    )

    private val answering = ChatAnswering(
        memory = memory,
        actions = actions,
        agent = agent,
        replies = replies,
        host = host
    )

    private var notes: Collection<NoteEntry> = emptyList()
    private var activeNote: RelativePath? = null
    private var answer: Job? = null
    private var nextId = 0L

    init {
        launchGuarded(onError = ::logError) {
            val turns = historyStore.load(vaultPath = vaultPath)

            val entries = turns.map {
                ChatEntry.of(
                    turn = it,
                    id = nextId++
                )
            }

            if (entries.isNotEmpty()) memory.resume()
            reduce { it.copy(entries = entries + it.entries) }
        }

        checkModel()
    }

    override fun handleIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.OnQuestionChange -> reduce { it.copy(question = intent.text) }

            ChatIntent.OnSend -> send()

            ChatIntent.OnStop -> stop()

            is ChatIntent.OnScopeSelect -> reduce { it.copy(scope = intent.scope) }

            is ChatIntent.OnSourceClick -> emit(
                effect = ChatEffect.Reveal(
                    note = intent.source.note,
                    range = intent.source.range
                )
            )

            is ChatIntent.OnRetry -> retry(id = intent.id)

            ChatIntent.OnClearHistory -> clear()

            ChatIntent.OnCopyChat -> clipboard.copy(text = ChatTranscript.of(state = state.value))

            ChatIntent.OnCompact -> keeping.remember(isAsked = true)

            is ChatIntent.OnModeChoose -> reduce { it.copy(mode = intent.mode) }

            ChatIntent.OnModelSettingsOpen -> emit(effect = ChatEffect.OpenModelSettings)

            ChatIntent.OnShown -> checkModel()

            is ChatIntent.OnNotesChange -> notes = actions.holding(notes = intent.notes)

            is ChatIntent.OnActionPlace -> actions.place(intent = intent)

            is ChatIntent.OnIndexChange -> reduce { it.copy(isIndexReady = intent.isReady) }

            is ChatIntent.OnActiveNoteChange -> changeActiveNote(path = intent.path)

            is ChatIntent.OnWorkChange -> reduce { it.at(working = intent.working) }

            is ChatIntent.OnWorkRemove -> reduce { it.without(part = intent.part) }

            is ChatIntent.OnActionConfirm -> entryOf(id = intent.id)?.let {
                launchGuarded(onError = ::logError) {
                    actions.confirm(
                        entry = it,
                        notes = notes
                    )

                    keeping.save()
                }
            }

            is ChatIntent.OnActionCancel -> entryOf(id = intent.id)?.let {
                actions.cancel(entry = it)
            }

            is ChatIntent.OnActionUndo -> entryOf(id = intent.id)?.let {
                launchGuarded(onError = ::logError) {
                    actions.undo(entry = it)
                    keeping.save()
                }
            }

            is ChatIntent.OnFoundClick -> emit(effect = foundEffectOf(row = intent.row))

            is ChatIntent.OnSectionChoose -> entryOf(id = intent.id)?.sectionAsked?.let {
                offered(
                    request = SectionRequest.of(
                        section = it,
                        isWhole = intent.isWhole,
                        isEvery = intent.isEvery,
                        depth = intent.depth
                    )
                )
            }

            is ChatIntent.OnFollowUp -> offered(request = intent.text)

            is ChatIntent.OnSiteSearch, is ChatIntent.OnSitePageToggle, is ChatIntent.OnSiteClip ->
                siteSearch.on(intent = intent)?.let { answer = it }

            is ChatIntent.OnOfferedClick -> entryOf(id = intent.id)?.offered?.let {
                offered(request = it)
            }
        }
    }

    /** A request offered by a help answer: asked of the whole vault, or left in the field to finish its "…". */
    private fun offered(request: String) {
        // A button of an older turn clicked while one is answering starts no second turn beside it.
        if (state.value.isAnswering) return
        if (UNFINISHED in request) return reduce { it.copy(question = request) }

        ask(
            question = request,
            scope = ChatScope.VAULT,
            scopeName = null
        )
    }

    private fun entryOf(id: Long): ChatEntry? = state
        .value
        .entries
        .firstOrNull {
            it.id == id
        }

    override fun onCleared() {
        answer?.cancel()
        super.onCleared()
    }

    private fun send() {
        val current = state.value
        if (!current.canSend) return

        val scope = current.askedScope()

        ask(
            question = current.question.trim(),
            scope = scope,
            scopeName = current.nameOf(scope = scope)
        )

        reduce { it.copy(question = "") }
    }

    private fun retry(id: Long) {
        val failed = state
            .value
            .entries
            .firstOrNull {
                it.id == id
            } ?: return

        if (state.value.isAnswering) return

        reduce { current ->
            current.copy(
                entries = current.entries.filterNot {
                    it.id == id
                }
            )
        }

        ask(
            question = failed.question,
            scope = failed.scope,
            scopeName = failed.scopeName
        )
    }

    private fun ask(
        question: String,
        scope: ChatScope,
        scopeName: String?
    ) {
        val id = nextId++

        val entry = ChatEntry(
            id = id,
            question = question,
            scope = scope,
            step = ChatStep.PLANNING,
            answer = "",
            sources = emptyList(),
            readNotes = 0,
            scopeName = scopeName,
            outcome = null,
            error = null
        )

        reduce { it.copy(entries = it.entries + entry) }

        answer = launchGuarded(onError = {
            failed(
                id = id,
                throwable = it
            )
        }) {
            keeping.compactIfIdle()

            answering.answer(
                id = id,
                question = question,
                scope = scope
            )

            siteSearch.offer(id = id)
            keeping.save()
        }
    }

    private fun follow(
        id: Long,
        step: AnswerStep
    ) {
        update(id = id) { it.after(step = step) }

        if (step is AnswerStep.Failed) noteModelOf(error = step.error)

        val isEnd =
            step is AnswerStep.Done ||
                step == AnswerStep.NotFound ||
                step is AnswerStep.Failed

        if (isEnd) keeping.save()
    }

    /** Stops the answer being written; its text so far stays, with the citations it already holds. */
    private fun stop() {
        val entry = state
            .value
            .entries
            .lastOrNull()
            ?.takeIf { it.isAnswering } ?: return

        answer?.cancel()

        val kept = AnswerCitations.of(
            text = entry.answer,
            read = entry.sources
        )

        update(id = entry.id) {
            it.copy(
                step = ChatStep.DONE,
                answer = kept.text,
                sources = kept.sources,
                outcome = TurnOutcome.STOPPED
            )
        }

        keeping.save()
    }

    private fun clear() {
        if (state.value.isAnswering) answer?.cancel()
        keeping.clear()

        reduce {
            it.copy(
                entries = emptyList(),
                contextUsage = 0f
            )
        }
    }

    private fun checkModel() {
        launchGuarded(onError = ::logError) {
            val status = when (val ready = llmEngine.readiness()) {
                is Outcome.Success -> {
                    val info = ready.value

                    reduce {
                        it.copy(
                            isSmall = info.isSmall,
                            tier = info.tier,
                            isDeepOffered = info.hasDeepAnswer,
                            isAgentOffered = info.hasAgent
                        )
                    }

                    ModelStatus.READY
                }

                is Outcome.Failure -> statusOf(error = ready.error)
            }

            reduce { it.copy(model = status) }
        }
    }

    private fun noteModelOf(error: ChatError) {
        val model = (error as? ChatError.Model)?.error ?: return
        reduce { it.copy(model = statusOf(error = model)) }
    }

    private fun changeActiveNote(path: RelativePath?) {
        activeNote = path

        reduce {
            it.copy(
                noteName = path?.nameWithoutExtension,
                folderName = path
                    ?.parent
                    ?.takeUnless { folder -> folder.isRoot }
                    ?.name
            )
        }
    }

    private fun update(
        id: Long,
        change: (ChatEntry) -> ChatEntry
    ) = reduce { current ->
        current.copy(
            entries = current.entries.map {
                if (it.id == id) it.changedBy(change = change) else it
            }
        )
    }

    private fun failed(
        id: Long,
        throwable: Throwable
    ) {
        logError(throwable = throwable)

        follow(
            id = id,
            step = AnswerStep.Failed(error = ChatError.Model(error = LlmError.EngineFailed))
        )
    }

    private fun logError(throwable: Throwable) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }
    }

    private companion object {
        const val LOG_TAG = "ChatViewModel"
        const val UNFINISHED = "…"

        const val UNEXPECTED_ERROR_MESSAGE = "The chat failed unexpectedly"

        fun statusOf(error: LlmError): ModelStatus = when (error) {
            LlmError.NoModel -> ModelStatus.NO_MODEL
            LlmError.NotEnoughMemory -> ModelStatus.NOT_ENOUGH_MEMORY
            LlmError.EngineFailed -> ModelStatus.FAILED
        }
    }
}
