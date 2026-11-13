package app.notekea.chat.di

import app.notekea.chat.data.BundledHelpGuideSource
import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.CompactChatUseCase
import app.notekea.chat.domain.agent.AgentAnswerUseCase
import app.notekea.chat.domain.agent.AgentTaskUseCase
import app.notekea.chat.domain.answer.AnswerFromFoundUseCase
import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.answer.ConverseUseCase
import app.notekea.chat.domain.answer.DescribeVaultUseCase
import app.notekea.chat.domain.help.AnswerHelpUseCase
import app.notekea.chat.domain.help.HelpGuideSource
import app.notekea.chat.domain.plan.ActOnRequestUseCase
import app.notekea.chat.domain.plan.ActionPlanReader
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.plan.PlanRequestUseCase
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
    factoryOf(::AgentAnswerUseCase)
    factoryOf(::AgentTaskUseCase)
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
            llmEngine = get()
        )
    }
}
