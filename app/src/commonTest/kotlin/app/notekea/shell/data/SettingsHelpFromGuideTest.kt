package app.notekea.shell.data

import app.notekea.chat.domain.help.AnswerHelpUseCase
import app.notekea.chat.domain.help.HelpGuide
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.settings.domain.SettingsTopic
import app.notekea.settings.domain.SettingsHelpError
import app.notekea.settings.domain.SettingsHelpStep
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Ask about Settings from the guide: the open section's topic read whatever is asked, in the question's language. */
class SettingsHelpFromGuideTest {
    private val engine = FakeLlmEngine()

    private val helper = SettingsHelpFromGuide(
        answerHelp = AnswerHelpUseCase(
            llmEngine = engine,
            guideSource = { HelpGuide.of(markdown = GUIDE) }
        ),
        conventions = PreviewPlatformConventions.linux
    )

    @Test
    fun `a question on a token reads the setup steps, and is answered in Spanish`() = runTest {
        engine.reply = { "Una llave." }

        val steps = helper
            .answer(
                question = "¿qué es eso?",
                topic = SettingsTopic.SYNC,
                history = emptyList()
            )
            .toList()

        val prompt = engine
            .generated
            .single()
            .joinToString { it.text }

        assertEquals(
            expected = listOf<Any?>(SettingsHelpStep.Done(text = "Una llave."), true, false, true),
            actual = listOf(
                steps.last(),
                SETUP_STEP in prompt,
                OTHER_TOPIC in prompt,
                "Spanish" in prompt
            )
        )
    }

    @Test
    fun `without a model it says so`() = runTest {
        engine.failure = LlmError.NoModel

        assertEquals(
            expected = SettingsHelpStep.Failed(error = SettingsHelpError.NoModel),
            actual = helper
                .answer(
                    question = "what is a token?",
                    topic = SettingsTopic.SYNC,
                    history = emptyList()
                )
                .toList()
                .last()
        )
    }

    private companion object {
        const val SETUP_STEP = "Make a free account at github.com."
        const val OTHER_TOPIC = "Bold text"

        val GUIDE = """
            # Make text bold
            words: bold, negrita
            $OTHER_TOPIC with {bold}.

            # Set up GitHub sync, step by step
            words: github, token, set up sync
            1. $SETUP_STEP
        """.trimIndent()
    }
}
