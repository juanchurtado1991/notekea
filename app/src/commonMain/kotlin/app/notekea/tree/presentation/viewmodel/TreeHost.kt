package app.notekea.tree.presentation.viewmodel

import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.ViewModelHost
import app.notekea.tree.presentation.model.VaultTreeEffect
import app.notekea.tree.presentation.model.VaultTreeState

/**
 * What the sidebar's parts share with [VaultTreeViewModel], which routes the intents to them: the [state], the ways to
 * change it ([reduce], or [updateTree], which flattens the rows again and saves the tree's preferences), send an effect
 * ([emit]), start work ([launch]), open an entry in a tab ([open]) and select one before opening it ([reveal]).
 */
internal interface TreeHost : ViewModelHost<VaultTreeState, VaultTreeEffect> {
    fun updateTree(transform: (VaultTreeState) -> VaultTreeState)

    fun open(path: RelativePath?)

    fun reveal(path: RelativePath)
}
