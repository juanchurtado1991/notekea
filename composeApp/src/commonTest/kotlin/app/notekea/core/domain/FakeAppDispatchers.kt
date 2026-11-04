package app.notekea.core.domain

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Runs every dispatcher role on the test's [dispatcher], so `runTest` controls all scheduling; a separate [io] on the
 * same scheduler makes a switch to it really suspend, for a test of what happens meanwhile.
 */
class FakeAppDispatchers(
    dispatcher: CoroutineDispatcher,
    override val io: CoroutineDispatcher = dispatcher
) : AppDispatchers {
    override val default: CoroutineDispatcher = dispatcher
    override val main: CoroutineDispatcher = dispatcher
}
