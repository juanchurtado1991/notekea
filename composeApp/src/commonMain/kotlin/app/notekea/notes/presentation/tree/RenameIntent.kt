package app.notekea.notes.presentation.tree

/** The intents of a rename (`ROADMAP.md` 1.4, 2.5), which [TreeRenaming] handles: the name, and the links after it. */
sealed interface RenameIntent : VaultTreeIntent
