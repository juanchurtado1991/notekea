package app.notekea.core.data

import app.notekea.core.domain.AppDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Desktop dispatchers. `main` is the Swing event dispatch thread provided by `kotlinx-coroutines-swing`.
 * This is the only class allowed to reference [Dispatchers] directly.
 */
class JvmAppDispatchers : AppDispatchers {
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val main: CoroutineDispatcher = Dispatchers.Main
}
