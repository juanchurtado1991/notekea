package app.notekea.clips.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.clips.domain.ClipError
import app.notekea.clips.domain.ClipStep
import app.notekea.clips.domain.SectionLinks
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.NotekeaDialog
import app.notekea.core.presentation.designsystem.components.feedback.ErrorBanner
import app.notekea.core.presentation.designsystem.components.fields.NotekeaTextField
import app.notekea.core.presentation.designsystem.components.layout.NotekeaStatusProgress
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.clip_cancel
import app.notekea.resources.clip_checking
import app.notekea.resources.clip_clip
import app.notekea.resources.clip_error_address
import app.notekea.resources.clip_error_large
import app.notekea.resources.clip_error_local
import app.notekea.resources.clip_error_not_article
import app.notekea.resources.clip_error_offline
import app.notekea.resources.clip_error_refused
import app.notekea.resources.clip_error_slow
import app.notekea.resources.clip_explain
import app.notekea.resources.clip_fetching
import app.notekea.resources.clip_levels
import app.notekea.resources.clip_only_page
import app.notekea.resources.clip_section_more
import app.notekea.resources.clip_section_progress
import app.notekea.resources.clip_section_question
import app.notekea.resources.clip_title
import app.notekea.resources.clip_url_hint
import app.notekea.resources.clip_with_all
import app.notekea.resources.clip_with_section
import app.notekea.resources.clip_writing
import org.jetbrains.compose.resources.stringResource

private const val DIALOG_WIDTH_DP = 520
private val DEEPER = listOf(2, 3)

/**
 * "Clip web page…": the address to clip, what clipping does, how far it is (downloading, writing notes, checking them
 * against the article), and why it failed in plain words; Cancel stops it.
 */
@Composable
fun ClipDialog(
    state: ClipState,
    onIntent: (ClipIntent) -> Unit
) {
    if (!state.isOpen) return

    NotekeaDialog(
        onDismiss = { onIntent(ClipIntent.OnDismiss) },
        modifier = Modifier.width(width = DIALOG_WIDTH_DP.dp)
    ) {
        Column(
            modifier = Modifier.padding(all = NotekeaDimensions.SpacingL),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)
        ) {
            Text(
                text = stringResource(resource = Res.string.clip_title),
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = stringResource(resource = Res.string.clip_explain),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            NotekeaTextField(
                value = state.url,
                onValueChange = { onIntent(ClipIntent.OnUrlChange(url = it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = stringResource(resource = Res.string.clip_url_hint),
                onSubmit = { onIntent(ClipIntent.OnClip) }
            )

            state.error?.let { ErrorBanner(message = messageOf(error = it)) }
            progressOf(step = state.step)?.let { NotekeaStatusProgress(label = it) }

            state.progress?.let {
                NotekeaStatusProgress(
                    label = stringResource(Res.string.clip_section_progress, it.done + 1, it.total)
                )
            }

            state
                .section
                ?.takeIf { state.isAsking }
                ?.let {
                    SectionQuestion(
                        links = it,
                        summary = state.sectionSummary,
                        onIntent = onIntent
                    )
                }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    space = NotekeaDimensions.SpacingS,
                    alignment = Alignment.End
                )
            ) {
                NotekeaButton(
                    text = stringResource(resource = Res.string.clip_cancel),
                    onClick = { onIntent(ClipIntent.OnDismiss) },
                    style = NotekeaButtonStyle.TEXT
                )

                if (!state.isAsking) {
                    NotekeaButton(
                        text = stringResource(resource = Res.string.clip_clip),
                        onClick = { onIntent(ClipIntent.OnClip) },
                        enabled = state.canClip
                    )
                }
            }
        }
    }
}

/** "This page links 30 more pages under …/integrations/": only it, or them too. */
@Composable
private fun SectionQuestion(
    links: SectionLinks,
    summary: String?,
    onIntent: (ClipIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
        summary?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = stringResource(Res.string.clip_section_question, links.urls.size, links.prefix),
            style = MaterialTheme.typography.bodyMedium
        )

        if (links.more > 0) {
            Text(
                text = stringResource(resource = Res.string.clip_section_more, links.more),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
            NotekeaButton(
                text = stringResource(resource = Res.string.clip_only_page),
                onClick = { onIntent(ClipIntent.OnClipPage) },
                style = NotekeaButtonStyle.TEXT
            )

            NotekeaButton(
                text = stringResource(resource = Res.string.clip_with_section, links.urls.size),
                onClick = { onIntent(ClipIntent.OnClipSection) }
            )

            // The pages those pages link, and the ones they link: how deep is the person's to say.
            if (links.canGoDeeper) {
                DEEPER.forEach { levels ->
                    NotekeaButton(
                        text = stringResource(Res.string.clip_levels, levels),
                        onClick = { onIntent(ClipIntent.OnClipDeep(levels = levels)) },
                        style = NotekeaButtonStyle.SECONDARY
                    )
                }
            }

            if (links.more > 0) {
                NotekeaButton(
                    text = stringResource(Res.string.clip_with_all, links.urls.size + links.more),
                    onClick = { onIntent(ClipIntent.OnClipAll) },
                    style = NotekeaButtonStyle.TEXT
                )
            }
        }
    }
}

@Composable
private fun progressOf(step: ClipStep?): String? = when (step) {
    ClipStep.Fetching -> stringResource(resource = Res.string.clip_fetching)
    is ClipStep.Writing -> stringResource(resource = Res.string.clip_writing)
    ClipStep.Checking -> stringResource(resource = Res.string.clip_checking)
    else -> null
}

@Composable
private fun messageOf(error: ClipError): String {
    val resource = when (error) {
        ClipError.BadAddress -> Res.string.clip_error_address
        ClipError.LocalAddress -> Res.string.clip_error_local
        ClipError.Offline -> Res.string.clip_error_offline
        ClipError.TooSlow -> Res.string.clip_error_slow
        ClipError.TooLarge -> Res.string.clip_error_large
        is ClipError.Refused -> Res.string.clip_error_refused
        ClipError.NotArticle -> Res.string.clip_error_not_article
    }

    return stringResource(resource = resource)
}

@Preview
@Composable
private fun ClipDialogPreview() {
    NotekeaPreviewThemes {
        ClipDialog(
            state = ClipState(
                isOpen = true,
                url = "https://example.com/sleep",
                step = ClipStep.Checking
            ),
            onIntent = {}
        )
    }
}
