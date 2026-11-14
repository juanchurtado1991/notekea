package app.notekea.chat.domain.skill

/**
 * Short instructions for one kind of work (`docs/agent-loop.md`): its [name], a [description] the router chooses it
 * by, and the [instructions] the loop is given only when it is chosen.
 */
data class Skill(
    val name: String,
    val description: String,
    val instructions: String
)
