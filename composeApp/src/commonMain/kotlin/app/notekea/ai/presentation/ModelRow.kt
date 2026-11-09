package app.notekea.ai.presentation

import app.notekea.ai.domain.ModelSize

/**
 * A chat model as Settings → AI model shows it: its [downloadBytes], whether it [isInstalled], and whether it [fits]
 * this computer, which needs [minimumMemoryGb] otherwise.
 */
data class ModelRow(
    val size: ModelSize,
    val name: String,
    val downloadBytes: Long,
    val isInstalled: Boolean,
    val fits: Boolean,
    val minimumMemoryGb: Int
)
