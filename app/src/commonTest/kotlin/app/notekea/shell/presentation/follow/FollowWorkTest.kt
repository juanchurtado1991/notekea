package app.notekea.shell.presentation.follow

import app.notekea.chat.domain.context.WorkContext
import app.notekea.chat.domain.context.WorkPlace
import app.notekea.core.notes.domain.OutlineHeading
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.platform.WindowDialog
import app.notekea.shell.presentation.tabs.TabKind
import kotlin.test.Test
import kotlin.test.assertEquals

/** Where the person is, as the chat is told: the tab, the heading over the cursor, the selection, or Settings. */
class FollowWorkTest {
    private val outline = listOf(
        OutlineHeading(
            level = 1,
            text = "Arepas",
            offset = 0,
            depth = 0
        ),
        OutlineHeading(
            level = 2,
            text = "Ingredients",
            offset = 10,
            depth = 1
        ),
        OutlineHeading(
            level = 2,
            text = "Steps",
            offset = 80,
            depth = 1
        )
    )

    @Test
    fun `a note gives its section and selection, a folder and a view their own, Settings wins`() {
        val arepas = RelativePath(value = "Recipes/Arepas.md")

        assertEquals(
            expected = listOf(
                WorkContext(
                    place = WorkPlace.NOTE,
                    name = "Recipes/Arepas",
                    section = "Ingredients",
                    selected = "salt"
                ),
                WorkContext(
                    place = WorkPlace.FOLDER,
                    name = "Recipes"
                ),
                WorkContext(place = WorkPlace.GRAPH),
                WorkContext(
                    place = WorkPlace.SETTINGS,
                    name = "Sync"
                ),
                null
            ),
            actual = listOf(
                workOf(
                    kind = TabKind.NOTE,
                    path = arepas,
                    selected = "salt"
                ),
                workOf(
                    kind = TabKind.FOLDER,
                    path = RelativePath(value = "Recipes"),
                    selected = "salt"
                ),
                workOf(
                    kind = TabKind.GRAPH,
                    path = null
                ),
                workOf(
                    kind = TabKind.NOTE,
                    path = arepas,
                    dialog = WindowDialog.SYNC
                ),
                workOf(
                    kind = null,
                    path = null
                )
            )
        )
    }

    @Test
    fun `the cursor under the note's own title gives no section`() {
        assertEquals(
            expected = null,
            actual = workContextOf(
                outline = outline,
                cursor = 2,
                kind = TabKind.NOTE,
                path = RelativePath(value = "Recipes/Arepas.md"),
                dialog = null,
                selected = null
            )?.section
        )
    }

    private fun workOf(
        kind: TabKind?,
        path: RelativePath?,
        dialog: WindowDialog? = null,
        selected: String? = null
    ): WorkContext? = workContextOf(
        outline = outline,
        cursor = 42,
        kind = kind,
        path = path,
        dialog = dialog,
        selected = selected
    )
}
