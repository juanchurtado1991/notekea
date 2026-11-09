package app.notekea.core.platform.domain

/** The system's reduce-motion setting: read once, when the app starts; false where it cannot be read. */
interface SystemMotion {
    suspend fun isReduced(): Boolean
}
