package app.notekea.core.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.mikepenz.markdown.model.ImageData
import com.mikepenz.markdown.model.ImageTransformer

/**
 * Gives the Markdown renderer the images of a note, drawn by [painterOf] from the image's address as the Markdown
 * writes it; an address it does not know shows nothing, as the renderer does for an image it cannot load.
 */
class NotekeaImageTransformer(private val painterOf: (String) -> Painter?) : ImageTransformer {
    @Composable
    override fun transform(
        link: String
    ): ImageData? = painterOf(link)?.let { ImageData(painter = it) }
}
