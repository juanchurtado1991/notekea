package app.notekea.core.presentation.designsystem.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable

/** A tween of [durationMillis], or no animation at all where motion is reduced ([LocalReduceMotion]). */
@Composable
fun <T> motionTween(durationMillis: Int): FiniteAnimationSpec<T> = if (LocalReduceMotion.current) {
    snap()
} else {
    tween(durationMillis = durationMillis)
}

/**
 * A spring without bounce for what moves into place (macOS redesign 9A): it starts fast and settles softly, as AppKit's
 * panels do; no animation at all where motion is reduced ([LocalReduceMotion]).
 */
@Composable
fun <T> motionSpring(): FiniteAnimationSpec<T> = if (LocalReduceMotion.current) {
    snap()
} else {
    spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = SPRING_STIFFNESS
    )
}

private const val SPRING_STIFFNESS = 500f

/** Brings the row at [index] into view, gliding unless [reduceMotion]. */
suspend fun LazyListState.followItem(
    index: Int,
    reduceMotion: Boolean
) {
    if (reduceMotion) scrollToItem(index = index) else animateScrollToItem(index = index)
}
