package app.notekea.core.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * The set-up every ViewModel test shares: [testDispatcher] is `Dispatchers.Main` while a test runs, so the
 * ViewModel's scope runs on the test's scheduler (`runTest(testDispatcher)`).
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class ViewModelTestBase {
    protected val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setMainDispatcher() {
        Dispatchers.setMain(dispatcher = testDispatcher)
    }

    @AfterTest
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }
}
