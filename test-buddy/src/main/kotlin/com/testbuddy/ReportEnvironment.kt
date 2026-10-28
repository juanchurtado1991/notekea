package com.testbuddy

import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.util.Locale

/** Where a report was made, in its second line: `Windows 11 10.0 · es-CO · 1920×1080 at 125%`. */
public data class ReportEnvironment(
    val system: String,
    val language: String,
    val screen: String
) {
    public companion object {
        /** The machine this process runs on; the screen says "no screen" when there is none (CI on Linux). */
        public fun current(): ReportEnvironment = ReportEnvironment(
            system = "${System.getProperty("os.name")} ${System.getProperty("os.version")}",
            language = Locale.getDefault().toLanguageTag(),
            screen = screen()
        )

        private fun screen(): String {
            if (GraphicsEnvironment.isHeadless()) return NO_SCREEN
            val size = Toolkit.getDefaultToolkit().screenSize
            val transform = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration
                .defaultTransform
            val scale = Math.round(transform.scaleX * PERCENT)
            return "${size.width}×${size.height} at $scale%"
        }

        private const val NO_SCREEN = "no screen"
        private const val PERCENT = 100
    }
}
