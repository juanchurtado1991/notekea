package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.notes.domain.EditorPane
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.Workspace
import app.notekea.notes.domain.WorkspaceStore
import com.russhwolf.settings.Settings
import kotlinx.coroutines.withContext

/**
 * [WorkspaceStore] on Multiplatform Settings: one key per vault (`workspace.<hash>`, [VaultKeyHash]) holding lines of
 * text: the active pane first, then each pane as a line `/<active tab>` followed by its tabs' paths, one per line. A
 * path never starts with `/`, so the two never mix. Anything that cannot be read back is no workspace at all.
 */
class SettingsWorkspaceStore(
    private val settings: Settings,
    private val dispatchers: AppDispatchers
) : WorkspaceStore {
    override suspend fun read(
        vaultPath: String
    ): Workspace? = withContext(context = dispatchers.io) {
        val lines = settings.getStringOrNull(key = keyOf(vaultPath = vaultPath))?.split(LINE_SEPARATOR)
        val activePane = lines?.firstOrNull()?.toIntOrNull() ?: return@withContext null
        val panes = mutableListOf<EditorPane>()
        lines.drop(n = 1).forEach { line ->
            when {
                line.startsWith(prefix = PANE_MARK) -> {
                    val active = line.removePrefix(prefix = PANE_MARK).toIntOrNull() ?: return@withContext null
                    panes += EditorPane(active = active)
                }

                line.isEmpty() -> Unit

                else -> {
                    val pane = panes.removeLastOrNull() ?: return@withContext null
                    panes += pane.copy(tabs = pane.tabs + RelativePath(value = line))
                }
            }
        }
        workspaceOf(panes = panes, activePane = activePane)
    }

    override suspend fun write(
        vaultPath: String,
        workspace: Workspace
    ) = withContext(context = dispatchers.io) {
        val text = buildList {
            add(workspace.activePane.toString())
            workspace.panes.forEach { pane ->
                add(PANE_MARK + pane.active)
                pane.tabs.forEach { add(it.value) }
            }
        }.joinToString(separator = LINE_SEPARATOR)
        settings.putString(key = keyOf(vaultPath = vaultPath), value = text)
    }

    /** The panes read, or null when they do not make a workspace (no pane, too many, an active tab out of range). */
    private fun workspaceOf(
        panes: List<EditorPane>,
        activePane: Int
    ): Workspace? {
        val isValid = panes.size in 1..Workspace.MAX_PANES && activePane in panes.indices &&
            panes.all { it.active in EditorPane.NONE..it.tabs.lastIndex && (it.active >= 0) == it.tabs.isNotEmpty() }
        return if (isValid) Workspace(panes = panes, activePane = activePane) else null
    }

    private fun keyOf(
        vaultPath: String
    ): String = KEY_PREFIX + VaultKeyHash.of(vaultPath = vaultPath)

    companion object {
        private const val KEY_PREFIX = "workspace."
        private const val LINE_SEPARATOR = "\n"
        private const val PANE_MARK = "/"
    }
}
