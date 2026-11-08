package app.notekea.models.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** [ModelInstaller] that only records what it was asked; tests set [current] to the session they need. */
class FakeModelInstaller : ModelInstaller {
    val current = MutableStateFlow<InstallSession?>(value = null)
    val started = mutableListOf<ModelSize>()
    var pauses = 0
    var cancels = 0

    override val session: StateFlow<InstallSession?> = current

    override fun start(
        model: ChatModel
    ) {
        started += model.size
    }

    override fun pause() {
        pauses++
    }

    override fun cancel() {
        cancels++
    }
}
