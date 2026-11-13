package app.notekea.chat.domain.answer

import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.PlannedStep

/**
 * A question about the notes as a whole ("¿de qué tratan las notas?", "summarize everything") is the vault's
 * overview, not an answer from a few passages: the chat probe of 2026-11-11 answered it with the Guides' sentences.
 */
internal object OverviewAsked {
    private val WHOLE = Regex(
        pattern = "(de qu[eé] (tratan|van|hablan)|qu[eé] (hay|tengo) en) (mis |las |todas (mis |las )?)?notas|" +
            "what (are|is) (all )?(my|the) notes( all)? about|what('s| is) in my (notes|vault)|" +
            "(summari[sz]e|resum[ií]\\w*|resume) (everything|all my notes|my notes|my vault|todo|todas mis notas|" +
            "mis notas)\\b|tell me (something )?about my notes|(cont[aá]me|cu[eé]ntame|dime|decime) (algo )?(de|sobre) " +
            "(mis|las) notas|(cu[aá]les|qu[eé]) (son|notas son) (las )?m[aá]s importantes|(which|what) (are )?(my |the )?" +
            "most important( notes)?",
        option = RegexOption.IGNORE_CASE
    )

    fun shaped(
        steps: List<PlannedStep>,
        request: String
    ): List<PlannedStep> {
        val isWhole = steps.none { it.kind.isConfirmed } &&
            WHOLE.containsMatchIn(input = request)

        return if (isWhole) listOf(PlannedStep(kind = ActionKind.OVERVIEW)) else steps
    }
}
