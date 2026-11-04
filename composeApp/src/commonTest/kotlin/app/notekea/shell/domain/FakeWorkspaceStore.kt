package app.notekea.shell.domain

import kotlinx.coroutines.delay
import app.notekea.notes.domain.Gate

/**
 * Each vault's tabs in memory; every call goes through [gate], as the disk takes its time, and a write takes as long
 * as the next of [writeMillis], as a disk may.
 */
class FakeWorkspaceStore : WorkspaceStore {
    val workspaces = mutableMapOf<String, Workspace>()
    val gate = Gate()
    val writeMillis = ArrayDeque<Long>()

    /** Thrown by every write, as a bug in the store would. */
    var failure: Throwable? = null

    override suspend fun read(
        vaultPath: String
    ): Workspace? {
        gate.pass()
        return workspaces[vaultPath]
    }

    override suspend fun write(
        vaultPath: String,
        workspace: Workspace
    ) {
        gate.pass()
        failure?.let { throw it }
        writeMillis.removeFirstOrNull()?.let { delay(timeMillis = it) }
        workspaces[vaultPath] = workspace
    }
}
