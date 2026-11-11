package app.notekea.core.platform.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.core.platform.domain.SystemAccent
import kotlinx.coroutines.withContext

/**
 * [SystemAccent] through each system's own tool, as the JDK does not read it: `defaults` on macOS, whose accent is one
 * of eight named colors (blue when none was chosen), and the registry on Windows, which keeps it as 0xAABBGGRR. Linux
 * has no common accent, so it is null there. [run] gives what a command printed, null when it cannot start.
 */
class CommandSystemAccent(
    private val operatingSystem: OperatingSystem,
    private val dispatchers: AppDispatchers,
    private val run: (List<String>) -> String? = CommandOutput::of
) : SystemAccent {
    override suspend fun read(): Long? = withContext(context = dispatchers.io) {
        when (operatingSystem) {
            OperatingSystem.LINUX -> null
            OperatingSystem.MACOS -> macAccent(output = run(MACOS_COMMAND))
            OperatingSystem.WINDOWS -> windowsAccent(output = run(WINDOWS_COMMAND))
        }
    }

    // `defaults` fails with no value when the accent is macOS's default, multicolor: blue.
    private fun macAccent(output: String?): Long = output
        ?.trim()
        ?.toIntOrNull()
        ?.let {
            MACOS_ACCENTS[it]
        }
        ?: MACOS_BLUE

    // "AccentColor    REG_DWORD    0xffd77800": alpha, blue, green, red.
    private fun windowsAccent(output: String?): Long? {
        val abgr = output
            ?.let { WINDOWS_VALUE.find(input = it) }
            ?.groupValues
            ?.get(1)
            ?.toLongOrNull(radix = HEX) ?: return null

        val red = abgr and BYTE
        val green = abgr shr BYTE_BITS and BYTE
        val blue = abgr shr (BYTE_BITS * 2) and BYTE

        return OPAQUE or (red shl (BYTE_BITS * 2)) or (green shl BYTE_BITS) or blue
    }

    private companion object {
        const val BYTE = 0xFFL
        const val BYTE_BITS = 8
        const val HEX = 16
        const val MACOS_BLUE = 0xFF007AFFL
        const val OPAQUE = 0xFF000000L
        val MACOS_ACCENTS = mapOf(
            -1 to 0xFF8C8C8CL,
            0 to 0xFFFF5257L,
            1 to 0xFFF7821BL,
            2 to 0xFFFFC600L,
            3 to 0xFF62BA46L,
            4 to MACOS_BLUE,
            5 to 0xFFA550A7L,
            6 to 0xFFF74F9EL
        )
        val MACOS_COMMAND = listOf("defaults", "read", "-g", "AppleAccentColor")
        val WINDOWS_COMMAND =
            listOf("reg", "query", "HKCU\\Software\\Microsoft\\Windows\\DWM", "/v", "AccentColor")
        val WINDOWS_VALUE = Regex(pattern = "REG_DWORD\\s+0x([0-9a-fA-F]+)")
    }
}
