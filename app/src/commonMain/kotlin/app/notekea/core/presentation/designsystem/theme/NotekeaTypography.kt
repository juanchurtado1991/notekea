package app.notekea.core.presentation.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.notekea.resources.Res
import app.notekea.resources.inter_bold
import app.notekea.resources.inter_medium
import app.notekea.resources.inter_regular
import app.notekea.resources.inter_semibold
import app.notekea.resources.jetbrains_mono_bold
import app.notekea.resources.jetbrains_mono_regular
import org.jetbrains.compose.resources.Font

/*
 * Bundled typefaces (Inter for UI and text, JetBrains Mono for code) and two scales. The interface follows macOS's
 * (macOS redesign 3A, 2026-11-11): text 13, small 11, titles 15 / 17 / 22. The note keeps its reading scale in slots no
 * interface text uses: `displayMedium` (H1, 28), `displaySmall` (H2, 20) and `bodyLarge` (16, the body and H3–H6).
 * Built inside composition because `Font()` reads Compose resources there.
 */

private const val CODE_SIZE_SP = 14
private const val NOTE_H1_SIZE_SP = 28
private const val NOTE_H2_SIZE_SP = 20
private const val NOTE_SIZE_SP = 16
private const val READING_LINE_HEIGHT = 1.6f
private const val UI_BODY_SIZE_SP = 13
private const val UI_LARGE_TITLE_SIZE_SP = 22
private const val UI_MINI_SIZE_SP = 10
private const val UI_SMALL_SIZE_SP = 11
private const val UI_TITLE_SIZE_SP = 17
private const val UI_SUBTITLE_SIZE_SP = 15

@Composable
fun notekeaCodeTextStyle(): TextStyle = TextStyle(
    fontFamily = FontFamily(
        Font(
            resource = Res.font.jetbrains_mono_regular,
            weight = FontWeight.Normal
        ),
        Font(
            resource = Res.font.jetbrains_mono_bold,
            weight = FontWeight.Bold
        )
    ),
    fontSize = CODE_SIZE_SP.sp
)

@Composable
fun notekeaTypography(): Typography {
    val inter = FontFamily(
        Font(
            resource = Res.font.inter_regular,
            weight = FontWeight.Normal
        ),
        Font(
            resource = Res.font.inter_medium,
            weight = FontWeight.Medium
        ),
        Font(
            resource = Res.font.inter_semibold,
            weight = FontWeight.SemiBold
        ),
        Font(
            resource = Res.font.inter_bold,
            weight = FontWeight.Bold
        )
    )

    val base = Typography()

    return Typography(
        // The note's headings keep the line heights they always had, from the slots they used before 3A.
        displayMedium = base.headlineMedium.withInter(inter, FontWeight.SemiBold, NOTE_H1_SIZE_SP),
        displaySmall = base.titleLarge.withInter(inter, FontWeight.SemiBold, NOTE_H2_SIZE_SP),
        headlineMedium = base.headlineMedium.withInter(
            inter,
            FontWeight.SemiBold,
            UI_LARGE_TITLE_SIZE_SP
        ),
        headlineSmall = base.headlineSmall.withInter(inter, FontWeight.SemiBold, UI_TITLE_SIZE_SP),
        titleLarge = base.titleLarge.withInter(inter, FontWeight.SemiBold, UI_TITLE_SIZE_SP),
        titleMedium = base.titleMedium.withInter(inter, FontWeight.SemiBold, UI_SUBTITLE_SIZE_SP),
        titleSmall = base.titleSmall.withInter(inter, FontWeight.SemiBold, UI_BODY_SIZE_SP),
        // The notes' text, typed and drawn: lines as far apart as long reading wants (Bear, iA Writer).
        bodyLarge = base
            .bodyLarge
            .withInter(inter, FontWeight.Normal, NOTE_SIZE_SP)
            .copy(lineHeight = (NOTE_SIZE_SP * READING_LINE_HEIGHT).sp),
        bodyMedium = base.bodyMedium.withInter(inter, FontWeight.Normal, UI_BODY_SIZE_SP),
        bodySmall = base.bodySmall.withInter(inter, FontWeight.Normal, UI_SMALL_SIZE_SP),
        labelLarge = base.labelLarge.withInter(inter, FontWeight.Medium, UI_BODY_SIZE_SP),
        labelMedium = base.labelMedium.withInter(inter, FontWeight.Medium, UI_SMALL_SIZE_SP),
        labelSmall = base.labelSmall.withInter(inter, FontWeight.Medium, UI_MINI_SIZE_SP)
    )
}

private fun TextStyle.withInter(
    inter: FontFamily,
    weight: FontWeight,
    sizeSp: Int
): TextStyle = copy(
    fontFamily = inter,
    fontWeight = weight,
    fontSize = sizeSp.sp
)
