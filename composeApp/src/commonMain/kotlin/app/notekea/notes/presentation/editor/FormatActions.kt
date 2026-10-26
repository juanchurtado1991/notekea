package app.notekea.notes.presentation.editor

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.domain.markdown.CalloutKind
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.notes.domain.FormatAction
import app.notekea.resources.Res
import app.notekea.resources.format_bold_placeholder
import app.notekea.resources.format_callout_abstract
import app.notekea.resources.format_callout_bug
import app.notekea.resources.format_callout_danger
import app.notekea.resources.format_callout_example
import app.notekea.resources.format_callout_failure
import app.notekea.resources.format_callout_info
import app.notekea.resources.format_callout_note
import app.notekea.resources.format_callout_placeholder
import app.notekea.resources.format_callout_question
import app.notekea.resources.format_callout_quote
import app.notekea.resources.format_callout_success
import app.notekea.resources.format_callout_tip
import app.notekea.resources.format_callout_todo
import app.notekea.resources.format_callout_warning
import app.notekea.resources.format_checklist_placeholder
import app.notekea.resources.format_code_placeholder
import app.notekea.resources.format_heading_placeholder
import app.notekea.resources.format_italic_placeholder
import app.notekea.resources.format_list_placeholder
import app.notekea.resources.format_quote_placeholder
import app.notekea.resources.format_table_column
import app.notekea.resources.format_web_link_placeholder
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The toolbar's [FormatAction]s with their placeholders in the app's language, so the toolbar and the shortcuts insert
 * the same text. A callout is titled with its type in plain words ([calloutTitles]), as Read mode shows it.
 */
data class FormatActions(
    val bold: FormatAction.Bold,
    val italic: FormatAction.Italic,
    val headings: List<FormatAction.Heading>,
    val webLink: FormatAction.WebLink,
    val bulletList: FormatAction.BulletList,
    val numberedList: FormatAction.NumberedList,
    val checklist: FormatAction.Checklist,
    val quote: FormatAction.Quote,
    val codeBlock: FormatAction.CodeBlock,
    val table: FormatAction.Table,
    val calloutPlaceholder: String,
    val calloutTitles: Map<CalloutKind, String>
) {
    fun callout(
        kind: CalloutKind,
        isFolded: Boolean
    ): FormatAction.Callout = FormatAction.Callout(
        type = kind.name.lowercase(),
        title = calloutTitles.getValue(kind),
        placeholder = calloutPlaceholder,
        isFolded = isFolded
    )
}
