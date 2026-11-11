package app.notekea.chat.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.components.NotekeaStatusRing
import app.notekea.core.presentation.designsystem.components.NotekeaTooltip
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.chat_compacting
import app.notekea.resources.chat_memory
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/**
 * How full the chat's memory is ([usage], from 0 to 1), as Claude Code's context ring: it fills as the chat goes on
 * and empties when the chat compacts itself; the tooltip says the share, or that it is compacting.
 */
@Composable
internal fun ChatMemoryRing(
    usage: Float,
    isCompacting: Boolean,
    modifier: Modifier = Modifier
) {
    val percent = (
        usage.coerceIn(
            minimumValue = 0f,
            maximumValue = 1f
        ) * PERCENT
        ).roundToInt()

    val label = if (isCompacting) {
        stringResource(resource = Res.string.chat_compacting)
    } else {
        stringResource(resource = Res.string.chat_memory, percent)
    }

    NotekeaTooltip(label = label) {
        Box(
            modifier = modifier
                .semantics { contentDescription = label }
                .padding(all = NotekeaDimensions.SpacingXs)
        ) { NotekeaStatusRing(fraction = usage) }
    }
}

private const val PERCENT = 100

@Preview
@Composable
private fun ChatMemoryRingPreview() {
    NotekeaPreviewThemes {
        ChatMemoryRing(
            usage = 0.45f,
            isCompacting = false
        )
    }
}
