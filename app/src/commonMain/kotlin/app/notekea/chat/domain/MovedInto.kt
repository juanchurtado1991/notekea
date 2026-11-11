package app.notekea.chat.domain

/**
 * A move of named notes whose folder came under folder, not target ("move Pancakes and Arepas, folder Archive"): that
 * folder is where they go (the chat probe of 2026-11-11 failed it for a missing target).
 */
internal object MovedInto {
    fun of(step: PlannedStep): PlannedStep {
        val folder = step[ActionField.FOLDER]
        val isNamed = step[ActionField.NOTE] != null || step.notes.isNotEmpty()

        val isInto =
            step.kind == ActionKind.MOVE &&
                step[ActionField.TARGET] == null &&
                folder != null &&
                isNamed

        if (!isInto) return step

        val target = ActionField.TARGET to folder.orEmpty()

        return step.copy(fields = step.fields - ActionField.FOLDER + target)
    }
}
