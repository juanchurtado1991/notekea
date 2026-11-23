package app.notekea.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
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
 * Base class of every screen ViewModel (`ARCHITECTURE.md` §4). [state] changes only through [reduce]; [effects] are
 * delivered once; [onIntent] names the intent in the [IntentTrail] and hands it to [handleIntent]. [launchGuarded]
 * catches unexpected exceptions, always rethrowing cancellation; [launchLogged] logs them with [logger].
 */
abstract class BaseViewModel<S, I, E>(initialState: S) : ViewModel() {
    private val effectChannel = Channel<E>(capacity = Channel.BUFFERED)
    private val mutableState = MutableStateFlow(value = initialState)

    protected val logger: Logger = Logger.withTag(tag = this::class.simpleName.orEmpty())

    val effects: Flow<E> = effectChannel.receiveAsFlow()
    val state: StateFlow<S> = mutableState.asStateFlow()

    fun onIntent(intent: I) {
        IntentTrail.record(
            owner = this,
            intent = intent
        )

        handleIntent(intent = intent)
    }

    protected abstract fun handleIntent(intent: I)

    protected fun emit(effect: E) {
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

    /** [launchGuarded] for work whose unexpected failure is only logged, with [message] at [severity]. */
    protected fun launchLogged(
        message: String,
        severity: Severity = Severity.Error,
        block: suspend () -> Unit
    ): Job = launchGuarded(
        onError = {
            logger.log(
                severity = severity,
                tag = logger.tag,
                throwable = it,
                message = message
            )
        },
        block = block
    )

    /**
     * This ViewModel as a [ViewModelHost] whose [ViewModelHost.launch] hands failures to [onError]: what a screen's
     * host delegates to, adding only what its parts need besides.
     */
    protected fun hostOf(onError: (Throwable) -> Unit): ViewModelHost<S, E> =
        object : ViewModelHost<S, E> {
            override val state: StateFlow<S> get() = this@BaseViewModel.state

            override fun reduce(transform: (S) -> S) =
                this@BaseViewModel.reduce(transform = transform)

            override fun emit(effect: E) = this@BaseViewModel.emit(effect = effect)

            override fun launch(block: suspend () -> Unit): Job = launchGuarded(
                onError = onError,
                block = block
            )
        }

    protected fun reduce(transform: (S) -> S) {
        mutableState.update(function = transform)
    }
}
