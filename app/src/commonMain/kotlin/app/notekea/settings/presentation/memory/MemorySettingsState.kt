package app.notekea.settings.presentation.memory

import app.notekea.core.memory.domain.MemoryField
import app.notekea.core.memory.domain.MemoryLine

/**
 * Settings → Chat memory for the open vault ([hasVault], `docs/agent-memory.md`): the switch ([isOn]), the simple
 * [fields], the [lines] learnt besides them, the vault's [folders] for new notes, Forget everything waiting for its
 * confirmation ([isForgetting]), and whether the last change failed ([hasFailed]).
 */
data class MemorySettingsState(
    val hasVault: Boolean = false,
    val isOn: Boolean = true,
    val fields: Map<MemoryField, String> = emptyMap(),
    val lines: List<MemoryLine> = emptyList(),
    val folders: List<String> = emptyList(),
    val isForgetting: Boolean = false,
    val hasFailed: Boolean = false
) {
    fun valueOf(field: MemoryField): String = fields[field].orEmpty()
}
