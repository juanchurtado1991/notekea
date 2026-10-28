package app.notekea.core.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.theme.palette.NotekeaPalette

/**
 * Notekea's logo, as vectors the window, About, the welcome screen and the rail draw at any size: a kea and an "N" on a
 * note with a tangerine folded corner, in ink like the text, so it sits quietly beside the editor. [Logo] is for light
 * backgrounds (an ink note, the mark in the background's color) and [LogoDark] for dark ones, where the note turns
 * light; [NotekeaLogo][app.notekea.core.presentation.components.NotekeaLogo] picks the one that fits the theme. The
 * mark is clipped to the note's inner square, as the designed SVG does.
 */
object NotekeaBrand {
    private const val LOGO_NAME = "NotekeaLogo"
    private const val LOGO_SIZE_DP = 64
    private const val VIEWPORT_WIDTH = 718f
    private const val VIEWPORT_HEIGHT = 719f
    private const val MARK_CLIP_PATH = "M62 88H593V619H62Z"
    private const val SQUARE_PATH = "M468,1H156.37A156.37,156.37,0,0,0,0,157.37V562.63A156.37,156.37,0,0,0,156.37,719" +
        "H561.63A156.37,156.37,0,0,0,718,562.63V251Z"
    private const val FOLD_PATH = "M499 212L718 250L468 0Z"
    private const val MARK_TOP_PATH = "M277.77,260.87q3,1,5.92,2.27a51,51,0,0,0-10.45-3.71C274.75,259.88,276.26," +
        "260.35,277.77,260.87Z"
    private const val MARK_PATH = "M264.75,553.37a212.11,212.11,0,0,1-26.09-32.53,104.85,104.85,0,0,1-12.9-27.65" +
        "c-.51-1.83-1-3.67-1.54-5.67-.52.9,0,2.19-1.16,2.3-1,.08-1.35-1-1.71-1.71-1-2-2-4-2.75-6a116.15,116.15,0,0," +
        "1-7.74-44c0-2.62,0-5.23,0-7.87-1.23.36-.7,2.69-2.15,2.18-1.24-.44-1.14-2.17-1.47-3.39a9.09,9.09,0,0," +
        "1-.21-1.27c-1.35-9.33-.39-18.51,1.78-27.61,1.89-7.94,4.8-15.55,7.48-23.23a22.5,22.5,0,0,0,.56-12.46" +
        "c-.94-4.3-3.7-7.69-5.95-11.32-2.66-4.3-4.89-8.68-3.59-14.24a8.41,8.41,0,0,0-1.14.68A37,37,0,0,1,185.28,348" +
        "a2.92,2.92,0,0,0-2.07,1.33c-2.75,3.26-4.15,7.12-5.14,11.19-1.48,6-.5,12.07,0,18.1.06.64.64,1.55-.23," +
        "1.92-.72.3-1.28-.5-1.79-.94a31.26,31.26,0,0,1-7.22-9.24c-8.18-15.59-8.2-31.55-1.48-47.56,4-9.46,10.18-17.48," +
        "17.62-24.54.68-.63,1.46-1.18,1.43-2.26-.15-5.16,2.24-9.39,5.1-13.36,9.68-13.38,22.83-21.56,38.8-25.23," +
        "14.56-3.34,28.86-2.18,42.9,2a51,51,0,0,1,10.45,3.71c14.07,5.91,26,15,36.3,26.45,5,5.59,9.69,11.46,12.62," +
        "18.49,1.09,2.62,1.07,2.63-1.65,2.72a7.32,7.32,0,0,0,1.61,2.48c7.28,9.85,13.09,20.42,16.13,32.38.28,1.08,1," +
        "2.42.19,3.26s-1.91-.49-2.94-.54a1.71,1.71,0,0,0-.5.16,18.67,18.67,0,0,0,3.3,4.43c5.76,6.45,12.16,12.28," +
        "18.08,18.57a70.87,70.87,0,0,1,9.47,12.14c.42.71,1.3,1.57.78,2.29s-.43.78.18,1.15c2.39,1.43,4.72,3,7.13,4.35" +
        "a231.47,231.47,0,0,1,44.89,33.8,209.43,209.43,0,0,1,35,43.65,41.38,41.38,0,0,1,3.85,8.81c.17.53.44,1.18-.07," +
        "1.61s-1.09,0-1.59-.21c-1.08-.47-2.13-1-3.19-1.48a45,45,0,0,0,4.57,5.49c8.71,9,16.93,18.39,24.76,28.13" +
        "a456.35,456.35,0,0,1,38.27,55.25c1.69,2.89,3.37,5.79,4.87,8.78,14.68,3.1,29.68,4,44.83,3.86,5,0,6.27-1.91," +
        "6.27-6.79q-.21-185.86-.13-371.73c0-3.78-.11-7.57.16-11.34,1.2-16.8,8.59-25.06,25.17-27.9,3.3-.56,4.93-1.89," +
        "4.89-5.22,0-2.1,0-4.2,0-6.3,0-8.82,0-8.81-9-8.8q-84.75,0-169.49,0c-13.9,0-14.47.65-13.3,14.63.3,3.47,1.58," +
        "5.13,5.25,5.68,16.5,2.5,24.82,12.13,24.84,28.61q.09,78.45,0,156.88c0,2.09.51,4.37-1,6.72a29,29,0,0," +
        "1-1.83-2.42" +
        "c-14.94-25.8-29.71-51.69-44.81-77.39-13.08-22.23-24.43-45.51-39.77-66.39-32.31-43.95-76.63-64.87" +
        "-130.32-66.1-40.52-.92-81.07-.2-121.6-.23-15.12," +
        "0-15,0-15,14.88,0,4.76.87,7.1,6.57,7.19,14,.22,25,10.76,26.56,24.6a108.25,108.25,0,0,1,.76,11.93q.09," +
        "160.66.05,321.32a111.88,111.88,0,0,1-.94,13.81c-1.35,10.58-10.13,20.15-19.87,22-9.42,1.78-9.42,1.78-9.38," +
        "11.13s0,9.39,9.47,9.39c57.54,0,115.09.08,172.63-.06h2.35Q276.59,565.71,264.75,553.37Z"
    private const val EYE_PATH = "M225.12,289.3a7.33,7.33,0,0,0-7.54,7.59,7,7,0,0,0,7.23,7.33c4.57,0,7.61-2.91," +
        "7.61-7.38S229.42,289.33,225.12,289.3Z"
    private const val MARK_DOT_PATH = "M302.93,560.76v.06l.06,0Z"

    val Logo: ImageVector by lazy {
        logoOf(note = NotekeaPalette.InkLight, mark = NotekeaPalette.BackgroundLight, fold = NotekeaPalette.AccentLight)
    }

    val LogoDark: ImageVector by lazy {
        logoOf(note = NotekeaPalette.InkDark, mark = NotekeaPalette.BackgroundDark, fold = NotekeaPalette.AccentDark)
    }

    private fun logoOf(
        note: Color,
        mark: Color,
        fold: Color
    ): ImageVector {
        val markFill = SolidColor(value = mark)
        return ImageVector.Builder(
            name = LOGO_NAME,
            defaultWidth = LOGO_SIZE_DP.dp,
            defaultHeight = LOGO_SIZE_DP.dp,
            viewportWidth = VIEWPORT_WIDTH,
            viewportHeight = VIEWPORT_HEIGHT
        )
            .addPath(pathData = addPathNodes(pathStr = SQUARE_PATH), fill = SolidColor(value = note))
            .addGroup(clipPathData = addPathNodes(pathStr = MARK_CLIP_PATH))
            .addPath(pathData = addPathNodes(pathStr = MARK_TOP_PATH), fill = markFill)
            .addPath(pathData = addPathNodes(pathStr = MARK_PATH), fill = markFill)
            .addPath(pathData = addPathNodes(pathStr = EYE_PATH), fill = markFill)
            .addPath(pathData = addPathNodes(pathStr = MARK_DOT_PATH), fill = markFill)
            .clearGroup()
            .addPath(
                pathData = addPathNodes(pathStr = FOLD_PATH),
                fill = SolidColor(value = fold)
            )
            .build()
    }
}
