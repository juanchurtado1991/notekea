package app.notekea.chat.data

import com.ghost.serialization.annotations.GhostSerialization

/**
 * One item of a turn as the history keeps it, told apart by [type]: a call ([name], [target]), a result ([text],
 * [origin]), what was said ([text], [sources]) or a proposal ([changes], [outcome], [problem]).
 */
@GhostSerialization
data class TurnItemDto(
    val type: String,
    val name: String = "",
    val target: String = "",
    val text: String = "",
    val origin: String? = null,
    val outcome: String? = null,
    val problem: String? = null,
    val sources: List<AnswerSourceDto> = emptyList(),
    val changes: List<ProposedChangeDto> = emptyList()
)
