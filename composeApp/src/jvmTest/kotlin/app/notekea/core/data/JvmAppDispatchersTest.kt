package app.notekea.core.data

import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertSame

class JvmAppDispatchersTest {
    private val dispatchers = JvmAppDispatchers()

    @Test
    fun `background dispatchers map to the coroutine defaults`() {
        assertSame(
            expected = Dispatchers.Default,
            actual = dispatchers.default
        )
        assertSame(
            expected = Dispatchers.IO,
            actual = dispatchers.io
        )
    }

    @Test
    fun `main dispatcher is the desktop main dispatcher`() {
        assertSame(
            expected = Dispatchers.Main,
            actual = dispatchers.main
        )
    }
}
