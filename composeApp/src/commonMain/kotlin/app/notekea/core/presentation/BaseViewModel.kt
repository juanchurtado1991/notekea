package app.notekea.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base class of every screen ViewModel (`ARCHITECTURE.md` §4).
 * [state] is the single source of truth, changed only through [reduce]; [effects] delivers one-shot events
 * exactly once (buffered until collected); [onIntent] is the only public entry point: it names the intent in the
 * [IntentTrail] (the steps a crash or test report lists), then the ViewModel handles it ([handleIntent]).
 * [launchGuarded] catches unexpected exceptions at the edge of the app while always rethrowing cancellation.
 */
abstract class BaseViewModel<S, I, E>(initialState: S) : ViewModel() {
    private val effectChannel = Channel<E>(capacity = Channel.BUFFERED)
    private val mutableState = MutableStateFlow(value = initialState)

    val effects: Flow<E> = effectChannel.receiveAsFlow()
    val state: StateFlow<S> = mutableState.asStateFlow()

    fun onIntent(
        intent: I
    ) {
        IntentTrail.record(owner = this, intent = intent)
        handleIntent(intent = intent)
    }

    protected abstract fun handleIntent(
        intent: I
    )

    protected fun emit(
        effect: E
    ) {
        effectChannel.trySend(element = effect)
    }

    /** Returns the launched job, so a caller can skip a second submit while the first one runs. */
    protected fun launchGuarded(
        onError: (Throwable) -> Unit,
        block: suspend () -> Unit
    ): Job = viewModelScope.launch {
        try {
            block()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            onError(throwable)
        }
    }

    protected fun reduce(
        transform: (S) -> S
    ) {
        mutableState.update(function = transform)
    }
}
