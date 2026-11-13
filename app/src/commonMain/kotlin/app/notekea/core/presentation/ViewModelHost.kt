package app.notekea.core.presentation

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

/**
 * What a ViewModel shares with the parts it routes intents to (`ARCHITECTURE.md` §4): its [state], and the only ways to
 * change it ([reduce]), send an effect ([emit]) and start work ([launch], whose failures the ViewModel handles). A
 * screen's host adds what its parts need besides.
 */
interface ViewModelHost<S, E> {
    val state: StateFlow<S>

    fun reduce(transform: (S) -> S)

    fun emit(effect: E)

    fun launch(block: suspend () -> Unit): Job
}
