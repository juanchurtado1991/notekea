package app.notekea.chat.domain.language

import app.notekea.chat.domain.ChatExchange
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.PlannedStep

/**
 * "Make it shorter", "en viñetas", "explain it simpler": the last reply said again another way, from the chat so far,
 * not a new search (the chat probe, 2026-11-11: "make it shorter" was answered with a longer answer).
 */
internal object ReplyReworded {
    private val REWORDING = Regex(
        pattern = "^\\W*(make it|make that|say it|put it|write it|explain it|can you make it|" +
            "hac[eé]lo|hazlo|ponelo|ponlo|decilo|dilo|explic[aá]lo|resum[ií]lo|resumelo|m[aá]s corto|m[aá]s largo|" +
            "m[aá]s simple|m[aá]s f[aá]cil|en vi[nñ]etas|en una (frase|l[ií]nea)|in (bullet points|one sentence|" +
            "a list|short)|shorter|longer|simpler|briefly|tl;?dr|translate it|traduc[eí]lo)\\b",
        option = RegexOption.IGNORE_CASE
    )

    // Only a language asked ("in spanish please", "en inglés"): the chat probe on Large took it with a note and said
    // it had no reply to say again.
    private val LANGUAGE_ONLY = Regex(
        pattern = "^\\W*(please |por favor )?((say it|write it|answer|respond|dilo|decilo|contesta) )?(in|en) " +
            "(english|spanish|french|portuguese|german|italian|ingl[eé]s|espa[nñ]ol|franc[eé]s|portugu[eé]s|" +
            "alem[aá]n|italiano)( please| por favor)?\\W*$",
        option = RegexOption.IGNORE_CASE
    )

    fun shaped(
        steps: List<PlannedStep>,
        request: String,
        history: List<ChatExchange>
    ): List<PlannedStep> {
        val trimmed = request.trim()

        val isAsked =
            REWORDING.containsMatchIn(input = trimmed) || LANGUAGE_ONLY.matches(input = trimmed)

        val isRewording = history.isNotEmpty() && isAsked
        if (!isRewording) return steps

        return listOf(PlannedStep(kind = ActionKind.CHAT))
    }
}
