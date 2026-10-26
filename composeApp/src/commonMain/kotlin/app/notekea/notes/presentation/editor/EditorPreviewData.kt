package app.notekea.notes.presentation.editor

import androidx.compose.foundation.text.input.TextFieldState
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.notes.domain.NoteMetadata
import app.notekea.notes.domain.OutlineFinder
import app.notekea.notes.domain.RelativePath

/** Sample values shared by the editor's previews: the Arepas recipe of the smoke vault. */
internal object EditorPreviewData {
    private const val BODY = "# Arepas\n\n## Ingredients\n\n- 2 cups of **pre-cooked** corn flour\n" +
        "- 1 teaspoon of salt #shopping\n\nServe with [[Café con leche]].\n"

    /** Where "corn" is in the sample body. */
    val CORN_MATCH = BODY.indexOf(string = "corn").let { it until it + "corn".length }

    val platformConventions = PreviewPlatformConventions.linux

    val path = RelativePath(value = "Recipes/Arepas.md")

    val properties = NoteMetadata(title = null, tags = listOf("recipe", "venezuelan"), aliases = listOf("Arepa"))

    fun buffer(): TextFieldState = TextFieldState(initialText = BODY)

    /** The headings of the sample body. */
    val outline = OutlineFinder().headings(text = BODY)

    fun state(): EditorState = EditorState(notePath = path, outline = outline)
}
