package app.notekea.graph.presentation.model

import app.notekea.core.notes.domain.RelativePath

/** A small graph for the previews. */
internal object GraphPreviewData {
    private const val RADIUS = 11f
    private val AREPAS = RelativePath(value = "Recipes/Arepas.md")
    private val MENU = RelativePath(value = "Recipes/Weekly menu.md")
    private val TASKS = RelativePath(value = "Guides/Tasks.md")
    private val GARDEN = RelativePath(value = "Projects/Garden plan.md")
    private val SOUP = RelativePath(value = "Recipes/Tomato soup.md")

    val state = GraphState(
        isLoading = false,
        scope = GraphScope.THIS_NOTE,
        center = AREPAS,
        selected = MENU,
        folders = listOf("Guides", "Projects", "Recipes"),
        dots = listOf(
            GraphDot(
                AREPAS,
                "Arepas",
                "Recipes",
                x = 0f,
                y = 0f,
                radius = 18f,
                depth = 0
            ),
            GraphDot(
                MENU,
                "Weekly menu",
                "Recipes",
                x = 140f,
                y = -90f,
                radius = RADIUS,
                depth = 1
            ),
            GraphDot(
                TASKS,
                "Tasks",
                "Guides",
                x = -130f,
                y = -80f,
                radius = RADIUS,
                depth = 1
            ),
            GraphDot(
                GARDEN,
                "Garden plan",
                "Projects",
                x = 40f,
                y = 210f,
                radius = RADIUS,
                depth = 2
            )
        ),
        lines = listOf(
            GraphLine(
                first = AREPAS,
                second = MENU,
                isRelated = false
            ),
            GraphLine(
                first = AREPAS,
                second = TASKS,
                isRelated = false
            ),
            GraphLine(
                first = AREPAS,
                second = GARDEN,
                isRelated = true
            )
        ),
        preview = GraphPreview(
            path = MENU,
            name = "Weekly menu",
            folder = "Recipes",
            snippet = "Monday arepas, Tuesday tomato soup, Wednesday leftovers.",
            connected = listOf(AREPAS),
            suggested = listOf(GARDEN, SOUP)
        )
    )
}
