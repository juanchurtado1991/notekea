package app.notekea.chat.presentation

import app.notekea.chat.domain.ActOnRequestUseCase
import app.notekea.chat.domain.ActionRunner
import app.notekea.chat.domain.AnswerCitations
import app.notekea.chat.domain.AnswerFromNotesUseCase
import app.notekea.chat.domain.AnswerStep
import app.notekea.chat.domain.ChatError
import app.notekea.chat.domain.ChatHistoryStore
import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.ChatTurn
import app.notekea.chat.domain.FoundRow
import app.notekea.chat.domain.TurnOutcome
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.LocalCalendar
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.BaseViewModel
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlin.time.Clock

/**
 * The chat of the vault at [vaultPath]: questions answered from its notes ([answerFromNotes]) one at a time, the
 * answer shown as it is written, kept in the vault's history ([historyStore]); the model in use is looked at when the
 * chat shows, so it can offer Settings → AI model instead of a question field that cannot answer.
 */
class ChatViewModel(
    private val vaultPath: String,
    private val answerFromNotes: AnswerFromNotesUseCase,
    actOnRequest: ActOnRequestUseCase,
    actionRunner: ActionRunner,
    localCalendar: LocalCalendar,
    clock: Clock,
    private val historyStore: ChatHistoryStore,
    private val llmEngine: LlmEngine
) : BaseViewModel<ChatState, ChatIntent, ChatEffect>(initialState = ChatState()) {
    private val logger = Logger.withTag(tag = LOG_TAG)

    private val actions = ChatActions(
        vaultPath = vaultPath,
        actOnRequest = actOnRequest,
        actionRunner = actionRunner,
        localCalendar = localCalendar,
        clock = clock,
        update = { id, change ->
            update(
                id = id,
                change = change
            )
        },
        emit = { emit(effect = it) }
    )

    private var notes: Collection<NoteEntry> = emptyList()
    private var activeNote: RelativePath? = null
    private var answering: Job? = null
    private var nextId = 0L

    init {
        launchGuarded(onError = ::logError) {
            val turns = historyStore.load(vaultPath = vaultPath)
            val entries = turns.map { entryOf(turn = it) }

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

            ChatIntent.OnDeepToggle -> reduce { it.copy(isDeep = !it.isDeep) }

            ChatIntent.OnModelSettingsOpen -> emit(effect = ChatEffect.OpenModelSettings)

            ChatIntent.OnShown -> checkModel()

            is ChatIntent.OnNotesChange -> notes = intent.notes

            is ChatIntent.OnIndexChange -> reduce { it.copy(isIndexReady = intent.isReady) }

            is ChatIntent.OnActiveNoteChange -> changeActiveNote(path = intent.path)

            is ChatIntent.OnActionConfirm -> entryOf(id = intent.id)?.let {
                launchGuarded(onError = ::logError) {
                    actions.confirm(
                        entry = it,
                        notes = notes
                    )
                }
            }

            is ChatIntent.OnActionCancel -> entryOf(id = intent.id)?.let {
                actions.cancel(entry = it)
            }

            is ChatIntent.OnActionUndo -> entryOf(id = intent.id)?.let {
                launchGuarded(onError = ::logError) { actions.undo(entry = it) }
            }

            is ChatIntent.OnFoundClick -> emit(effect = foundEffectOf(row = intent.row))
        }
    }

    private fun entryOf(id: Long): ChatEntry? = state
        .value
        .entries
        .firstOrNull {
            it.id == id
        }

    /** A task opens at its line; a note opens. */
    private fun foundEffectOf(row: FoundRow): ChatEffect = row.line?.let {
        ChatEffect.Reveal(
            note = row.note,
            range = it..it
        )
    } ?: ChatEffect.Open(note = row.note)

    override fun onCleared() {
        answering?.cancel()
        super.onCleared()
    }

    private fun send() {
        val current = state.value
        if (!current.canSend) return

        val scope = scopeOf(current = current)

        ask(
            question = current.question.trim(),
            scope = scope,
            scopeName = nameOf(
                scope = scope,
                current = current
            )
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

        answering = launchGuarded(onError = {
            failed(
                id = id,
                throwable = it
            )
        }) {
            // A request in the whole vault may be an action; a question asked of a note or a folder is answered.
            val isQuestion = scope != ChatScope.VAULT ||
                actions.act(id = id, request = question, notes = notes, openNote = activeNote)

            if (!isQuestion) return@launchGuarded save()

            update(id = id) { it.copy(step = ChatStep.SEARCHING) }
            val isDeep = state.value.isDeep && state.value.isDeepOffered

            answerFromNotes(
                vaultPath = vaultPath,
                notes = notes,
                question = question,
                only = onlyFor(scope = scope),
                drafts = if (isDeep) DEEP_DRAFTS else 1
            ).collect { step ->
                follow(
                    id = id,
                    step = step
                )
            }
        }
    }

    private fun follow(
        id: Long,
        step: AnswerStep
    ) {
        update(id = id) { entry ->
            when (step) {
                AnswerStep.Searching -> entry.copy(step = ChatStep.SEARCHING)

                is AnswerStep.Reading -> entry.copy(
                    step = ChatStep.READING,
                    readNotes = step.notes
                )

                is AnswerStep.Writing -> entry.copy(
                    step = ChatStep.WRITING,
                    answer = step.text,
                    sources = step.read
                )

                is AnswerStep.Drafting -> entry.copy(step = ChatStep.DRAFTING)

                is AnswerStep.Done -> entry.copy(
                    step = ChatStep.DONE,
                    answer = step.answer.text,
                    sources = step.answer.sources,
                    outcome = TurnOutcome.ANSWERED
                )

                AnswerStep.NotFound -> entry.copy(
                    step = ChatStep.DONE,
                    answer = "",
                    sources = emptyList(),
                    outcome = TurnOutcome.NOT_FOUND
                )

                is AnswerStep.Failed -> entry.copy(
                    step = ChatStep.DONE,
                    outcome = TurnOutcome.FAILED,
                    error = step.error
                )
            }
        }

        if (step is AnswerStep.Failed) noteModelOf(error = step.error)

        val isEnd =
            step is AnswerStep.Done ||
                step == AnswerStep.NotFound ||
                step is AnswerStep.Failed

        if (isEnd) save()
    }

    /** Stops the answer being written; its text so far stays, with the citations it already holds. */
    private fun stop() {
        val entry = state
            .value
            .entries
            .lastOrNull()
            ?.takeIf { it.isAnswering } ?: return

        answering?.cancel()

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

        save()
    }

    private fun clear() {
        if (state.value.isAnswering) answering?.cancel()
        reduce { it.copy(entries = emptyList()) }

        launchGuarded(onError = ::logError) { historyStore.clear(vaultPath = vaultPath) }
    }

    private fun checkModel() {
        launchGuarded(onError = ::logError) {
            val status = when (val ready = llmEngine.readiness()) {
                is Outcome.Success -> {
                    val info = ready.value

                    reduce {
                        it.copy(
                            isSmall = info.isSmall,
                            isDeepOffered = info.hasDeepAnswer
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

    /** The scope chosen, or the whole vault when there is no open note (or folder) to ask of. */
    private fun scopeOf(current: ChatState): ChatScope = when {
        current.scope == ChatScope.NOTE && current.noteName == null -> ChatScope.VAULT
        current.scope == ChatScope.FOLDER && current.folderName == null -> ChatScope.VAULT
        else -> current.scope
    }

    private fun nameOf(
        scope: ChatScope,
        current: ChatState
    ): String? = when (scope) {
        ChatScope.VAULT -> null
        ChatScope.NOTE -> current.noteName
        ChatScope.FOLDER -> current.folderName
    }

    private fun onlyFor(scope: ChatScope): Set<RelativePath>? {
        val note = activeNote ?: return null

        return when (scope) {
            ChatScope.VAULT -> null

            ChatScope.NOTE -> setOf(note)

            ChatScope.FOLDER -> {
                val prefix = note
                    .parent
                    ?.value
                    .orEmpty() + FOLDER_SEPARATOR

                notes
                    .map { it.file.path }
                    .filter { it.value.startsWith(prefix = prefix) }
                    .toSet()
            }
        }
    }

    private fun update(
        id: Long,
        change: (ChatEntry) -> ChatEntry
    ) = reduce { current ->
        current.copy(
            entries = current.entries.map {
                if (it.id == id) change(it) else it
            }
        )
    }

    private fun save() {
        val turns = state
            .value
            .entries
            .filterNot { it.isAnswering }
            .takeLast(n = MAX_TURNS)
            .map { turnOf(entry = it) }

        launchGuarded(onError = ::logError) {
            historyStore.save(
                vaultPath = vaultPath,
                turns = turns
            )
        }
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

    private fun entryOf(turn: ChatTurn): ChatEntry = ChatEntry(
        id = nextId++,
        question = turn.question,
        scope = turn.scope,
        step = ChatStep.DONE,
        answer = turn.answer,
        sources = turn.sources,
        readNotes = 0,
        scopeName = turn.scopeName,
        outcome = turn.outcome,
        error = null
    )

    private fun turnOf(entry: ChatEntry): ChatTurn = ChatTurn(
        question = entry.question,
        scope = entry.scope,
        outcome = entry.outcome ?: TurnOutcome.STOPPED,
        answer = entry.answer,
        sources = entry.sources,
        scopeName = entry.scopeName
    )

    private fun logError(throwable: Throwable) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }
    }

    private companion object {
        const val FOLDER_SEPARATOR = "/"
        const val LOG_TAG = "ChatViewModel"
        const val MAX_TURNS = 100

        // Three drafts and a vote (3.6, docs/answer-quality.md).
        const val DEEP_DRAFTS = 3
        const val UNEXPECTED_ERROR_MESSAGE = "The chat failed unexpectedly"

        fun statusOf(error: LlmError): ModelStatus = when (error) {
            LlmError.NoModel -> ModelStatus.NO_MODEL
            LlmError.NotEnoughMemory -> ModelStatus.NOT_ENOUGH_MEMORY
            LlmError.EngineFailed -> ModelStatus.FAILED
        }
    }
}
