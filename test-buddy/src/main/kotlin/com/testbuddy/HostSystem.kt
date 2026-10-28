package com.testbuddy

/** The operating system a suite runs on, which decides the keys of a [Shortcut]. */
public enum class HostSystem {
    WINDOWS,
    MACOS,
    LINUX;

    public companion object {
        /** The system this process runs on; any system that is neither Windows nor macOS is treated as Linux. */
        public fun current(): HostSystem = of(osName = System.getProperty("os.name").orEmpty())

        internal fun of(osName: String): HostSystem {
            val name = osName.lowercase()
            return when {
                name.startsWith(prefix = "windows") -> WINDOWS
                name.startsWith(prefix = "mac") -> MACOS
                else -> LINUX
            }
        }
    }
}
