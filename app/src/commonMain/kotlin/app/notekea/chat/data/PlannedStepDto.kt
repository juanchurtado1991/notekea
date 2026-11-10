package app.notekea.chat.data

import com.ghost.serialization.annotations.GhostSerialization

/** One step of a plan as the model writes it (`ActionPrompt.SCHEMA`): every field but the action may be left out. */
@GhostSerialization
data class PlannedStepDto(
    val action: String,
    val note: String? = null,
    val notes: List<String> = emptyList(),
    val folder: String? = null,
    val tag: String? = null,
    val title: String? = null,
    val text: String? = null,
    val template: String? = null,
    val property: String? = null,
    val value: String? = null,
    val target: String? = null,
    val due: String? = null,
    val period: String? = null,
    val panel: String? = null,
    val theme: String? = null,
    val request: String? = null
)
