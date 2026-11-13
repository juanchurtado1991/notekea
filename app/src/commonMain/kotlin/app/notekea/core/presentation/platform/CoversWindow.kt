package app.notekea.core.presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.tooling.preview.Preview

/** Tells the window that what calls this is drawn over it for as long as it is composed ([WindowCovers]). */
@Composable
fun CoversWindow() {
    val covers = LocalWindowCovers.current

    DisposableEffect(covers) {
        covers.count++

        onDispose {
            covers.count--
        }
    }
}

// It draws nothing.
@Preview
@Composable
private fun CoversWindowPreview() {
    CoversWindow()
}
