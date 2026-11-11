package app.notekea.core.platform.domain

/**
 * The system's accent color (Settings → Appearance, macOS redesign 4B), as 0xAARRGGBB: read once, when the app starts;
 * null where the system has none Notekea can read (Linux).
 */
interface SystemAccent {
    suspend fun read(): Long?
}
