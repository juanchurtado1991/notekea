package app.notekea.notes.presentation.tree

import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

/**
 * What the sidebar's parts (bookmarks, daily notes, templates) share with [VaultTreeViewModel], which routes the
 * intents to them: the [state], the ways to change it ([reduce], or [updateTree], which flattens the rows again and
 * saves the tree's preferences), send an effect ([emit]), start work ([launch]), open an entry in a tab ([open]) and
 * select one with its folders expanded before opening it ([reveal]).
 */
internal interface TreeHost {
    val state: StateFlow<VaultTreeState>

    fun reduce(
        transform: (VaultTreeState) -> VaultTreeState
    )

    fun updateTree(
        transform: (VaultTreeState) -> VaultTreeState
    )

    fun emit(
        effect: VaultTreeEffect
    )

    fun launch(
        block: suspend () -> Unit
    ): Job

    fun open(
        path: RelativePath?
    )

    fun reveal(
        path: RelativePath
    )
}
