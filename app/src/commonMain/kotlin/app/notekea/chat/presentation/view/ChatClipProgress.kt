package app.notekea.chat.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.chat.domain.plan.PageProgress
import app.notekea.core.presentation.designsystem.components.feedback.NotekeaProgressBar
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.chat_step_clipping
import app.notekea.resources.chat_step_clipping_label
import app.notekea.resources.chat_step_finding
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_WIDTH_DP = 320

/**
 * How far a clip of many pages is, in the chat (the developer's request of 2026-11-13): how many pages the links have
 * given so far, then "Clipping page 12 of 45" with the page's address and a bar.
 */
@Composable
internal fun ChatClipProgress(
    progress: PageProgress,
    modifier: Modifier = Modifier
) {
    val found = progress.found

    val text = if (found != null) {
        replyString(Res.string.chat_step_finding, found)
    } else {
        replyString(
            Res.string.chat_step_clipping,
            progress.done + 1,
            progress.total,
            progress.url.substringAfter(delimiter = "://")
        )
    }

    val total = progress.total.coerceAtLeast(minimumValue = 1)
    val fraction = if (found != null) null else progress.done.toFloat() / total

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // While the links are followed nobody knows how many pages there will be: the bar only moves.
        NotekeaProgressBar(
            label = stringResource(resource = Res.string.chat_step_clipping_label),
            fraction = fraction,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun ChatClipProgressPreview() {
    NotekeaPreviewThemes {
        ChatClipProgress(
            progress = PageProgress(
                done = 11,
                total = 45,
                url = "https://developer.android.com/develop/ui/compose/layouts"
            ),
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)
        )
    }
}
