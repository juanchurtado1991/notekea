package app.notekea.core.platform.data

/**
 * Reads the color scheme in what `gdbus` prints for the freedesktop settings portal (`org.freedesktop.appearance
 * color-scheme`): `1` asks for dark, `2` for light and `0` says nothing. It reads the answer to a call
 * (`(<uint32 1>,)`) and the line a change prints when monitored (`SettingChanged ('org.freedesktop.appearance',
 * 'color-scheme', <uint32 1>)`); any other line says nothing either.
 */
class PortalColorSchemeParser {
    fun isDark(
        output: String
    ): Boolean? {
        val isChange = CHANGE_MARK in output
        if (isChange && SETTING !in output) return null
        return when (VALUE.find(input = output)?.groupValues?.get(index = 1)) {
            DARK -> true
            LIGHT -> false
            else -> null
        }
    }

    companion object {
        private const val CHANGE_MARK = "SettingChanged"
        private const val DARK = "1"
        private const val LIGHT = "2"
        private const val SETTING = "'org.freedesktop.appearance', 'color-scheme'"
        private val VALUE = Regex(pattern = "<uint32 (\\d+)>")
    }
}
