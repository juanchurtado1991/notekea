package app.notekea.chat.domain

/**
 * Plans the action eval of 2026-11-11 found the model mixing up, put right from the request's own words: "bookmark
 * Home" planned as showing the bookmarks, "find my notes on habits" as a query of properties, and "mueve todas las
 * notas de Guides" naming the folder as a note.
 */
internal object PlanWords {
    private val BOOKMARKING = Regex(
        pattern = "^\\W*(bookmark|a[nñ]ad[ei]\\w* a (los )?marcadores|marc[aá]\\w* como favorit[oa])\\s+" +
            "(the folder |la carpeta |el folder )?(.+?)\\W*$",
        option = RegexOption.IGNORE_CASE
    )

    private val TOPIC = Regex(
        pattern = "^\\W*(find|search|look for|busca\\w*|encuentra\\w*) (my |all |all my |mis |todas mis )?" +
            "(notes|notas) (on|about|sobre|de|acerca de) (.+?)\\W*$",
        option = RegexOption.IGNORE_CASE
    )

    private val TAGGING = setOf(ActionKind.ADD_TAG, ActionKind.SET_PROPERTY)

    private val LETTER = Regex(pattern = "\\p{L}")

    /** [steps] as the request's words say: talk with no word, a bookmark asked, or notes found by what they say. */
    fun shaped(
        steps: List<PlannedStep>,
        request: String
    ): List<PlannedStep> {
        // "?" alone was planned as opening today's note (the chat probe on Large, 2026-11-11).
        val isWordless = !LETTER.containsMatchIn(input = request)
        if (isWordless) return listOf(PlannedStep(kind = ActionKind.CHAT))

        val single = steps.singleOrNull() ?: return steps

        // "tell me a joke about my notes" planned as talk was answered "I cannot tell jokes about your notes".
        val isWritten = single.kind == ActionKind.CHAT && AnswerPrompt.isCreative(text = request)

        if (isWritten) {
            return listOf(
                PlannedStep(
                    kind = ActionKind.ANSWER,
                    fields = mapOf(ActionField.REQUEST to request)
                )
            )
        }

        val bookmarked = BOOKMARKING
            .find(input = request)
            ?.takeIf {
                single.kind == ActionKind.SHOW_PANEL
            }

        if (bookmarked != null) {
            val (_, _, _, folder, name) = bookmarked.groupValues
            val field = if (folder.isNotEmpty()) ActionField.FOLDER else ActionField.NOTE

            return listOf(
                PlannedStep(
                    kind = ActionKind.BOOKMARK,
                    fields = mapOf(field to name)
                )
            )
        }

        val topic = TOPIC
            .find(input = request)
            ?.takeIf {
                single.kind == ActionKind.FIND_NOTES
            }
            ?: return steps

        return listOf(
            PlannedStep(
                kind = ActionKind.SEARCH,
                fields = mapOf(ActionField.TEXT to topic.groupValues.last())
            )
        )
    }

    /** [step] with a folder it names as its note put in its folder field, when no note has that name. */
    fun folderNamed(
        step: PlannedStep,
        context: ActionContext
    ): PlannedStep {
        val named = step[ActionField.NOTE] ?: return step
        val isOrganizing = step.kind.isOrganizing || step.kind in TAGGING

        val folder = context
            .folders
            .firstOrNull {
                it.equals(
                    other = named.trim('/'),
                    ignoreCase = true
                )
            }

        val isNote = context.notes.any {
            it.substringAfterLast(delimiter = '/').equals(
                other = named,
                ignoreCase = true
            )
        }

        if (!isOrganizing ||
            folder == null ||
            isNote ||
            step[ActionField.FOLDER] != null
        ) {
            return step
        }

        return step.copy(fields = step.fields - ActionField.NOTE + (ActionField.FOLDER to folder))
    }

    /**
     * [steps] with a move's missing target found among its notes: the folder the plan creates or the vault holds,
     * listed as one of them ("move Arepas, Pancakes, Archive" after create_folder Archive, the probe on Large).
     */
    fun targetsNamed(
        steps: List<PlannedStep>,
        context: ActionContext
    ): List<PlannedStep> {
        val created = steps.mapNotNull {
            it[ActionField.FOLDER].takeIf { _ ->
                it.kind == ActionKind.CREATE_FOLDER
            }
        }

        val folders = (created + context.folders).associateBy { it.lowercase() }

        return steps.map { step ->
            val isUntargeted = step.kind == ActionKind.MOVE && step[ActionField.TARGET] == null

            val target = step
                .notes
                .lastOrNull()
                ?.let {
                    folders[it.trim('/').lowercase()]
                }

            if (!isUntargeted || target == null) return@map step

            step.copy(
                fields = step.fields + (ActionField.TARGET to target),
                notes = step.notes.dropLast(n = 1)
            )
        }
    }
}
