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

private const val TABLE_COLUMNS = 3

/** The [FormatActions] in the app's language. */
@Composable
fun formatActions(): FormatActions {
    val headingPlaceholder = stringResource(resource = Res.string.format_heading_placeholder)
    val listPlaceholder = stringResource(resource = Res.string.format_list_placeholder)
    return FormatActions(
        bold = FormatAction.Bold(placeholder = stringResource(resource = Res.string.format_bold_placeholder)),
        italic = FormatAction.Italic(placeholder = stringResource(resource = Res.string.format_italic_placeholder)),
        headings = (1..3).map { FormatAction.Heading(level = it, placeholder = headingPlaceholder) },
        webLink = FormatAction.WebLink(placeholder = stringResource(resource = Res.string.format_web_link_placeholder)),
        bulletList = FormatAction.BulletList(placeholder = listPlaceholder),
        numberedList = FormatAction.NumberedList(placeholder = listPlaceholder),
        checklist = FormatAction.Checklist(
            placeholder = stringResource(resource = Res.string.format_checklist_placeholder)
        ),
        quote = FormatAction.Quote(placeholder = stringResource(resource = Res.string.format_quote_placeholder)),
        codeBlock = FormatAction.CodeBlock(placeholder = stringResource(resource = Res.string.format_code_placeholder)),
        table = FormatAction.Table(
            columnNames = (1..TABLE_COLUMNS).map { stringResource(Res.string.format_table_column, it) }
        ),
        calloutPlaceholder = stringResource(resource = Res.string.format_callout_placeholder),
        calloutTitles = CalloutKind.entries.associateWith { stringResource(resource = calloutTitle(kind = it)) }
    )
}

private fun calloutTitle(
    kind: CalloutKind
): StringResource = when (kind) {
    CalloutKind.NOTE -> Res.string.format_callout_note
    CalloutKind.ABSTRACT -> Res.string.format_callout_abstract
    CalloutKind.INFO -> Res.string.format_callout_info
    CalloutKind.TODO -> Res.string.format_callout_todo
    CalloutKind.TIP -> Res.string.format_callout_tip
    CalloutKind.SUCCESS -> Res.string.format_callout_success
    CalloutKind.QUESTION -> Res.string.format_callout_question
    CalloutKind.WARNING -> Res.string.format_callout_warning
    CalloutKind.FAILURE -> Res.string.format_callout_failure
    CalloutKind.DANGER -> Res.string.format_callout_danger
    CalloutKind.BUG -> Res.string.format_callout_bug
    CalloutKind.EXAMPLE -> Res.string.format_callout_example
    CalloutKind.QUOTE -> Res.string.format_callout_quote
}

@Preview
@Composable
private fun formatActionsPreview() {
    NotekeaPreviewThemes {
        Text(text = formatActions().calloutTitles.values.joinToString())
    }
}
