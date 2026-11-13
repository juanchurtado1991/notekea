package app.notekea.core.presentation.designsystem.theme.palette

import androidx.compose.ui.graphics.Color
import app.notekea.core.appearance.domain.ColorTheme

/**
 * The dark and light colors of every theme, from its 60/30/10 palette: Polar, Desert and Storm from three palettes the
 * developer chose (2026-11-12), Forest, Ocean and Lavender made the same way. Each was moved only as far as its texts,
 * links and buttons reach WCAG AA (`ColorThemeSchemesTest`).
 */
internal object ThemeColorSets {
    val dark: Map<ColorTheme, ThemeColors> = mapOf(
        ColorTheme.POLAR to ThemeColors(
            page = Color(color = 0xFF161B1D),
            surface = Color(color = 0xFF1C2022),
            sidebar = Color(color = 0xFF1C2429),
            chrome = Color(color = 0xFF1A2024),
            raised = Color(color = 0xFF292D2F),
            ink = Color(color = 0xFFD7DDE0),
            muted = Color(color = 0xFF93999B),
            lead = Color(color = 0xFFF06E86),
            onLead = Color(color = 0xFF1F1F21),
            selection = Color(color = 0xFF202B32),
            outline = Color(color = 0xFF373B3C),
            accent = Color(color = 0xFFEC4F6D)
        ),
        ColorTheme.DESERT to ThemeColors(
            page = Color(color = 0xFF1D1A16),
            surface = Color(color = 0xFF22201C),
            sidebar = Color(color = 0xFF28281C),
            chrome = Color(color = 0xFF23221A),
            raised = Color(color = 0xFF2F2D29),
            ink = Color(color = 0xFFE0DCD7),
            muted = Color(color = 0xFF9B9893),
            lead = Color(color = 0xFFFFD21F),
            onLead = Color(color = 0xFF1F1F21),
            selection = Color(color = 0xFF303220),
            outline = Color(color = 0xFF3C3A37),
            accent = Color(color = 0xFFFFD21F)
        ),
        ColorTheme.STORM to ThemeColors(
            page = Color(color = 0xFF18191B),
            surface = Color(color = 0xFF1D1F21),
            sidebar = Color(color = 0xFF1A1B1E),
            chrome = Color(color = 0xFF191B1D),
            raised = Color(color = 0xFF2A2C2E),
            ink = Color(color = 0xFFD7D9E0),
            muted = Color(color = 0xFF96989C),
            lead = Color(color = 0xFFFF670D),
            onLead = Color(color = 0xFF1F1F21),
            selection = Color(color = 0xFF412616),
            outline = Color(color = 0xFF38393B),
            accent = Color(color = 0xFFFF5F00)
        ),
        ColorTheme.FOREST to ThemeColors(
            page = Color(color = 0xFF191D16),
            surface = Color(color = 0xFF1F221C),
            sidebar = Color(color = 0xFF1D2924),
            chrome = Color(color = 0xFF1B241E),
            raised = Color(color = 0xFF2B2F29),
            ink = Color(color = 0xFFDAE0D7),
            muted = Color(color = 0xFF979B93),
            lead = Color(color = 0xFFD1895A),
            onLead = Color(color = 0xFF1F1F21),
            selection = Color(color = 0xFF20322D),
            outline = Color(color = 0xFF393C37),
            accent = Color(color = 0xFFC8733A)
        ),
        ColorTheme.OCEAN to ThemeColors(
            page = Color(color = 0xFF1D1B16),
            surface = Color(color = 0xFF22201C),
            sidebar = Color(color = 0xFF1F2327),
            chrome = Color(color = 0xFF1E2020),
            raised = Color(color = 0xFF2F2D29),
            ink = Color(color = 0xFFE0DDD7),
            muted = Color(color = 0xFF9B9993),
            lead = Color(color = 0xFFFF6B57),
            onLead = Color(color = 0xFF1F1F21),
            selection = Color(color = 0xFF202932),
            outline = Color(color = 0xFF3C3B37),
            accent = Color(color = 0xFFFF6B57)
        ),
        ColorTheme.LAVENDER to ThemeColors(
            page = Color(color = 0xFF18161D),
            surface = Color(color = 0xFF1E1C22),
            sidebar = Color(color = 0xFF201C29),
            chrome = Color(color = 0xFF1D1A24),
            raised = Color(color = 0xFF2B292F),
            ink = Color(color = 0xFFDAD7E0),
            muted = Color(color = 0xFF97949C),
            lead = Color(color = 0xFF2BB673),
            onLead = Color(color = 0xFF1F1F21),
            selection = Color(color = 0xFF242032),
            outline = Color(color = 0xFF39373C),
            accent = Color(color = 0xFF2BB673)
        )
    )

    val light: Map<ColorTheme, ThemeColors> = mapOf(
        ColorTheme.POLAR to ThemeColors(
            page = Color(color = 0xFFF1F3F4),
            surface = Color(color = 0xFFF9FAFB),
            sidebar = Color(color = 0xFFC2DAE9),
            chrome = Color(color = 0xFFD8E5EE),
            raised = Color(color = 0xFFD3E3ED),
            ink = Color(color = 0xFF16242D),
            muted = Color(color = 0xFF505B62),
            lead = Color(color = 0xFFBB0023),
            onLead = Color(color = 0xFFFFFFFF),
            selection = Color(color = 0xFFCBE0ED),
            outline = Color(color = 0xFFA4CBE2),
            accent = Color(color = 0xFFEC4F6D)
        ),
        ColorTheme.DESERT to ThemeColors(
            page = Color(color = 0xFFF5F3EF),
            surface = Color(color = 0xFFFBFAF9),
            sidebar = Color(color = 0xFFEBEAE0),
            chrome = Color(color = 0xFFF0EEE7),
            raised = Color(color = 0xFFEFEDE5),
            ink = Color(color = 0xFF27281A),
            muted = Color(color = 0xFF65655A),
            lead = Color(color = 0xFF7A650F),
            onLead = Color(color = 0xFFFFFFFF),
            selection = Color(color = 0xFFEEEFE6),
            outline = Color(color = 0xFFE4E5D6),
            accent = Color(color = 0xFFFFDD57)
        ),
        ColorTheme.STORM to ThemeColors(
            page = Color(color = 0xFFF1F2F3),
            surface = Color(color = 0xFFFAFAFA),
            sidebar = Color(color = 0xFFE4E5E7),
            chrome = Color(color = 0xFFEAEBED),
            raised = Color(color = 0xFFE9EAEC),
            ink = Color(color = 0xFF1F2023),
            muted = Color(color = 0xFF616265),
            lead = Color(color = 0xFFB04200),
            onLead = Color(color = 0xFFFFFFFF),
            selection = Color(color = 0xFFE9EAEC),
            outline = Color(color = 0xFFDBDDE0),
            accent = Color(color = 0xFFFF8740)
        ),
        ColorTheme.FOREST to ThemeColors(
            page = Color(color = 0xFFEEF1EC),
            surface = Color(color = 0xFFF8F9F7),
            sidebar = Color(color = 0xFFDDECE6),
            chrome = Color(color = 0xFFE5EEE9),
            raised = Color(color = 0xFFE3EEE8),
            ink = Color(color = 0xFF162C26),
            muted = Color(color = 0xFF576761),
            lead = Color(color = 0xFF96562C),
            onLead = Color(color = 0xFFFFFFFF),
            selection = Color(color = 0xFFE4F0EC),
            outline = Color(color = 0xFFD3E8E2),
            accent = Color(color = 0xFFD6966B)
        ),
        ColorTheme.OCEAN to ThemeColors(
            page = Color(color = 0xFFF5F1E8),
            surface = Color(color = 0xFFFBF9F6),
            sidebar = Color(color = 0xFFDEE6EA),
            chrome = Color(color = 0xFFE8EBE9),
            raised = Color(color = 0xFFE6EAE9),
            ink = Color(color = 0xFF16222D),
            muted = Color(color = 0xFF5C6368),
            lead = Color(color = 0xFFA64639),
            onLead = Color(color = 0xFFFFFFFF),
            selection = Color(color = 0xFFE3EBF1),
            outline = Color(color = 0xFFCFDEEB),
            accent = Color(color = 0xFFFF9081)
        ),
        ColorTheme.LAVENDER to ThemeColors(
            page = Color(color = 0xFFEEEBF5),
            surface = Color(color = 0xFFF8F7FB),
            sidebar = Color(color = 0xFFE1DDED),
            chrome = Color(color = 0xFFE7E3F0),
            raised = Color(color = 0xFFE6E2F0),
            ink = Color(color = 0xFF1C172B),
            muted = Color(color = 0xFF605C6D),
            lead = Color(color = 0xFF1A6F46),
            onLead = Color(color = 0xFFFFFFFF),
            selection = Color(color = 0xFFE7E4F0),
            outline = Color(color = 0xFFD8D3E7),
            accent = Color(color = 0xFF60C896)
        )
    )
}
