package app.notekea.chat.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.chat.domain.SectionAsked
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.chat_section_all
import app.notekea.resources.chat_section_more
import app.notekea.resources.chat_section_only
import app.notekea.resources.chat_section_question

/** "This page links 30 more pages under …": clip only it, or them too; [onChoose] says whether the whole section. */
@Composable
internal fun ChatSectionQuestion(
    section: SectionAsked,
    onChoose: (isWhole: Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
        section.summary?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = replyString(Res.string.chat_section_question, section.count, section.prefix),
            style = MaterialTheme.typography.bodyMedium
        )

        if (section.more > 0) {
            Text(
                text = replyString(Res.string.chat_section_more, section.more),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
            NotekeaButton(
                text = replyString(resource = Res.string.chat_section_only),
                onClick = { onChoose(false) },
                style = NotekeaButtonStyle.TEXT
            )

            NotekeaButton(
                text = replyString(Res.string.chat_section_all, section.count),
                onClick = { onChoose(true) }
            )
        }
    }
}

@Preview
@Composable
private fun ChatSectionQuestionPreview() {
    NotekeaPreviewThemes {
        ChatSectionQuestion(
            section = SectionAsked(
                url = "https://example.com/docs/integrations/introduction",
                prefix = "https://example.com/docs/integrations/",
                count = 30,
                more = 4
            ),
            onChoose = {}
        )
    }
}
