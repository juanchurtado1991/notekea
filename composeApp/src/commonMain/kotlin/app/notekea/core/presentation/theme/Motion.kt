package app.notekea.core.presentation.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable

/** A tween of [durationMillis], or no animation at all where motion is reduced ([LocalReduceMotion]). */
@Composable
fun <T> motionTween(
    durationMillis: Int
): FiniteAnimationSpec<T> = if (LocalReduceMotion.current) snap() else tween(durationMillis = durationMillis)

/** Brings the row at [index] into view, gliding unless [reduceMotion]. */
suspend fun LazyListState.followItem(
    index: Int,
    reduceMotion: Boolean
) {
    if (reduceMotion) scrollToItem(index = index) else animateScrollToItem(index = index)
}
