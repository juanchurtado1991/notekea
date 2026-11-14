package app.notekea.chat.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.chat.domain.plan.ClipWords
import app.notekea.chat.domain.plan.SectionAsked
import app.notekea.core.presentation.designsystem.components.NotekeaOptionRow
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.chat_section_clip
import app.notekea.resources.chat_section_clip_hint
import app.notekea.resources.chat_section_closest
import app.notekea.resources.chat_section_counted
import app.notekea.resources.chat_section_counted_full
import app.notekea.resources.chat_section_deep
import app.notekea.resources.chat_section_deep_hint
import app.notekea.resources.chat_section_found
import app.notekea.resources.chat_section_full
import app.notekea.resources.chat_section_full_all
import app.notekea.resources.chat_section_full_hint
import app.notekea.resources.chat_section_index
import app.notekea.resources.chat_section_left_out
import app.notekea.resources.chat_section_levels
import app.notekea.resources.chat_section_links
import app.notekea.resources.chat_section_links_hint
import app.notekea.resources.chat_section_no_summaries
import app.notekea.resources.chat_section_one_page
import app.notekea.resources.chat_section_only
import app.notekea.resources.chat_section_only_hint
import app.notekea.resources.chat_section_pages
import app.notekea.resources.chat_section_recommended

private const val DEEPER = 2
private const val SECONDS_A_PAGE = 3
private const val MINUTE = 60

// Past these the model's summary of each page is left out (the clips adapter's own limit).
private const val SUMMARIZED_PAGES = 40

/**
 * "I found 23 links on this page" (the developer's request of 2026-11-13): always the same ways to go on, a row
 * each with what it clips and how much: only the page, its links, a level deeper, or the whole documentation. Once
 * a deep choice was counted, the question is to confirm it. [onChoose] says which.
 */
@Composable
internal fun ChatSectionQuestion(
    section: SectionAsked,
    onChoose: (isWhole: Boolean, isEvery: Boolean, depth: Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
        if (section.isCounted) {
            Counted(
                section = section,
                onChoose = onChoose
            )
        } else {
            Found(
                section = section,
                onChoose = onChoose
            )
        }
    }
}

@Composable
private fun Found(
    section: SectionAsked,
    onChoose: (isWhole: Boolean, isEvery: Boolean, depth: Int) -> Unit
) {
    Heading(text = replyString(Res.string.chat_section_found, section.count + section.more))

    section.summary?.let { Quiet(text = it) }
    if (section.isIndex) Quiet(text = replyString(resource = Res.string.chat_section_index))

    val recommended = replyString(resource = Res.string.chat_section_recommended)
    val isDeepSuggested = section.isIndex && section.canGoDeeper

    val closest = if (section.more > 0) {
        replyString(Res.string.chat_section_closest, section.count, section.count + section.more)
    } else {
        replyString(resource = Res.string.chat_section_links_hint)
    }

    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
        NotekeaOptionRow(
            title = replyString(resource = Res.string.chat_section_only),
            onClick = { onChoose(false, false, 1) },
            supportingText = replyString(resource = Res.string.chat_section_only_hint),
            trailing = replyString(resource = Res.string.chat_section_one_page)
        )

        NotekeaOptionRow(
            title = replyString(resource = Res.string.chat_section_links),
            onClick = { onChoose(true, false, 1) },
            supportingText = closest,
            trailing = replyString(Res.string.chat_section_pages, section.count + 1),
            badge = recommended.takeIf {
                !isDeepSuggested
            },
            isSuggested = !isDeepSuggested
        )

        // An index of the site already lists every page: there is no deeper level to follow.
        if (section.canGoDeeper) {
            NotekeaOptionRow(
                title = replyString(resource = Res.string.chat_section_deep),
                onClick = { onChoose(true, false, DEEPER) },
                supportingText = replyString(resource = Res.string.chat_section_deep_hint),
                trailing = replyString(Res.string.chat_section_levels, DEEPER),
                badge = recommended.takeIf { isDeepSuggested },
                isSuggested = isDeepSuggested
            )
        }

        val all = if (section.canGoDeeper) {
            replyString(resource = Res.string.chat_section_full_all)
        } else {
            replyString(Res.string.chat_section_pages, section.count + section.more + 1)
        }

        NotekeaOptionRow(
            title = replyString(resource = Res.string.chat_section_full),
            onClick = { onChoose(true, true, ClipWords.FULL_DEPTH) },
            supportingText = replyString(resource = Res.string.chat_section_full_hint),
            trailing = all
        )
    }
}

@Composable
private fun Counted(
    section: SectionAsked,
    onChoose: (isWhole: Boolean, isEvery: Boolean, depth: Int) -> Unit
) {
    val pages = section.count + 1
    val isFull = section.depth >= ClipWords.FULL_DEPTH

    val found = if (isFull) {
        replyString(Res.string.chat_section_counted_full, pages)
    } else {
        replyString(Res.string.chat_section_counted, pages, section.depth)
    }

    Heading(text = found)
    if (section.more > 0) Quiet(text = replyString(Res.string.chat_section_left_out, section.more))

    if (section.count > SUMMARIZED_PAGES) {
        Quiet(text = replyString(resource = Res.string.chat_section_no_summaries))
    }

    val minutes = (pages * SECONDS_A_PAGE / MINUTE).coerceAtLeast(minimumValue = 1)

    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
        NotekeaOptionRow(
            title = replyString(Res.string.chat_section_clip, pages),
            onClick = { onChoose(true, isFull, section.depth) },
            supportingText = replyString(Res.string.chat_section_clip_hint, minutes),
            isSuggested = true
        )

        NotekeaOptionRow(
            title = replyString(resource = Res.string.chat_section_only),
            onClick = { onChoose(false, false, 1) },
            supportingText = replyString(resource = Res.string.chat_section_only_hint),
            trailing = replyString(resource = Res.string.chat_section_one_page)
        )
    }
}

@Composable
private fun Heading(text: String) {
    Text(
        text = text,
        style = MaterialTheme
            .typography
            .bodyMedium
            .copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun Quiet(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview
@Composable
private fun ChatSectionQuestionPreview() {
    NotekeaPreviewThemes {
        Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingL)) {
            ChatSectionQuestion(
                section = SectionAsked(
                    url = "https://example.com/docs/",
                    prefix = "https://example.com/docs/",
                    count = 23,
                    more = 0,
                    canGoDeeper = true,
                    isIndex = true
                ),
                onChoose = { _, _, _ -> }
            )

            ChatSectionQuestion(
                section = SectionAsked(
                    url = "https://example.com/docs/",
                    prefix = "https://example.com/docs/",
                    count = 176,
                    more = 0,
                    depth = 2,
                    isCounted = true
                ),
                onChoose = { _, _, _ -> }
            )
        }
    }
}
