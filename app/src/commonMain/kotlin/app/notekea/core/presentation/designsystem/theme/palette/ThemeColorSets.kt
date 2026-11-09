package app.notekea.core.presentation.designsystem.theme.palette

import androidx.compose.ui.graphics.Color
import app.notekea.core.appearance.domain.ColorTheme

/** The dark and light colors of every theme but [ColorTheme.WORKSPACE_BLUE], which has [WorkspaceBluePalette]. */
internal object ThemeColorSets {
    val dark: Map<ColorTheme, ThemeColors> = mapOf(
        ColorTheme.GRAPHITE to ThemeColors(
            page = Color(color = 0xFF18191A),
            surface = Color(color = 0xFF1F2021),
            raised = Color(color = 0xFF2A2B2D),
            ink = Color(color = 0xFFD6D7D9),
            muted = Color(color = 0xFFA6A8AC),
            lead = Color(color = 0xFFB9C0CC),
            onLead = Color(color = 0xFF18191A),
            selection = Color(color = 0xFF2F3236),
            outline = Color(color = 0xFF3A3C3F),
            accent = Color(color = 0xFFD9A25F)
        ),
        ColorTheme.SAGE to ThemeColors(
            page = Color(color = 0xFF161A18),
            surface = Color(color = 0xFF1C211E),
            raised = Color(color = 0xFF272D29),
            ink = Color(color = 0xFFD4DBD6),
            muted = Color(color = 0xFFA2ACA5),
            lead = Color(color = 0xFF7FC29C),
            onLead = Color(color = 0xFF12201A),
            selection = Color(color = 0xFF233229),
            outline = Color(color = 0xFF38403B),
            accent = Color(color = 0xFFD9A25F)
        ),
        ColorTheme.LAGOON to ThemeColors(
            page = Color(color = 0xFF141A1C),
            surface = Color(color = 0xFF1A2224),
            raised = Color(color = 0xFF242E31),
            ink = Color(color = 0xFFD0DBDD),
            muted = Color(color = 0xFF9FADB0),
            lead = Color(color = 0xFF5CC2C7),
            onLead = Color(color = 0xFF0F2224),
            selection = Color(color = 0xFF1B3336),
            outline = Color(color = 0xFF344144),
            accent = Color(color = 0xFFE3A76A)
        ),
        ColorTheme.IRIS to ThemeColors(
            page = Color(color = 0xFF18171C),
            surface = Color(color = 0xFF1F1D24),
            raised = Color(color = 0xFF2B2932),
            ink = Color(color = 0xFFD9D6E0),
            muted = Color(color = 0xFFABA7B5),
            lead = Color(color = 0xFFA998F4),
            onLead = Color(color = 0xFF1A1630),
            selection = Color(color = 0xFF2C2843),
            outline = Color(color = 0xFF3B3845),
            accent = Color(color = 0xFFE8A06E)
        ),
        ColorTheme.ROSE to ThemeColors(
            page = Color(color = 0xFF1B1719),
            surface = Color(color = 0xFF221D20),
            raised = Color(color = 0xFF2F282C),
            ink = Color(color = 0xFFE0D6DA),
            muted = Color(color = 0xFFB4A7AD),
            lead = Color(color = 0xFFF08CA8),
            onLead = Color(color = 0xFF2A1620),
            selection = Color(color = 0xFF3A2630),
            outline = Color(color = 0xFF41373C),
            accent = Color(color = 0xFFE6A860)
        ),
        ColorTheme.CLAY to ThemeColors(
            page = Color(color = 0xFF1C1917),
            surface = Color(color = 0xFF231F1C),
            raised = Color(color = 0xFF2F2A26),
            ink = Color(color = 0xFFDED7CF),
            muted = Color(color = 0xFFB1A89E),
            lead = Color(color = 0xFFE3966C),
            onLead = Color(color = 0xFF2A1810),
            selection = Color(color = 0xFF392A21),
            outline = Color(color = 0xFF413A34),
            accent = Color(color = 0xFFD9B25F)
        ),
        ColorTheme.HONEY to ThemeColors(
            page = Color(color = 0xFF1B1916),
            surface = Color(color = 0xFF22201B),
            raised = Color(color = 0xFF2E2B25),
            ink = Color(color = 0xFFDDD8CC),
            muted = Color(color = 0xFFAFA99B),
            lead = Color(color = 0xFFE5B567),
            onLead = Color(color = 0xFF261D0B),
            selection = Color(color = 0xFF342D1E),
            outline = Color(color = 0xFF403C33),
            accent = Color(color = 0xFFE08A5B)
        )
    )

    val light: Map<ColorTheme, ThemeColors> = mapOf(
        ColorTheme.GRAPHITE to ThemeColors(
            page = Color(color = 0xFFF7F7F7),
            surface = Color(color = 0xFFFFFFFF),
            raised = Color(color = 0xFFEFEFF0),
            ink = Color(color = 0xFF28292C),
            muted = Color(color = 0xFF54565C),
            lead = Color(color = 0xFF4A5160),
            onLead = Color(color = 0xFFFFFFFF),
            selection = Color(color = 0xFFE8E9EC),
            outline = Color(color = 0xFFDCDDE0),
            accent = Color(color = 0xFFC47A14)
        ),
        ColorTheme.SAGE to ThemeColors(
            page = Color(color = 0xFFF5F7F4),
            surface = Color(color = 0xFFFFFFFF),
            raised = Color(color = 0xFFECEFEA),
            ink = Color(color = 0xFF26302A),
            muted = Color(color = 0xFF525E57),
            lead = Color(color = 0xFF2C7A55),
            onLead = Color(color = 0xFFFFFFFF),
            selection = Color(color = 0xFFE3F0E8),
            outline = Color(color = 0xFFD9DED8),
            accent = Color(color = 0xFFC47A14)
        ),
        ColorTheme.LAGOON to ThemeColors(
            page = Color(color = 0xFFF4F8F8),
            surface = Color(color = 0xFFFFFFFF),
            raised = Color(color = 0xFFE9F0F0),
            ink = Color(color = 0xFF213033),
            muted = Color(color = 0xFF4C5F63),
            lead = Color(color = 0xFF0B7682),
            onLead = Color(color = 0xFFFFFFFF),
            selection = Color(color = 0xFFDFF1F2),
            outline = Color(color = 0xFFD6E0E1),
            accent = Color(color = 0xFFC9761F)
        ),
        ColorTheme.IRIS to ThemeColors(
            page = Color(color = 0xFFF8F7FA),
            surface = Color(color = 0xFFFFFFFF),
            raised = Color(color = 0xFFF0EEF4),
            ink = Color(color = 0xFF2C2935),
            muted = Color(color = 0xFF595565),
            lead = Color(color = 0xFF6A4ED6),
            onLead = Color(color = 0xFFFFFFFF),
            selection = Color(color = 0xFFECE8FB),
            outline = Color(color = 0xFFDFDCE6),
            accent = Color(color = 0xFFC9762E)
        ),
        ColorTheme.ROSE to ThemeColors(
            page = Color(color = 0xFFFAF6F7),
            surface = Color(color = 0xFFFFFFFF),
            raised = Color(color = 0xFFF3ECEF),
            ink = Color(color = 0xFF35272D),
            muted = Color(color = 0xFF63515A),
            lead = Color(color = 0xFFBE3468),
            onLead = Color(color = 0xFFFFFFFF),
            selection = Color(color = 0xFFFBE7EE),
            outline = Color(color = 0xFFE6DCE0),
            accent = Color(color = 0xFFC97A12)
        ),
        ColorTheme.CLAY to ThemeColors(
            page = Color(color = 0xFFF8F5F0),
            surface = Color(color = 0xFFFFFDFA),
            raised = Color(color = 0xFFF0EBE3),
            ink = Color(color = 0xFF33291F),
            muted = Color(color = 0xFF62564A),
            lead = Color(color = 0xFFAD4F28),
            onLead = Color(color = 0xFFFFFFFF),
            selection = Color(color = 0xFFF8E8DE),
            outline = Color(color = 0xFFE3DBCF),
            accent = Color(color = 0xFFB58A14)
        ),
        ColorTheme.HONEY to ThemeColors(
            page = Color(color = 0xFFF9F7F1),
            surface = Color(color = 0xFFFFFEFA),
            raised = Color(color = 0xFFF1EDE2),
            ink = Color(color = 0xFF322C1E),
            muted = Color(color = 0xFF5F5847),
            lead = Color(color = 0xFF8F6108),
            onLead = Color(color = 0xFFFFFFFF),
            selection = Color(color = 0xFFF8EED6),
            outline = Color(color = 0xFFE3DDCE),
            accent = Color(color = 0xFFD06D36)
        )
    )
}
