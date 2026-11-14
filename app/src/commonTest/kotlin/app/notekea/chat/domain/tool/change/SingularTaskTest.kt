package app.notekea.chat.domain.tool.change

import kotlin.test.Test
import kotlin.test.assertEquals

/** The one thing a task names with "the", which a change to several notes may have guessed (step 5's measure). */
class SingularTaskTest {
    @Test
    fun `a task that names one thing with the gives it, and one that names several gives none`() {
        assertEquals(
            expected = listOf(PROJECT, "the supplier meeting", "el proyecto a", null, null, null),
            actual = listOf(
                "move the project to Archive",
                "summarize the supplier meeting",
                "mueve el proyecto a Archivo",
                "tag the notes about my home renovation with house",
                "link the weekly menu to every recipe it uses",
                "compare the two supplier meetings in a note"
            ).map { SingularTask.oneIn(task = it) }
        )
    }

    private companion object {
        const val PROJECT = "the project to"
    }
}
