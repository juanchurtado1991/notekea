package app.notekea.chat.di

import app.notekea.chat.domain.AnswerFromNotesUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** The chat: answers from the notes with the local model (3.2). */
val chatModule = module { factoryOf(::AnswerFromNotesUseCase) }
