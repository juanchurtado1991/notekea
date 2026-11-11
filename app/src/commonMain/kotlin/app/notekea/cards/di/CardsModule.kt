package app.notekea.cards.di

import app.notekea.cards.data.GhostCardsReader
import app.notekea.cards.domain.AnswerCardUseCase
import app.notekea.cards.domain.CardsReader
import app.notekea.cards.domain.MakeCardsUseCase
import app.notekea.cards.domain.ReadDeckUseCase
import app.notekea.cards.presentation.StudyViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Study cards (4.6): made from a note by the model, reviewed in the Study tab, kept in the notes. */
val cardsModule = module {
    factory<CardsReader> { GhostCardsReader() }
    factoryOf(::MakeCardsUseCase)
    factoryOf(::ReadDeckUseCase)
    factoryOf(::AnswerCardUseCase)

    viewModel { parameters ->
        StudyViewModel(
            vaultPath = parameters.get(),
            readDeck = get(),
            answerCard = get(),
            localCalendar = get(),
            clock = get()
        )
    }
}
