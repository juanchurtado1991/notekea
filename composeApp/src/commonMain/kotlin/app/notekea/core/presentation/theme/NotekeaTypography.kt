package app.notekea.core.presentation.theme

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
 * Bundled typefaces (Inter for UI and text, JetBrains Mono for code) and the type scale 12 / 14 / 16 / 20 / 28 sp.
 * Built inside composition because `Font()` reads Compose resources there.
 */

private const val BODY_SIZE_SP = 14
private const val HEADLINE_SIZE_SP = 28
private const val LARGE_SIZE_SP = 16
private const val READING_LINE_HEIGHT = 1.6f
private const val SMALL_SIZE_SP = 12
private const val TITLE_SIZE_SP = 20

@Composable
fun notekeaCodeTextStyle(): TextStyle = TextStyle(
    fontFamily = FontFamily(
        Font(resource = Res.font.jetbrains_mono_regular, weight = FontWeight.Normal),
        Font(resource = Res.font.jetbrains_mono_bold, weight = FontWeight.Bold)
    ),
    fontSize = BODY_SIZE_SP.sp
)

@Composable
fun notekeaTypography(): Typography {
    val inter = FontFamily(
        Font(resource = Res.font.inter_regular, weight = FontWeight.Normal),
        Font(resource = Res.font.inter_medium, weight = FontWeight.Medium),
        Font(resource = Res.font.inter_semibold, weight = FontWeight.SemiBold),
        Font(resource = Res.font.inter_bold, weight = FontWeight.Bold)
    )
    val base = Typography()
    return Typography(
        headlineMedium = base.headlineMedium.withInter(inter, FontWeight.SemiBold, HEADLINE_SIZE_SP),
        titleLarge = base.titleLarge.withInter(inter, FontWeight.SemiBold, TITLE_SIZE_SP),
        titleMedium = base.titleMedium.withInter(inter, FontWeight.Medium, LARGE_SIZE_SP),
        // The notes' text, in Edit and Preview: lines as far apart as long reading wants (Bear, iA Writer).
        bodyLarge = base.bodyLarge.withInter(inter, FontWeight.Normal, LARGE_SIZE_SP)
            .copy(lineHeight = (LARGE_SIZE_SP * READING_LINE_HEIGHT).sp),
        bodyMedium = base.bodyMedium.withInter(inter, FontWeight.Normal, BODY_SIZE_SP),
        bodySmall = base.bodySmall.withInter(inter, FontWeight.Normal, SMALL_SIZE_SP),
        labelLarge = base.labelLarge.withInter(inter, FontWeight.Medium, BODY_SIZE_SP),
        labelMedium = base.labelMedium.withInter(inter, FontWeight.Medium, SMALL_SIZE_SP)
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
