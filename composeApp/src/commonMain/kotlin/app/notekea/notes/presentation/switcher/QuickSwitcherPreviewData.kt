package app.notekea.notes.presentation.switcher

import app.notekea.notes.domain.RelativePath

/** Sample values shared by the quick switcher's previews and screen tests: notes of the smoke vault. */
internal object QuickSwitcherPreviewData {
    val noteRows = listOf(
        QuickSwitcherRow(
            path = RelativePath(value = "Recipes/Arepas.md"),
            name = "Arepas",
            nameRanges = listOf(0..2),
            folder = "Recipes"
        ),
        QuickSwitcherRow(
            path = RelativePath(value = "Recipes/Café con leche.md"),
            name = "Café con leche",
            detailRanges = listOf(20..22),
            folder = "Recipes",
            detail = "Morning coffee with arepas"
        ),
        QuickSwitcherRow(
            path = RelativePath(value = "Prepare dough.md"),
            name = "Prepare dough",
            nameRanges = listOf(3..5)
        )
    )

    val contentRows = listOf(
        QuickSwitcherRow(
            path = RelativePath(value = "Recipes/Arepas.md"),
            name = "Arepas",
            detailRanges = listOf(27..30),
            folder = "Recipes",
            detail = "- 2 cups of **pre-cooked** corn flour"
        ),
        QuickSwitcherRow(
            path = RelativePath(value = "Recipes/Pasta e fagioli.md"),
            name = "Pasta e fagioli",
            detailRanges = listOf(10..13),
            folder = "Recipes",
            detail = "Beans and corn, slowly cooked with rosemary until the soup is thick enough to hold a spoon."
        )
    )

    fun notes(): QuickSwitcherState = QuickSwitcherState(isOpen = true, query = "are", rows = noteRows)

    fun content(): QuickSwitcherState = QuickSwitcherState(
        isOpen = true,
        mode = QuickSwitcherMode.CONTENT,
        query = "corn",
        rows = contentRows,
        selectedIndex = 1
    )
}
