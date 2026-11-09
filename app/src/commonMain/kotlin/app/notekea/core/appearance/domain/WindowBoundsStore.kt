package app.notekea.core.appearance.domain

/** Where the main window's [WindowBounds] are kept between runs. */
interface WindowBoundsStore {
    /** The bounds the window was left with; null before it was ever kept. */
    suspend fun bounds(): WindowBounds?

    suspend fun save(bounds: WindowBounds)
}
