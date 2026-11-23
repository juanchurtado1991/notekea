package app.notekea.tree.presentation.tags

import app.notekea.core.presentation.ViewModelTestBase
import app.notekea.core.presentation.effectsOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TagPickerViewModelTest : ViewModelTestBase() {
    private val viewModel by lazy {
        TagPickerViewModel().apply { onIntent(intent = TagPickerIntent.OnTagsChange(tags = TAGS)) }
    }

    @Test
    fun `it opens on the most used tags, and typing filters them and offers a new one last`() =
        runTest(
            testDispatcher
        ) {
            viewModel.onIntent(intent = TagPickerIntent.OnOpen())
            val suggested = names()
            viewModel.onIntent(intent = TagPickerIntent.OnQueryChange(query = "rec"))

            assertEquals(
                expected =
                TAGS.take(n = 8) to
                    listOf("recipe" to false, "recipe/drinks" to false, "rec" to true),
                actual = suggested to viewModel
                    .state
                    .value
                    .rows
                    .map {
                        it.name to it.isNew
                    }
            )
        }

    @Test
    fun `enter adds the selected tag, a click adds its row, and closing says so`() = runTest(
        testDispatcher
    ) {
        val effects = effectsOf(viewModel = viewModel)
        viewModel.onIntent(intent = TagPickerIntent.OnOpen(query = "weekend plan"))
        viewModel.onIntent(intent = TagPickerIntent.OnConfirm)
        viewModel.onIntent(intent = TagPickerIntent.OnOpen())
        viewModel.onIntent(intent = TagPickerIntent.OnSelectPrevious)
        viewModel.onIntent(intent = TagPickerIntent.OnRowClick(index = 1))
        viewModel.onIntent(intent = TagPickerIntent.OnOpen())
        viewModel.onIntent(intent = TagPickerIntent.OnClose)
        runCurrent()

        assertEquals(
            expected = listOf(
                TagPickerEffect.TagChosen(name = "weekend-plan"),
                TagPickerEffect.TagChosen(name = "recipe"),
                TagPickerEffect.Dismissed
            ),
            actual = effects
        )

        assertEquals(
            expected = TagPickerState(),
            actual = viewModel.state.value
        )
    }

    @Test
    fun `tags that change while it is open update the rows`() = runTest(testDispatcher) {
        viewModel.onIntent(intent = TagPickerIntent.OnOpen(query = "lunch"))
        val before = names()
        viewModel.onIntent(intent = TagPickerIntent.OnTagsChange(tags = TAGS + "lunch"))

        assertEquals(
            expected = listOf("lunch") to listOf("lunch"),
            actual = before to names()
        )

        assertEquals(
            expected = false,
            actual = viewModel
                .state
                .value
                .rows
                .single()
                .isNew
        )
    }

    private fun names(): List<String> = viewModel
        .state
        .value
        .rows
        .map { it.name }

    companion object {
        private val TAGS = listOf(
            "smoke",
            "recipe",
            "crlf",
            "editor",
            "ideas",
            "markdown-extensions",
            "notekea",
            "recipe/drinks",
            "travel"
        )
    }
}
