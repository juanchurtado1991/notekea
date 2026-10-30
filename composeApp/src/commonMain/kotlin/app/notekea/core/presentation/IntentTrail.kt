package app.notekea.core.presentation

import kotlin.concurrent.Volatile

/**
 * The person's steps as the app's ViewModels see them: each intent's name (`Editor: OnSaveShortcut`), never what it
 * carries (`GUIDELINES.md` §13). The desktop app sends them to the crash reports' last steps ([listener]); nothing
 * listens elsewhere, as in tests.
 */
object IntentTrail {
    @Volatile
    var listener: ((step: String) -> Unit)? = null

    fun record(
        owner: Any,
        intent: Any?
    ) {
        val listener = listener ?: return
        val screen = owner::class.simpleName.orEmpty().removeSuffix(suffix = VIEW_MODEL_SUFFIX)
        listener("$screen: ${intent?.let { it::class.simpleName }}")
    }

    private const val VIEW_MODEL_SUFFIX = "ViewModel"
}
