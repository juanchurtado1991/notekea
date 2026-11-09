package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.nodes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/** Buttons and text fields are as tall as the rest of a dense desktop interface, not Material's 40 and 56 dp. */
class CompactControlsScreenTest {
    @Test
    fun `every button style and a one line text field are one control tall`() {
        var heights = emptyMap<String, Float>()
        UiThread.run {
            ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) {
                    Column {
                        NotekeaButtonStyle.entries.forEach {
                            NotekeaButton(text = it.name, onClick = {
                            }, modifier = Modifier.testTag(tag = it.name), style = it)
                        }
                        NotekeaTextField(
                            value = "",
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth().testTag(tag = FIELD),
                            placeholder = FIELD
                        )
                    }
                }
            }.use { scene ->
                repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = it * FRAME_NANOS) }
                heights = scene.nodes().mapNotNull { node ->
                    node.config.getOrNull(SemanticsProperties.TestTag)?.let { it to node.boundsInRoot.height }
                }.toMap()
            }
        }

        val control = NotekeaDimensions.ControlHeight.value
        assertEquals(
            expected = (NotekeaButtonStyle.entries.map { it.name } + FIELD).associateWith { control },
            actual = heights
        )
    }

    companion object {
        private const val FIELD = "field"
        private const val FRAME_NANOS = 16_000_000L
        private const val HEIGHT = 400
        private const val SETTLE_FRAMES = 3
        private const val WIDTH = 400
    }
}
