package app.notekea.settings.presentation.help

import app.notekea.core.presentation.settings.SettingsSpotlight
import app.notekea.core.settings.domain.SettingsPlace
import app.notekea.core.settings.domain.SettingsTopic
import app.notekea.settings.domain.SettingsHelpError
import app.notekea.settings.domain.SettingsHelpExchange
import app.notekea.settings.domain.SettingsHelpStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Settings → Sync's help (the developer's request of 2026-11-11): questions answered, follow-ups, no model, Stop. */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsHelpViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a question is answered under it, and the next one is asked with it`() =
        runTest(dispatcher) {
            val asked = mutableListOf<List<SettingsHelpExchange>>()

            val viewModel = SettingsHelpViewModel(
                helper = { question, _, history ->
                    asked += history
                    flowOf(SettingsHelpStep.Writing(text = "A key"), SettingsHelpStep.Done(text = "A key for $question"))
                }
            )

            ask(
                viewModel = viewModel,
                question = "what is GitHub?"
            )

            ask(
                viewModel = viewModel,
                question = "is it free?"
            )

            assertEquals(
                expected = listOf<Any?>(
                    listOf(
                        SettingsHelpExchange(
                            question = "what is GitHub?",
                            reply = "A key for what is GitHub?"
                        ),
                        SettingsHelpExchange(
                            question = "is it free?",
                            reply = "A key for is it free?"
                        )
                    ),
                    listOf(
                        SettingsHelpExchange(
                            question = "what is GitHub?",
                            reply = "A key for what is GitHub?"
                        )
                    ),
                    false,
                    ""
                ),
                actual = listOf(
                    viewModel.state.value.exchanges,
                    asked.last(),
                    viewModel.state.value.isAnswering,
                    viewModel.state.value.draft
                )
            )
        }

    @Test
    fun `without a model it says so, and an empty question is not asked`() = runTest(dispatcher) {
        var calls = 0

        val viewModel = SettingsHelpViewModel(
            helper = { _, _, _ ->
                calls++
                flowOf(SettingsHelpStep.Failed(error = SettingsHelpError.NoModel))
            }
        )

        ask(
            viewModel = viewModel,
            question = "   "
        )

        ask(
            viewModel = viewModel,
            question = "what is a token?"
        )

        assertEquals(
            expected = listOf<Any?>(1, SettingsHelpError.NoModel, false),
            actual = listOf(calls, viewModel.state.value.error, viewModel.state.value.isAnswering)
        )
    }

    @Test
    fun `Stop keeps the reply as written so far`() = runTest(dispatcher) {
        val viewModel = SettingsHelpViewModel(
            helper = { _, _, _ ->
                flow {
                    emit(value = SettingsHelpStep.Writing(text = "GitHub is"))
                    awaitCancellation()
                }
            }
        )

        ask(
            viewModel = viewModel,
            question = "what is GitHub?"
        )

        val writing = viewModel.state.value.isAnswering
        viewModel.onIntent(intent = SettingsHelpIntent.OnStop)
        runCurrent()

        assertEquals(
            expected = listOf<Any?>(true, false, "GitHub is"),
            actual = listOf(
                writing,
                viewModel.state.value.isAnswering,
                viewModel
                    .state
                    .value
                    .exchanges
                    .last()
                    .reply
            )
        )
    }

    // The developer's request of 2026-11-11: the chat opens the setting it is about, as an intent opens a screen.
    @Test
    fun `the setting a question names is shown at once, one its reply names after it, and Show again`() =
        runTest(dispatcher) {
            val viewModel = SettingsHelpViewModel(
                helper = { _, _, _ ->
                    flowOf(SettingsHelpStep.Done(text = "Paste the access token GitHub gave you."))
                }
            )

            ask(
                viewModel = viewModel,
                question = "¿dónde va la dirección del repositorio?"
            )

            val byQuestion = viewModel.state.value.spotlight

            ask(
                viewModel = viewModel,
                question = "and then?"
            )

            val byReply = viewModel.state.value.spotlight
            val again = SettingsHelpIntent.OnShow(place = SettingsPlace.SYNC_ADDRESS)
            viewModel.onIntent(intent = again)

            assertEquals(
                expected = listOf<Any?>(
                    SettingsSpotlight(
                        place = SettingsPlace.SYNC_ADDRESS,
                        serial = 1
                    ),
                    SettingsSpotlight(
                        place = SettingsPlace.SYNC_TOKEN,
                        serial = 2
                    ),
                    SettingsSpotlight(
                        place = SettingsPlace.SYNC_ADDRESS,
                        serial = 3
                    ),
                    listOf(SettingsPlace.SYNC_ADDRESS, SettingsPlace.SYNC_TOKEN)
                ),
                actual = listOf(
                    byQuestion,
                    byReply,
                    viewModel.state.value.spotlight,
                    viewModel
                        .state
                        .value
                        .exchanges
                        .map { it.place }
                )
            )
        }

    @Test
    fun `without a model the setting a question names is still shown`() = runTest(dispatcher) {
        val viewModel = SettingsHelpViewModel(
            helper = { _, _, _ ->
                flowOf(SettingsHelpStep.Failed(error = SettingsHelpError.NoModel))
            }
        )

        ask(
            viewModel = viewModel,
            question = "how do I change the theme?"
        )

        assertEquals(
            expected = SettingsPlace.APPEARANCE_THEME,
            actual = viewModel.state.value.spotlight?.place
        )
    }

    private fun TestScope.ask(
        viewModel: SettingsHelpViewModel,
        question: String
    ) {
        viewModel.onIntent(intent = SettingsHelpIntent.OnDraftChange(text = question))
        viewModel.onIntent(intent = SettingsHelpIntent.OnAsk(topic = SettingsTopic.SYNC))
        runCurrent()
    }
}
