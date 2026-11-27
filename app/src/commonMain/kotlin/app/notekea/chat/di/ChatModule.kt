package app.notekea.chat.di

import app.notekea.chat.data.BundledHelpGuideSource
import app.notekea.chat.data.GhostModelJson
import app.notekea.chat.data.GhostTextNotesReader
import app.notekea.chat.domain.CompactChatUseCase
import app.notekea.chat.domain.agent.ProposalCheck
import app.notekea.chat.domain.answer.ConverseUseCase
import app.notekea.chat.domain.answer.DescribeVaultUseCase
import app.notekea.chat.domain.answer.PassageSearch
import app.notekea.chat.domain.help.AnswerHelpUseCase
import app.notekea.chat.domain.help.HelpGuideSource
import app.notekea.chat.domain.loop.ChatLoop
import app.notekea.chat.domain.loop.FrameSource
import app.notekea.chat.domain.loop.LoopTools
import app.notekea.chat.domain.loop.RunTurnUseCase
import app.notekea.chat.domain.loop.TaskFramer
import app.notekea.chat.domain.loop.TurnFrames
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.plan.TextNotesReader
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.chat.domain.sources.AnswerFromPagesUseCase
import app.notekea.chat.domain.tool.ModelJson
import app.notekea.chat.presentation.viewmodel.ChatSources
import app.notekea.chat.presentation.viewmodel.ChatViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** The chat: the agent's loop on the whole vault, answers from a note or a folder (3.2), its panel (3.3). */
val chatModule = module {
    factoryOf(::PassageSearch)
    factoryOf(::CheckPlanUseCase)

    factory {
        ComposeNotesUseCase(
            llmEngine = get(),
            noteTextCache = get(),
            notesReader = get(),
            clipper = get(),
            cardMaker = get()
        )
    }

    factoryOf(::ConverseUseCase)
    factoryOf(::AnswerFromPagesUseCase)
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
            modelJson = get(),
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
    factory<TextNotesReader> { GhostTextNotesReader() }
    factory<ModelJson> { GhostModelJson() }
    single<HelpGuideSource> { BundledHelpGuideSource(dispatchers = get()) }

    viewModel { parameters ->
        ChatViewModel(
            vaultPath = parameters.get(),
            converse = get(),
            answerHelp = get(),
            describeVault = get(),
            conventions = get(),
            compactChat = get(),
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
