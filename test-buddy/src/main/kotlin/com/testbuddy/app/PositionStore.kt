package com.testbuddy.app

import java.nio.file.Files
import java.nio.file.Path

/** Where the tester is in the manager (the tab, the manual case and its step), kept in [file] across restarts. */
internal class PositionStore(private val file: Path) {
    data class Position(
        val tab: ManagerTab,
        val index: Int,
        val step: Int
    )

    fun load(): Position? {
        if (!Files.isRegularFile(file)) return null
        val fields = Files.readString(file).trim().split("\t")
        val tab = ManagerTab.entries.firstOrNull { it.name == fields.getOrNull(0) } ?: return null
        return Position(
            tab = tab,
            index = fields.getOrNull(1)?.toIntOrNull() ?: 0,
            step =
            fields.getOrNull(2)?.toIntOrNull() ?: 0
        )
    }

    fun save(
        tab: ManagerTab,
        index: Int,
        step: Int
    ) {
        Files.createDirectories(file.parent)
        Files.writeString(file, "${tab.name}\t$index\t$step")
    }
}
