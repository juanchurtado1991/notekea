package app.notekea.core.domain

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Coroutine dispatchers injected everywhere instead of referencing `Dispatchers` directly,
 * so tests can replace them with a test dispatcher.
 */
interface AppDispatchers {
    val default: CoroutineDispatcher
    val io: CoroutineDispatcher
    val main: CoroutineDispatcher
}
