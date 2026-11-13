package app.notekea.chat.presentation

import app.notekea.chat.domain.help.HelpGuide
import app.notekea.chat.presentation.viewmodel.HelpShortcuts
import app.notekea.core.platform.domain.FakeSystemEnvironment
import app.notekea.core.platform.domain.ResolvePlatformConventionsUseCase
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The guide shipped with the app (`files/guide/guide.md`): every shortcut it names exists, every topic has words to
 * be found by, and the questions people asked find the topic that answers them.
 */
class HelpGuideFileTest {
    private val text = File("src/commonMain/composeResources/files/guide/guide.md").readText()
    private val guide = HelpGuide.of(markdown = text)

    @Test
    fun `every shortcut the guide names is one of the app's, written as each system writes it`() {
        val (linux, macOs) = listOf("Linux", "Mac OS X").map { name ->
            val conventions = ResolvePlatformConventionsUseCase(
                systemEnvironment = FakeSystemEnvironment(
                    operatingSystemName = name,
                    userHome = "/home/ana"
                )
            ).invoke()

            HelpShortcuts.of(conventions = conventions)
        }

        val unknown =
            HelpGuide.shortcutsIn(text = guide.topics.joinToString { it.text }) - linux.keys

        assertEquals(
            expected = emptySet<String>() to listOf("Ctrl+N", "⌘N"),
            actual = unknown to listOf(linux, macOs).map {
                it["newNote"]
            }
        )
    }

    @Test
    fun `every topic has a title, words and text, and the titles differ`() {
        val incomplete = guide.topics.filter {
            it.words.isEmpty() ||
                it.text.isBlank() ||
                it.title.isBlank()
        }

        assertEquals(
            expected = emptyList<String>() to guide.topics.size,
            actual = incomplete.map { it.title } to guide
                .topics
                .map { it.title }
                .distinct()
                .size
        )
    }

    @Test
    fun `questions people ask find the topic that answers them`() {
        val asked = mapOf(
            "¿cómo creo una nota nueva?" to "Create a note",
            "how do I create a note?" to "Create a note",
            "que puedes hacer por mi?" to "What the chat can do",
            "how do I see my tasks?" to "Tasks",
            "¿cómo cambio el tema a modo oscuro?" to "Color theme and appearance",
            "how do I open today's note" to "Today's note",
            "¿cómo sincronizo mis notas con github?" to "Set up GitHub sync, step by step",
            "¿cómo cambio el idioma a español?" to "Settings: General",
            "how do I make the text size bigger?" to "Settings: Editor",
            "¿dónde cambio la carpeta de adjuntos?" to "Settings: Files",
            "what do I do with a conflict copy?" to "Settle a sync conflict",
            "¿puedo buscar en mis pdf?" to "Documents: PDF, Word, Excel and more",
            "how do I clip a web page?" to "Clip a web page",
            "¿cómo hago tarjetas de estudio?" to "Study cards"
        )

        assertEquals(
            expected = asked.values.toList(),
            actual = asked.keys.map { question ->
                guide
                    .about(question = question)
                    .firstOrNull()
                    ?.title
            }
        )
    }
}
