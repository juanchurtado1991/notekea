package app.notekea.chat.domain.plan

import app.notekea.core.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/** The folders a step of the chat may name, from the vault's notes. */
class ActionContextTest {
    @Test
    fun `a folder that holds only folders is one a step may name`() {
        val folders = ActionContext.foldersOf(
            paths = listOf(
                RelativePath(value = "Projects/Garden/Tomato plan.md"),
                RelativePath(value = "Home.md")
            )
        )

        assertEquals(
            expected = listOf("Projects", "Projects/Garden"),
            actual = folders
        )
    }
}
