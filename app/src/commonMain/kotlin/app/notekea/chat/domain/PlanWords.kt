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

    /** [steps] as the request's words say: a bookmark asked, or notes found by what they say. */
    fun shaped(
        steps: List<PlannedStep>,
        request: String
    ): List<PlannedStep> {
        val single = steps.singleOrNull() ?: return steps

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
}
