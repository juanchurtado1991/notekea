package app.notekea.chat.data

import com.ghost.serialization.annotations.GhostSerialization

/** A plan as the model writes it. */
@GhostSerialization
data class ActionPlanDto(val steps: List<PlannedStepDto> = emptyList())
