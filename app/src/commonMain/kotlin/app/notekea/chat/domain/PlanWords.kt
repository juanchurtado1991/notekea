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

    private val MOVED_TO = Regex(
        pattern = "\\b(?:to|into|a|en|hacia)\\s+(?:the |la |una |el )?(?:folder |carpeta )?(\\p{Lu}[\\p{L}\\p{N}_-]*)"
    )

    // A date the request writes or names: without one, a due date the model filled in ("add those as tasks here"
    // got today's) is dropped.
    private val DATED = Regex(
        pattern = "\\d|today|tomorrow|tonight|week|month|monday|tuesday|wednesday|thursday|friday|saturday|sunday|" +
            "hoy|mañana|manana|semana|mes|lunes|martes|miércoles|miercoles|jueves|viernes|sábado|sabado|domingo",
        option = RegexOption.IGNORE_CASE
    )

    /** [step] without a due date the request never gave. */
    fun dueAsked(
        step: PlannedStep,
        request: String
    ): PlannedStep {
        val isUnasked = step[ActionField.DUE] != null && !DATED.containsMatchIn(input = request)

        return if (isUnasked) step.copy(fields = step.fields - ActionField.DUE) else step
    }

    private val TAGGING = setOf(ActionKind.ADD_TAG, ActionKind.SET_PROPERTY)

    private val LETTER = Regex(pattern = "\\p{L}")

    // "tráeme la última nota que escribí": the note changed last, not one the model picks (the probe on Medium).
    private val LATEST = Regex(
        pattern = "(([uú]ltima|m[aá]s reciente) nota|(last|latest|most recent) note|nota (m[aá]s )?reciente)",
        option = RegexOption.IGNORE_CASE
    )

    // "explícame qué es Notekea": the guide says it, not talk.
    private val WHAT_IS = Regex(
        pattern = "(qu[eé] es|what is|what's|explain|expl[ií]c\\w*|tell me about)\\W+(\\w+\\W+)?notekea\\b",
        option = RegexOption.IGNORE_CASE
    )

    // "¿cuál de mis proyectos está más atrasado?": its overdue tasks, not its budget.
    private val LATE = Regex(
        pattern = "\\b(atrasad\\w*|retrasad\\w*|late|behind|overdue|vencid\\w*)\\b",
        option = RegexOption.IGNORE_CASE
    )

    // "Notekea es una porquería, no sirve para nada" went to help, which listed what it does (the probe, Medium).
    private val COMPLAINT = Regex(
        pattern = "\\b(porquer[ií]a|basura|no sirve|in[uú]til|una mierda|odio|est[uú]pid[oa]|sucks|useless|garbage|" +
            "rubbish|terrible|horrible|i hate|stupid|dumb)\\b",
        option = RegexOption.IGNORE_CASE
    )

    /** [steps] as the request's words say: talk with no word, a bookmark asked, or notes found by what they say. */
    fun shaped(
        steps: List<PlannedStep>,
        request: String,
        context: ActionContext = ActionContext(today = "")
    ): List<PlannedStep> {
        // "?" alone was planned as opening today's note (the chat probe on Large, 2026-11-11).
        val isWordless = !LETTER.containsMatchIn(input = request)
        if (isWordless) return listOf(PlannedStep(kind = ActionKind.CHAT))

        val isComplaint = COMPLAINT.containsMatchIn(input = request) &&
            steps.none { it.kind.isConfirmed }

        if (isComplaint) return listOf(PlannedStep(kind = ActionKind.CHAT))

        val isAsked = steps.none { it.kind.isConfirmed }

        worded(
            request = request,
            context = context
        )?.takeIf { isAsked }?.let {
            return it
        }

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

    /** The plan a request's own words call for: the note changed last, the guide on Notekea, the overdue tasks. */
    private fun worded(
        request: String,
        context: ActionContext
    ): List<PlannedStep>? {
        val latest = context.latestNote?.takeIf { LATEST.containsMatchIn(input = request) }

        if (latest != null) {
            return listOf(
                PlannedStep(
                    kind = ActionKind.OPEN_NOTE,
                    fields = mapOf(ActionField.NOTE to latest)
                )
            )
        }

        if (WHAT_IS.containsMatchIn(input = request)) {
            return listOf(
                PlannedStep(
                    kind = ActionKind.HELP,
                    fields = mapOf(ActionField.REQUEST to "what is Notekea?")
                )
            )
        }

        val isLate = LATE.containsMatchIn(input = request) && !RefusalChecked.isChange(request)
        if (!isLate) return null

        return listOf(
            PlannedStep(
                kind = ActionKind.SHOW_TASKS,
                fields = mapOf(ActionField.PERIOD to "overdue")
            )
        )
    }

    /**
     * [steps] with a move's missing target found among its notes: the folder the plan creates or the vault holds,
     * listed as one of them ("move Arepas, Pancakes, Archive" after create_folder Archive, the probe on Large).
     */
    fun targetsNamed(
        steps: List<PlannedStep>,
        context: ActionContext,
        request: String = ""
    ): List<PlannedStep> {
        val created = steps.mapNotNull {
            it[ActionField.FOLDER].takeIf { _ ->
                it.kind == ActionKind.CREATE_FOLDER
            }
        }

        val folders = (created + context.folders).associateBy { it.lowercase() }

        // The folder the request moves them to ("to Archive", "a Cocina"), when the plan lost it.
        val said = MOVED_TO
            .findAll(input = request)
            .lastOrNull()
            ?.groupValues
            ?.last()

        return steps.map { step ->
            val isUntargeted = step.kind == ActionKind.MOVE && step[ActionField.TARGET] == null
            if (!isUntargeted) return@map step

            val listed = step
                .notes
                .lastOrNull()
                ?.let {
                    folders[it.trim('/').lowercase()]
                }

            val target = listed ?: created.singleOrNull() ?: said ?: return@map step

            // A folder written among the notes, however it was written ("Recipes/Archive (if created…)").
            val notes = step.notes.filterNot {
                target.lowercase() in it.lowercase()
            }

            step.copy(
                fields = step.fields + (ActionField.TARGET to target),
                notes = notes
            )
        }
    }
}
