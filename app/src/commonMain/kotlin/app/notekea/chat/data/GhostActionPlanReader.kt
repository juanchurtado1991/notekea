package app.notekea.chat.data

import app.notekea.chat.domain.ActionField
import app.notekea.chat.domain.ActionKind
import app.notekea.chat.domain.ActionPlanReader
import app.notekea.chat.domain.AgentMove
import app.notekea.chat.domain.PlannedStep
import com.ghost.serialization.Ghost
import com.ghost.serialization.exception.GhostJsonException

/** [ActionPlanReader] over the model's JSON: a step naming no known action is left out, a reply not JSON is null. */
class GhostActionPlanReader : ActionPlanReader {
    override fun read(json: String): List<PlannedStep>? {
        val dto = try {
            Ghost.deserialize<ActionPlanDto>(json)
        } catch (_: GhostJsonException) {
            return null
        }

        return dto.steps.mapNotNull { stepOf(dto = it) }
    }

    override fun readNotes(json: String): List<Pair<String, String>>? {
        val dto = try {
            Ghost.deserialize<TextNotesDto>(json)
        } catch (_: GhostJsonException) {
            return null
        }

        return dto
            .notes
            .filter {
                it.title.isNotBlank() && it.text.isNotBlank()
            }
            .map {
                it.title.trim() to it.text.trim()
            }
    }

    override fun readMove(json: String): AgentMove? = try {
        Ghost.deserialize<AgentMoveDto>(json).let {
            AgentMove(
                move = it.move,
                query = it.query,
                note = it.note
            )
        }
    } catch (_: GhostJsonException) {
        null
    }

    private fun stepOf(dto: PlannedStepDto): PlannedStep? {
        val kind = ActionKind.of(written = dto.action) ?: return null

        val fields = mapOf(
            ActionField.NOTE to dto.note,
            ActionField.FOLDER to dto.folder,
            ActionField.TAG to dto.tag,
            ActionField.TITLE to dto.title,
            ActionField.TEXT to dto.text,
            ActionField.TEMPLATE to dto.template,
            ActionField.PROPERTY to dto.property,
            ActionField.VALUE to dto.value,
            ActionField.TARGET to dto.target,
            ActionField.DUE to dto.due,
            ActionField.PERIOD to dto.period,
            ActionField.PANEL to dto.panel,
            ActionField.THEME to dto.theme,
            ActionField.REQUEST to dto.request,
            ActionField.URL to dto.url
        )

        return PlannedStep(
            kind = kind,
            fields = fields.mapNotNull { (field, value) ->
                value?.let {
                    field to it
                }
            }.toMap(),
            notes = dto.notes
        )
    }
}
