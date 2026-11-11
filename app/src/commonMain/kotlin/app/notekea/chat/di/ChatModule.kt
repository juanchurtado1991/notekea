package app.notekea.chat.di

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.ActOnRequestUseCase
import app.notekea.chat.domain.ActionPlanReader
import app.notekea.chat.domain.AgentAnswerUseCase
import app.notekea.chat.domain.AnswerFromNotesUseCase
import app.notekea.chat.domain.CheckPlanUseCase
import app.notekea.chat.domain.ComposeNotesUseCase
import app.notekea.chat.domain.ConverseUseCase
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
    factoryOf(::ComposeNotesUseCase)
    factoryOf(::ActOnRequestUseCase)
    factoryOf(::ConverseUseCase)
    factoryOf(::AgentAnswerUseCase)
    factory<ActionPlanReader> { GhostActionPlanReader() }

    viewModel { parameters ->
        ChatViewModel(
            vaultPath = parameters.get(),
            answerFromNotes = get(),
            converse = get(),
            agentAnswer = get(),
            actOnRequest = get(),
            actionRunner = get(),
            localCalendar = get(),
            clock = get(),
            historyStore = get(),
            llmEngine = get()
        )
    }
}
