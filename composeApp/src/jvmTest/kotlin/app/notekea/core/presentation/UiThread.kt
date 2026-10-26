package app.notekea.core.presentation

import javax.swing.SwingUtilities

/**
 * Runs headless screen tests on the AWT event thread, as the app runs its window (`ARCHITECTURE.md` §8). Compose
 * resumes some effects there (a scroll, a recomposition after background work), so a scene rendered from the test's
 * own thread is used from two threads and fails at random, often in whichever test runs next.
 */
object UiThread {
    /** Runs [block] on the AWT event thread and rethrows what it threw, so a failed assertion still fails the test. */
    fun run(
        block: () -> Unit
    ) {
        var failure: Throwable? = null
        SwingUtilities.invokeAndWait {
            try {
                block()
            } catch (throwable: Throwable) {
                failure = throwable
            }
        }
        failure?.let { throw it }
    }
}
