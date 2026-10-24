package app.notekea.core.presentation.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import app.notekea.core.presentation.theme.NotekeaDimensions

/**
 * Keyboard focus as the component's own border, the one focus style of the design system: while the focus target that
 * follows this modifier (a `clickable`, a `focusable`) has focus, a solid [width] stroke is drawn just inside [shape],
 * on top of the content, with no gap. Place it after size and before clip and click, so it sees the focus and
 * outlines the visible shape. It does not change the layout. [color] is usually primary; filled controls use a color
 * that shows on their fill.
 */
fun Modifier.focusBorder(
    color: Color,
    shape: Shape,
    width: Dp = NotekeaDimensions.FocusRingWidth
): Modifier = this then FocusBorderElement(color = color, shape = shape, width = width)
