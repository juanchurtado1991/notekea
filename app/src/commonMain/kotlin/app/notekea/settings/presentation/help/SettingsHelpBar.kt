package app.notekea.settings.presentation.help

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.fields.NotekeaPromptField
import app.notekea.core.presentation.designsystem.components.layout.NotekeaScrollArea
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.settings.domain.SettingsPlace
import app.notekea.resources.Res
import app.notekea.resources.chat_send
import app.notekea.resources.chat_stop
import app.notekea.resources.settings_help_failed
import app.notekea.resources.settings_help_hint
import app.notekea.resources.settings_help_intro
import app.notekea.resources.settings_help_no_model
import app.notekea.resources.settings_help_show
import app.notekea.resources.settings_help_sync_steps
import app.notekea.resources.settings_help_title
import app.notekea.settings.domain.SettingsHelpError
import app.notekea.settings.domain.SettingsHelpExchange
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_WIDTH_DP = 420
private const val PREVIEW_HEIGHT_DP = 520

/**
 * Ask about Settings, the bar beside the dialog: what it answers, or Sync's five steps while [isSync] (no model
 * needed), each question and its reply scrolling over a one-line field; without a model it says where to get one.
 */
@Composable
internal fun SettingsHelpBar(
    state: SettingsHelpState,
    isSync: Boolean,
    onDraftChange: (String) -> Unit,
    onAsk: () -> Unit,
    onStop: () -> Unit,
    onShow: (SettingsPlace) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(all = NotekeaDimensions.SpacingL),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)
    ) {
        Text(
            text = stringResource(resource = Res.string.settings_help_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Conversation(
            state = state,
            isSync = isSync,
            onShow = onShow,
            modifier = Modifier.weight(weight = 1f)
        )

        NotekeaPromptField(
            value = state.draft,
            onValueChange = onDraftChange,
            placeholder = stringResource(resource = Res.string.settings_help_hint),
            sendLabel = stringResource(resource = Res.string.chat_send),
            stopLabel = stringResource(resource = Res.string.chat_stop),
            isRunning = state.isAnswering,
            canSend = state.canAsk,
            onSend = onAsk,
            onStop = onStop,
            modifier = Modifier.fillMaxWidth(),
            isCompact = true
        )
    }
}

// What it answers (Sync's steps), then each question and its reply; it scrolls to the newest words as they come.
@Composable
private fun Conversation(
    state: SettingsHelpState,
    isSync: Boolean,
    onShow: (SettingsPlace) -> Unit,
    modifier: Modifier
) {
    val intro = if (isSync) Res.string.settings_help_sync_steps else Res.string.settings_help_intro

    val scrollState = rememberScrollState()

    LaunchedEffect(state.exchanges, state.error) {
        scrollState.animateScrollTo(value = scrollState.maxValue)
    }

    NotekeaScrollArea(
        state = scrollState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = scrollState),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)
        ) {
            Reply(text = stringResource(resource = intro))

            state.exchanges.forEach { exchange ->
                Exchange(
                    exchange = exchange,
                    onShow = onShow
                )
            }

            state.error?.let { Problem(error = it) }
        }
    }
}

@Composable
private fun Exchange(
    exchange: SettingsHelpExchange,
    onShow: (SettingsPlace) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = exchange.question,
            modifier = Modifier
                .clip(shape = MaterialTheme.shapes.medium)
                .background(color = MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(all = NotekeaDimensions.SpacingM),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (exchange.reply.isNotEmpty()) Reply(text = exchange.reply)

    exchange.place?.let { place ->
        NotekeaButton(
            text = stringResource(
                resource = Res.string.settings_help_show,
                stringResource(resource = nameOf(place = place))
            ),
            onClick = { onShow(place) },
            style = NotekeaButtonStyle.SECONDARY
        )
    }
}

@Composable
private fun Reply(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = NotekeaDimensions.BorderWidth,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(all = NotekeaDimensions.SpacingM),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun Problem(error: SettingsHelpError) {
    val message = when (error) {
        SettingsHelpError.NoModel -> Res.string.settings_help_no_model
        SettingsHelpError.ModelFailed -> Res.string.settings_help_failed
    }

    Text(
        text = stringResource(resource = message),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview
@Composable
private fun SettingsHelpBarPreview() {
    NotekeaPreviewThemes {
        SettingsHelpBar(
            state = SettingsHelpState(
                exchanges = listOf(
                    SettingsHelpExchange(
                        question = "¿qué es un token?",
                        reply = "Es una llave que Notekea usa en lugar de tu contraseña.",
                        place = SettingsPlace.SYNC_TOKEN
                    )
                )
            ),
            isSync = true,
            onDraftChange = {},
            onAsk = {},
            onStop = {},
            onShow = {},
            modifier = Modifier
                .width(width = PREVIEW_WIDTH_DP.dp)
                .height(height = PREVIEW_HEIGHT_DP.dp)
        )
    }
}
