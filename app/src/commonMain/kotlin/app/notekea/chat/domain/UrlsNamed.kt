package app.notekea.chat.domain

/**
 * A web page is clipped only from an address the request writes (`GUIDELINES.md` §13): a clip step's URL that the
 * request does not hold is replaced by the one it does, or left out, so the card asks for it; a request that names
 * an address and asks nothing else of the vault is a clip of it.
 */
internal object UrlsNamed {
    private val URL = Regex(
        pattern = "https?://[^\\s<>\"']+",
        option = RegexOption.IGNORE_CASE
    )
    private const val TRAILING = ".,;:!?)]}"

    fun shaped(
        steps: List<PlannedStep>,
        request: String
    ): List<PlannedStep> {
        val written = URL
            .findAll(input = request)
            .map { it.value.trimEnd(*TRAILING.toCharArray()) }
            .toList()

        val isClip = steps.any {
            it.kind == ActionKind.CLIP_URL
        }

        val isQuestion = steps.none { it.kind.isConfirmed }

        val isAskedOfPage =
            !isClip &&
                isQuestion &&
                written.isNotEmpty()

        if (isAskedOfPage) {
            return listOf(
                PlannedStep(
                    kind = ActionKind.CLIP_URL,
                    fields = mapOf(ActionField.URL to written.first())
                )
            )
        }

        return steps.map { step ->
            if (step.kind != ActionKind.CLIP_URL) return@map step

            val url = step[ActionField.URL]?.takeIf {
                it in written
            } ?: written.firstOrNull()

            step.copy(
                fields = url?.let {
                    step.fields + (ActionField.URL to it)
                } ?: (step.fields - ActionField.URL)
            )
        }
    }
}
