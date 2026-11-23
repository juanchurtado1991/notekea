package app.notekea.core.presentation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent

/** Collects [viewModel]'s effects in the test's background from now on: the list grows as the effects come. */
@OptIn(ExperimentalCoroutinesApi::class)
fun <E> TestScope.effectsOf(viewModel: BaseViewModel<*, *, E>): List<E> {
    val effects = mutableListOf<E>()

    backgroundScope.launch {
        viewModel.effects.collect { effects += it }
    }

    runCurrent()

    return effects
}
