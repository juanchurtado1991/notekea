package app.notekea.chat.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.sources.SourcePage
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.chat.presentation.model.ChatPreviewData
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaOptionRow
import app.notekea.core.presentation.designsystem.components.feedback.NotekeaStatusRing
import app.notekea.core.presentation.designsystem.components.fields.NotekeaCheckbox
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.chat_not_found
import app.notekea.resources.chat_site_clip
import app.notekea.resources.chat_site_found
import app.notekea.resources.chat_site_looking
import app.notekea.resources.chat_site_none
import app.notekea.resources.chat_site_search
import app.notekea.resources.chat_site_search_hint
import app.notekea.resources.chat_site_sources

/**
 * "I couldn't find this in your notes", and what to do about it (the developer's request of 2026-11-13): look for it
 * in the sites the vault has web sources of, then the pages found there with a box each, clipped and answered from
 * with one button. Nothing is clipped before the person says so.
 */
@Composable
internal fun ChatNotFound(
    entry: ChatEntry,
    onIntent: (ChatIntent) -> Unit
) {
    val pages = entry.sitePages

    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
        Quiet(text = replyString(resource = Res.string.chat_not_found))

        when {
            entry.isLookingInSites -> Looking(found = entry.progress?.found ?: 0)

            pages == null && entry.sites.isNotEmpty() -> NotekeaOptionRow(
                title = replyString(Res.string.chat_site_search, siteOf(sites = entry.sites)),
                onClick = { onIntent(ChatIntent.OnSiteSearch(id = entry.id)) },
                supportingText = replyString(resource = Res.string.chat_site_search_hint)
            )

            pages != null && pages.isEmpty() -> Quiet(
                text = replyString(Res.string.chat_site_none, siteOf(sites = entry.sites))
            )

            pages != null -> Pages(
                entry = entry,
                pages = pages,
                onIntent = onIntent
            )
        }
    }
}

@Composable
private fun Pages(
    entry: ChatEntry,
    pages: List<SourcePage>,
    onIntent: (ChatIntent) -> Unit
) {
    Text(
        text = replyString(resource = Res.string.chat_site_found),
        style = MaterialTheme
            .typography
            .bodyMedium
            .copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface
    )

    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
        pages.forEach { page ->
            NotekeaCheckbox(
                checked = page.url in entry.pickedPages,
                onClick = {
                    onIntent(
                        ChatIntent.OnSitePageToggle(
                            id = entry.id,
                            url = page.url
                        )
                    )
                },
                label = page.about
            )
        }
    }

    NotekeaButton(
        text = replyString(Res.string.chat_site_clip, entry.pickedPages.size),
        onClick = { onIntent(ChatIntent.OnSiteClip(id = entry.id)) },
        enabled = entry.pickedPages.isNotEmpty()
    )
}

@Composable
private fun Looking(found: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NotekeaStatusRing(fraction = null)
        Quiet(text = replyString(Res.string.chat_site_looking, found))
    }
}

@Composable
private fun Quiet(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// "wordpress.org", or "your web sources" when they come from several sites.
@Composable
private fun siteOf(sites: List<String>): String =
    sites.singleOrNull() ?: replyString(resource = Res.string.chat_site_sources)

@Preview
@Composable
private fun ChatNotFoundPreview() {
    val pages = listOf(
        SourcePage(
            url = "https://example.com/docs/store",
            about = "Set up an online store",
            section = "Web sources/Docs.md"
        ),
        SourcePage(
            url = "https://example.com/docs/payments",
            about = "Take payments",
            section = "Web sources/Docs.md"
        )
    )

    NotekeaPreviewThemes {
        Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingL)) {
            ChatNotFound(
                entry = ChatPreviewData.answered.copy(
                    outcome = TurnOutcome.NOT_FOUND,
                    sites = listOf("example.com")
                ),
                onIntent = {}
            )

            ChatNotFound(
                entry = ChatPreviewData.answered.copy(
                    outcome = TurnOutcome.NOT_FOUND,
                    sites = listOf("example.com"),
                    sitePages = pages,
                    pickedPages = setOf(pages.first().url)
                ),
                onIntent = {}
            )
        }
    }
}
