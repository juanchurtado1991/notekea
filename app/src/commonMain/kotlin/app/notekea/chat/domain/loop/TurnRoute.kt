package app.notekea.chat.domain.loop

import app.notekea.chat.domain.skill.Skill

/** What the router chose for a request: its kind of work, and the skill that fits it, if one does. */
data class TurnRoute(
    val kind: WorkKind,
    val skill: Skill?
)
