package app.notekea.chat.domain

/**
 * "Make it shorter", "en viñetas", "explain it simpler": the last reply said again another way, from the chat so far,
 * not a new search (the chat probe, 2026-11-11: "make it shorter" was answered with a longer answer).
 */
internal object ReplyReworded {
    private val REWORDING = Regex(
        pattern = "^\\W*(make it|make that|say it|put it|write it|explain it|can you make it|" +
            "hac[eé]lo|hazlo|ponelo|ponlo|decilo|dilo|explic[aá]lo|resum[ií]lo|resumelo|m[aá]s corto|m[aá]s largo|" +
            "m[aá]s simple|m[aá]s f[aá]cil|en vi[nñ]etas|en una (frase|l[ií]nea)|in (bullet points|one sentence|" +
            "a list|short)|shorter|longer|simpler|briefly|tl;?dr)\\b",
        option = RegexOption.IGNORE_CASE
    )

    fun shaped(
        steps: List<PlannedStep>,
        request: String,
        history: List<ChatExchange>
    ): List<PlannedStep> {
        val isRewording = history.isNotEmpty() && REWORDING.containsMatchIn(input = request.trim())
        if (!isRewording) return steps

        return listOf(PlannedStep(kind = ActionKind.CHAT))
    }
}
