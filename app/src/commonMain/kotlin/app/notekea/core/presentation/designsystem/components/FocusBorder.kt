package app.notekea.core.presentation.designsystem.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

/**
 * Keyboard focus as the component's own border, the one focus style of the design system: while the focus target that
 * follows this modifier has focus, a solid [width] stroke is drawn just inside [shape], over the content, without
 * changing the layout. Place it after size and before clip and click. [color] is usually primary.
 */
fun Modifier.focusBorder(
    color: Color,
    shape: Shape,
    width: Dp = NotekeaDimensions.FocusRingWidth
): Modifier = this then FocusBorderElement(
    color = color,
    shape = shape,
    width = width
)
