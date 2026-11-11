package app.notekea.chat.di

import app.notekea.chat.data.BundledHelpGuideSource
import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.ActOnRequestUseCase
import app.notekea.chat.domain.ActionPlanReader
import app.notekea.chat.domain.AgentAnswerUseCase
import app.notekea.chat.domain.AgentTaskUseCase
import app.notekea.chat.domain.AnswerFromNotesUseCase
import app.notekea.chat.domain.AnswerHelpUseCase
import app.notekea.chat.domain.CheckPlanUseCase
import app.notekea.chat.domain.ComposeNotesUseCase
import app.notekea.chat.domain.ConverseUseCase
import app.notekea.chat.domain.DescribeVaultUseCase
import app.notekea.chat.domain.HelpGuideSource
import app.notekea.chat.domain.PlanRequestUseCase
import app.notekea.chat.presentation.ChatViewModel
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
    factoryOf(::AgentAnswerUseCase)
    factoryOf(::AgentTaskUseCase)
    factoryOf(::AnswerHelpUseCase)
    factoryOf(::DescribeVaultUseCase)
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
            actionRunner = get(),
            localCalendar = get(),
            clock = get(),
            historyStore = get(),
            clipboard = get(),
            llmEngine = get()
        )
    }
}
