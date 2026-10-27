package app.notekea.core.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.theme.palette.NotekeaPalette

/**
 * Notekea's logo, as a vector the window, About and the welcome screen draw at any size. A placeholder until the logo
 * is designed: a cobalt rounded square with a white "N". The designed one replaces [Logo]'s paths, on a 1024 by 1024
 * viewport, and the packaged app icons are drawn from it.
 */
object NotekeaBrand {
    private const val LOGO_NAME = "NotekeaLogo"
    private const val LOGO_SIZE_DP = 64
    private const val LOGO_VIEWPORT = 1024f
    private const val SQUARE_PATH =
        "M224 0H800A224 224 0 0 1 1024 224V800A224 224 0 0 1 800 1024H224A224 224 0 0 1 0 800V224A224 224 0 0 1 224 0Z"
    private const val LETTER_PATH = "M300 760V264H392L632 616V264H724V760H632L392 408V760Z"

    val Logo: ImageVector by lazy {
        ImageVector.Builder(
            name = LOGO_NAME,
            defaultWidth = LOGO_SIZE_DP.dp,
            defaultHeight = LOGO_SIZE_DP.dp,
            viewportWidth = LOGO_VIEWPORT,
            viewportHeight = LOGO_VIEWPORT
        )
            .addPath(
                pathData = addPathNodes(pathStr = SQUARE_PATH),
                fill = SolidColor(value = NotekeaPalette.PrimaryLight)
            )
            .addPath(pathData = addPathNodes(pathStr = LETTER_PATH), fill = SolidColor(value = Color.White))
            .build()
    }
}
