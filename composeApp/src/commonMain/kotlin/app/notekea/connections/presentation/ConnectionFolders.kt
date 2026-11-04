package app.notekea.connections.presentation

import app.notekea.core.notes.domain.RelativePath

/** The folder a Connections row names beside its note: empty for a note at the vault's root. */
fun folderOf(
    path: RelativePath
): String = path.parent?.takeUnless { it.isRoot }?.value.orEmpty()
