package com.testbuddy.crash

import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.util.Locale

/**
 * The machine a report comes from: the system, Java, the language and the screen with its scale
 * (`Windows 11 10.0 · Java 21.0.5 · es-CO · 1920×1080 at 125%`).
 */
public data class AppEnvironment(
    val system: String,
    val java: String,
    val language: String,
    val screen: String
) {
    /** One line per fact, as reports list them. */
    public fun lines(): List<String> = listOf("OS: $system", "Java: $java", "Language: $language", "Screen: $screen")

    public companion object {
        /** This process's machine; the screen says "no screen" when there is none (CI on Linux). */
        public fun current(): AppEnvironment = AppEnvironment(
            system = "${System.getProperty(
                "os.name"
            )} ${System.getProperty("os.version")} (${System.getProperty("os.arch")})",
            java = System.getProperty("java.version").orEmpty(),
            language = Locale.getDefault().toLanguageTag(),
            screen = screen()
        )

        private fun screen(): String {
            if (GraphicsEnvironment.isHeadless()) return NO_SCREEN
            val size = Toolkit.getDefaultToolkit().screenSize
            val transform = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration
                .defaultTransform
            return "${size.width}×${size.height} at ${Math.round(transform.scaleX * PERCENT)}%"
        }

        private const val NO_SCREEN = "no screen"
        private const val PERCENT = 100
    }
}
