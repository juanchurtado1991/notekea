package app.notekea.chat.di

import app.notekea.chat.domain.AnswerFromNotesUseCase
import app.notekea.chat.presentation.ChatViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** The chat: answers from the notes with the local model (3.2) and its panel (3.3). */
val chatModule = module {
    factoryOf(::AnswerFromNotesUseCase)

    viewModel { parameters ->
        ChatViewModel(
            vaultPath = parameters.get(),
            answerFromNotes = get(),
            historyStore = get(),
            llmEngine = get()
        )
    }
}
