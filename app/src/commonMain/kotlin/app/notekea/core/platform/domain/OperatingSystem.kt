package app.notekea.core.platform.domain

/**
 * Desktop operating systems Notekea adapts its conventions to (`PLATFORM_GUIDELINES.md`).
 * Any Unix-like system that is not macOS (FreeBSD, unknown names) follows the [LINUX] conventions.
 */
enum class OperatingSystem {
    LINUX,
    MACOS,
    WINDOWS
}
