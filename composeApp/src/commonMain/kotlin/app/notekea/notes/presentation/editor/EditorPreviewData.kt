package app.notekea.notes.presentation.editor

import androidx.compose.foundation.text.input.TextFieldState
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.domain.time.CalendarDate
import app.notekea.notes.domain.FrontmatterContent
import app.notekea.notes.domain.NoteProperty
import app.notekea.notes.domain.PropertyValue
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

    /** The sample note's frontmatter as the properties view shows it: one property of each type. */
    val typedProperties = FrontmatterContent.Properties(
        properties = listOf(
            NoteProperty(key = "tags", value = PropertyValue.Items(items = listOf("recipe", "venezuelan"))),
            NoteProperty(key = "status", value = PropertyValue.Text(text = "Tested")),
            NoteProperty(key = "servings", value = PropertyValue.Number(text = "4")),
            NoteProperty(key = "favorite", value = PropertyValue.Checkbox(isChecked = true)),
            NoteProperty(
                key = "cooked",
                value = PropertyValue.Date(date = CalendarDate(year = 2026, month = 10, day = 27))
            )
        )
    )

    fun buffer(): TextFieldState = TextFieldState(initialText = BODY)

    /** The headings of the sample body. */
    val outline = OutlineFinder().headings(text = BODY)

    fun state(): EditorState = EditorState(notePath = path, outline = outline)
}
