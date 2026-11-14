package app.notekea.chat.eval

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.PlannedStep
import com.fasterxml.jackson.databind.JsonNode

/** Whether a plan is the one `smoke-data/eval/actions.json` expects: its actions, in order, with their fields. */
internal object ActionGrade {
    fun isRight(
        steps: List<PlannedStep>,
        expected: List<JsonNode>
    ): Boolean {
        val merged = merged(steps = steps)

        return merged.size == expected.size &&
            merged.zip(expected).all { (step, wanted) ->
                matches(
                    step = step,
                    wanted = wanted
                )
            }
    }

    /** Steps of one action in a row, each on one note, as one step on all of them: the same plan. */
    private fun merged(steps: List<PlannedStep>): List<PlannedStep> = steps
        .fold(emptyList()) { kept, step ->
            val last = kept.lastOrNull()

            val isSame =
                last != null &&
                    last.kind == step.kind &&
                    last.kind.isConfirmed &&
                    last.fields - ActionField.NOTE == step.fields - ActionField.NOTE

            if (!isSame) {
                kept + step
            } else {
                kept.dropLast(n = 1) + last.copy(
                    fields = last.fields - ActionField.NOTE,
                    notes = last.notes +
                        listOfNotNull(last[ActionField.NOTE], step[ActionField.NOTE]) +
                        step.notes
                )
            }
        }

    private fun matches(
        step: PlannedStep,
        wanted: JsonNode
    ): Boolean {
        if (step.kind.written != wanted.get("action").asText()) return false

        return wanted
            .get("fields")
            .properties()
            .all { (name, value) ->
                val field = ActionField.entries.first {
                    it.written == name
                }

                val listed = if (field == ActionField.NOTE) step.notes else emptyList()
                val planned = listOfNotNull(step[field]) + listed

                val expected = value.asText().lowercase()

                // A folder's notes, listed one by one, are that folder.
                val isFolderListed =
                    field == ActionField.FOLDER &&
                        step.notes.isNotEmpty() &&
                        step.notes.all { it.lowercase().startsWith(prefix = "$expected/") }

                isFolderListed || planned.any { expected.isEmpty() || expected in it.lowercase() }
            }
    }
}
