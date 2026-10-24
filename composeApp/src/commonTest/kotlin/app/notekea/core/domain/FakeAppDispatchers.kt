package app.notekea.core.domain

import kotlinx.coroutines.CoroutineDispatcher

/** Runs every dispatcher role on the test's [dispatcher], so `runTest` controls all scheduling. */
class FakeAppDispatchers(dispatcher: CoroutineDispatcher) : AppDispatchers {
    override val default: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
    override val main: CoroutineDispatcher = dispatcher
}
