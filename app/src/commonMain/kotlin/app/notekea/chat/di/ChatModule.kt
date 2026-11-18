package app.notekea.chat.di

import app.notekea.chat.data.BundledHelpGuideSource
import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.CompactChatUseCase
import app.notekea.chat.domain.agent.AgentAnswerUseCase
import app.notekea.chat.domain.agent.AgentTaskUseCase
import app.notekea.chat.domain.agent.ProposalCheck
import app.notekea.chat.domain.answer.AnswerFromFoundUseCase
import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.answer.ConverseUseCase
import app.notekea.chat.domain.answer.DescribeVaultUseCase
import app.notekea.chat.domain.help.AnswerHelpUseCase
import app.notekea.chat.domain.help.HelpGuideSource
import app.notekea.chat.domain.loop.ChatLoop
import app.notekea.chat.domain.loop.FrameSource
import app.notekea.chat.domain.loop.LoopTools
import app.notekea.chat.domain.loop.RunTurnUseCase
import app.notekea.chat.domain.loop.TaskFramer
import app.notekea.chat.domain.loop.TurnFrames
import app.notekea.chat.domain.plan.ActOnRequestUseCase
import app.notekea.chat.domain.plan.ActionPlanReader
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.plan.PlanRequestUseCase
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.chat.domain.sources.AnswerFromPagesUseCase
import app.notekea.chat.presentation.viewmodel.ChatSources
import app.notekea.chat.presentation.viewmodel.ChatViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** The chat: answers from the notes with the local model (3.2), its panel (3.3) and its actions (3.8, 3.9). */
val chatModule = module {
    factoryOf(::AnswerFromNotesUseCase)
    factoryOf(::PlanRequestUseCase)
    factoryOf(::CheckPlanUseCase)

    factory {
        ComposeNotesUseCase(
            llmEngine = get(),
            noteTextCache = get(),
            planReader = get(),
            clipper = get(),
            cardMaker = get()
        )
    }

    factoryOf(::ActOnRequestUseCase)
    factoryOf(::ConverseUseCase)
    factoryOf(::AnswerFromPagesUseCase)
    factoryOf(::AgentAnswerUseCase)
    factoryOf(::AgentTaskUseCase)
    factoryOf(::VaultGuide)
    factoryOf(::ProposalCheck)
    factoryOf(::LoopTools)
    factory { get<LoopTools>().toolsets() }
    factoryOf(::TaskFramer)
    factory<FrameSource> { TurnFrames(get(), get(), get()) }

    factory {
        RunTurnUseCase(
            llmEngine = get(),
            toolsets = get(),
            vaultGuide = get(),
            proposal = get(),
            clock = get(),
            // Bound on the desktop (`SqliteStepMemory`); elsewhere the loop drops what does not fit, as before.
            stepMemory = getOrNull(),
            // The vault's own web sources, trusted by definition (`docs/agent-web.md`).
            sourceSites = getOrNull(),
            // Which notes the task means, said before the turn (`docs/agent-frame.md`).
            frames = get()
        )
    }

    factoryOf(::ChatLoop)
    factoryOf(::AnswerHelpUseCase)
    factoryOf(::DescribeVaultUseCase)
    factoryOf(::CompactChatUseCase)
    factoryOf(::AnswerFromFoundUseCase)
    factory<ActionPlanReader> { GhostActionPlanReader() }
    single<HelpGuideSource> { BundledHelpGuideSource(dispatchers = get()) }

    viewModel { parameters ->
        ChatViewModel(
            vaultPath = parameters.get(),
            answerFromNotes = get(),
            converse = get(),
            agentAnswer = get(),
            answerHelp = get(),
            describeVault = get(),
            conventions = get(),
            actOnRequest = get(),
            agentTask = get(),
            compactChat = get(),
            answerFromFound = get(),
            actionRunner = get(),
            localCalendar = get(),
            clock = get(),
            historyStore = get(),
            clipboard = get(),
            llmEngine = get(),
            sources = ChatSources(
                answerFromPages = get(),
                sourceSites = getOrNull()
            ),
            loop = get()
        )
    }
}
