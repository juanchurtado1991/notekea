package app.notekea.notes.domain

/**
 * The editor's tabs (`ROADMAP.md` 1.11): one pane, or two side by side ([MAX_PANES]), and the [activePane], the one
 * the keyboard works in. A tab shows a note, another file or a folder (its notes as a grid). A path is open in one tab
 * at most, in either pane, so two editors never write the same note:
 * opening it again goes to its tab. Every change returns a new workspace; the active tab always exists while its pane
 * has tabs, and a second pane left without tabs closes.
 */
data class Workspace(
    val panes: List<EditorPane> = listOf(EditorPane()),
    val activePane: Int = 0
) {
    /** The file shown in the active pane, or null when it has no tab. */
    val activePath: RelativePath?
        get() = panes.getOrNull(index = activePane)?.activePath

    val isSplit: Boolean
        get() = panes.size == MAX_PANES

    /** Where [path] is open, as its pane and tab, or null when it is not. */
    fun find(
        path: RelativePath
    ): Pair<Int, Int>? = panes.withIndex().firstNotNullOfOrNull { (pane, it) ->
        it.tabs.indexOf(element = path).takeIf { tab -> tab >= 0 }?.let { tab -> pane to tab }
    }

    /**
     * Opens [path]: goes to its tab when it is open, otherwise adds a tab after the active one of the active pane. A tab
     * opened [inBackground] (a link clicked with `Mod`, or the middle button) is added without leaving the current one.
     */
    fun open(
        path: RelativePath,
        inBackground: Boolean = false
    ): Workspace {
        find(path = path)?.let { (pane, tab) -> return if (inBackground) this else select(pane = pane, tab = tab) }
        val pane = panes[activePane]
        val index = pane.active + 1
        val tabs = pane.tabs.toMutableList().apply { add(index = index, element = path) }
        val active = if (inBackground && pane.active != EditorPane.NONE) pane.active else index
        return withPane(index = activePane, pane = EditorPane(tabs = tabs, active = active))
    }

    fun select(
        pane: Int,
        tab: Int
    ): Workspace {
        val target = panes.getOrNull(index = pane) ?: return this
        if (tab !in target.tabs.indices) return this
        return withPane(index = pane, pane = target.copy(active = tab)).copy(activePane = pane)
    }

    /** The active pane's tab [step] places away, wrapping around at both ends (`Ctrl+Tab`, `Ctrl+Shift+Tab`). */
    fun cycle(
        step: Int
    ): Workspace {
        val pane = panes[activePane]
        if (pane.tabs.isEmpty()) return this
        return select(pane = activePane, tab = (pane.active + step).mod(other = pane.tabs.size))
    }

    /** The active pane's [number]th tab (`Mod+1` to `Mod+8`); 9 is always the last one, as in browsers. */
    fun jump(
        number: Int
    ): Workspace {
        val pane = panes[activePane]
        val tab = if (number >= LAST_TAB_NUMBER) pane.tabs.lastIndex else number - 1
        return select(pane = activePane, tab = tab)
    }

    /** Closes a tab; the one to its right becomes active (or the one to its left, at the end), as in browsers. */
    fun close(
        pane: Int,
        tab: Int
    ): Workspace {
        val target = panes.getOrNull(index = pane) ?: return this
        if (tab !in target.tabs.indices) return this
        val tabs = target.tabs.filterIndexed { index, _ -> index != tab }
        val active = when {
            tabs.isEmpty() -> EditorPane.NONE
            tab < target.active -> target.active - 1
            tab == target.active -> tab.coerceAtMost(maximumValue = tabs.lastIndex)
            else -> target.active
        }
        return withPane(index = pane, pane = EditorPane(tabs = tabs, active = active)).withoutEmptyPanes()
    }

    /** Closes every tab of [pane] but [tab]. */
    fun closeOthers(
        pane: Int,
        tab: Int
    ): Workspace {
        val path = panes.getOrNull(index = pane)?.tabs?.getOrNull(index = tab) ?: return this
        return withPane(index = pane, pane = EditorPane(tabs = listOf(path), active = 0)).copy(activePane = pane)
    }

    /**
     * Split right (`Mod+Shift+D`): the active tab moves to a new pane on the right, which becomes active. With two
     * panes, or no tab, nothing changes.
     */
    fun splitRight(): Workspace {
        val path = activePath ?: return this
        if (isSplit) return this
        return move(path = path, toPane = 1, toIndex = 0)
    }

    /**
     * Moves the tab of [path] to [toPane] at [toIndex] (a tab dragged within its pane or to the other one, or Split
     * right, which may create the second pane), where it becomes active.
     */
    fun move(
        path: RelativePath,
        toPane: Int,
        toIndex: Int
    ): Workspace {
        val (fromPane, fromTab) = find(path = path) ?: return this
        if (toPane >= MAX_PANES) return this
        val withoutTab = withPane(
            index = fromPane,
            pane = panes[fromPane].let { pane ->
                val tabs = pane.tabs.filterIndexed { index, _ -> index != fromTab }
                val active = if (pane.active > fromTab) pane.active - 1 else pane.active
                EditorPane(tabs = tabs, active = active.coerceAtMost(maximumValue = tabs.lastIndex))
            }
        )
        val panesBefore = if (toPane < withoutTab.panes.size) withoutTab.panes else withoutTab.panes + EditorPane()
        val target = panesBefore[toPane]
        val index = toIndex.coerceIn(minimumValue = 0, maximumValue = target.tabs.size)
        val tabs = target.tabs.toMutableList().apply { add(index = index, element = path) }
        val moved = Workspace(panes = panesBefore, activePane = activePane)
            .withPane(index = toPane, pane = EditorPane(tabs = tabs, active = index))
            .copy(activePane = toPane)
        return moved.withoutEmptyPanes()
    }

    /** The pane the keyboard works in, after a click in it. */
    fun focus(
        pane: Int
    ): Workspace = if (pane in panes.indices) copy(activePane = pane) else this

    /** Follows a rename or a move: the tabs of [from], or of anything inside it when it is a folder, now show [to]. */
    fun renamed(
        from: RelativePath,
        to: RelativePath
    ): Workspace = copy(
        panes = panes.map { pane ->
            pane.copy(tabs = pane.tabs.map { if (it.isWithin(folder = from)) it.rebased(from = from, to = to) else it })
        }
    )

    /** Closes the tabs of [path], or of anything inside it when it is a folder (deleted). */
    fun without(
        path: RelativePath
    ): Workspace = closeWhere { it.isWithin(folder = path) }

    /** Closes the tabs whose file is not in [existing] (files deleted while the vault was closed). */
    fun keepingOnly(
        existing: Set<RelativePath>
    ): Workspace = closeWhere { it !in existing }

    private fun closeWhere(
        predicate: (RelativePath) -> Boolean
    ): Workspace {
        var workspace = this
        while (true) {
            val (pane, tab) = workspace.panes.withIndex().firstNotNullOfOrNull { (pane, it) ->
                it.tabs.indexOfFirst(predicate).takeIf { tab -> tab >= 0 }?.let { tab -> pane to tab }
            } ?: return workspace
            workspace = workspace.close(pane = pane, tab = tab)
        }
    }

    private fun withPane(
        index: Int,
        pane: EditorPane
    ): Workspace = copy(panes = panes.mapIndexed { i, it -> if (i == index) pane else it })

    /** Drops panes without tabs while another pane remains, keeping the active pane on a pane that still exists. */
    private fun withoutEmptyPanes(): Workspace {
        if (panes.size < MAX_PANES || panes.none { it.tabs.isEmpty() }) return this
        val kept = panes.withIndex().filter { it.value.tabs.isNotEmpty() }.ifEmpty { listOf(panes.withIndex().first()) }
        val active = kept.indexOfFirst { it.index == activePane }.takeIf { it >= 0 } ?: 0
        return Workspace(panes = kept.map { it.value }, activePane = active)
    }

    companion object {
        const val MAX_PANES = 2
        private const val LAST_TAB_NUMBER = 9
        val EMPTY = Workspace()
    }
}
