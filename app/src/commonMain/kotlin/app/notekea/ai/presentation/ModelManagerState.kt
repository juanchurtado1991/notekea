package app.notekea.ai.presentation

import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.ai.domain.InstallSession
import app.notekea.ai.domain.ModelSize

/**
 * Settings → AI model (`ROADMAP.md` 3.1, design C): the [models] of the catalog, the one [recommended] for this
 * computer's [memoryGb], the one [active], the installation under way ([session]), and the [rerankerBytes] the first
 * installation adds. Only the [featured] size is shown at first; the others wait under "Other sizes".
 */
data class ModelManagerState(
    val isLoading: Boolean = true,
    val hasLoadFailed: Boolean = false,
    val memoryGb: Int = 0,
    val models: List<ModelRow> = emptyList(),
    val recommended: ModelSize? = null,
    val active: ModelSize? = null,
    val session: InstallSession? = null,
    val rerankerBytes: Long = 0,
    val isRerankerInstalled: Boolean = false,
    val folder: String = "",
    val isOtherSizesShown: Boolean = false,
    val pendingDelete: ModelSize? = null,
    val buttonOrder: DialogButtonOrder = DialogButtonOrder.CANCEL_THEN_PRIMARY
) {
    /** The size shown first: the one in use, else the one installing, else the one recommended. */
    val featured: ModelSize? get() = active ?: session?.size ?: recommended
}
