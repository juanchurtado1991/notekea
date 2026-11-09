package app.notekea.core.presentation.platform

import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.core.platform.domain.PlatformConventions

/*
 * Paths as users read them: on macOS and Linux the home folder is written "~", as their file managers and terminals
 * do; Windows users never see "~", so their paths stay as they are.
 */

private const val HOME_SYMBOL = "~"
private const val UNIX_SEPARATOR = "/"

fun displayPath(
    path: String,
    platformConventions: PlatformConventions
): String {
    val home = platformConventions.homeDirectory.removeSuffix(suffix = UNIX_SEPARATOR)
    val usesHomeSymbol = platformConventions.operatingSystem != OperatingSystem.WINDOWS && home.isNotEmpty()
    return when {
        !usesHomeSymbol -> path
        path == home -> HOME_SYMBOL
        path.startsWith(prefix = home + UNIX_SEPARATOR) -> HOME_SYMBOL + path.removePrefix(prefix = home)
        else -> path
    }
}
