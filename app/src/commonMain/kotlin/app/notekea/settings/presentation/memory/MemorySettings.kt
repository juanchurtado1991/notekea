package app.notekea.settings.presentation.memory

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.memory.domain.MemoryField
import app.notekea.core.memory.domain.MemoryLine
import app.notekea.core.memory.domain.MemorySection
import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.NotekeaConfirmDialog
import app.notekea.core.presentation.designsystem.components.NotekeaIconButton
import app.notekea.core.presentation.designsystem.components.NotekeaMutedText
import app.notekea.core.presentation.designsystem.components.feedback.NotekeaSpotlight
import app.notekea.core.presentation.designsystem.components.fields.NotekeaSwitch
import app.notekea.core.presentation.designsystem.components.fields.NotekeaTextField
import app.notekea.core.presentation.designsystem.components.menus.NotekeaContextMenuArea
import app.notekea.core.presentation.designsystem.components.menus.NotekeaMenuItem
import app.notekea.core.presentation.designsystem.components.menus.NotekeaPopUpButton
import app.notekea.core.presentation.designsystem.components.settings.NotekeaSettingsChoiceRow
import app.notekea.core.presentation.designsystem.components.settings.NotekeaSettingsGroup
import app.notekea.core.presentation.designsystem.components.settings.NotekeaSettingsPage
import app.notekea.core.presentation.designsystem.components.settings.NotekeaSettingsRow
import app.notekea.core.presentation.designsystem.components.settings.NotekeaSettingsSwitchRow
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.core.presentation.settings.SettingsSpotlight
import app.notekea.core.settings.domain.SettingsPlace
import app.notekea.resources.Res
import app.notekea.resources.settings_folder_root
import app.notekea.resources.settings_memory_about
import app.notekea.resources.settings_memory_cancel
import app.notekea.resources.settings_memory_country
import app.notekea.resources.settings_memory_currency
import app.notekea.resources.settings_memory_empty
import app.notekea.resources.settings_memory_failed
import app.notekea.resources.settings_memory_forget
import app.notekea.resources.settings_memory_forget_all
import app.notekea.resources.settings_memory_forget_all_message
import app.notekea.resources.settings_memory_forget_all_title
import app.notekea.resources.settings_memory_language
import app.notekea.resources.settings_memory_learnt
import app.notekea.resources.settings_memory_new_notes
import app.notekea.resources.settings_memory_no_vault
import app.notekea.resources.settings_memory_on
import app.notekea.resources.settings_memory_on_detail
import app.notekea.resources.settings_memory_where
import app.notekea.resources.settings_section_memory
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private const val FIELD_WIDTH_DP = 200

/**
 * Settings → Chat memory (`docs/agent-memory.md`, "How the person sets it up"), drawn as macOS's System Settings
 * draws a pane (chosen by the developer, 2026-11-15): the switch, "About you" with its simple fields, and what the
 * chat learnt, a row a line, Forget on hover and on right-click; Forget everything… at the bottom, confirmed first.
 */
@Composable
internal fun MemorySettings(
    state: MemorySettingsState,
    onIntent: (MemorySettingsIntent) -> Unit,
    buttonOrder: DialogButtonOrder,
    spotlight: SettingsSpotlight?,
    modifier: Modifier = Modifier
) {
    // Read again each time the page shows: the chat may have remembered something since.
    LaunchedEffect(Unit) { onIntent(MemorySettingsIntent.OnShown) }

    NotekeaSettingsPage(
        title = stringResource(resource = Res.string.settings_section_memory),
        modifier = modifier
    ) {
        Switch(state, onIntent, spotlight)

        if (!state.hasVault) {
            NotekeaMutedText(
                text = stringResource(resource = Res.string.settings_memory_no_vault),
                style = MaterialTheme.typography.bodyMedium
            )

            return@NotekeaSettingsPage
        }

        val failed = stringResource(resource = Res.string.settings_memory_failed)

        if (state.hasFailed) {
            NotekeaMutedText(
                text = failed,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        About(state, onIntent)
        Learnt(state.lines, onIntent)

        val forgetAllAt = spotlight?.serialFor(place = SettingsPlace.CHAT_MEMORY_FORGET_ALL)

        NotekeaSpotlight(shownAt = forgetAllAt) {
            NotekeaButton(
                text = stringResource(resource = Res.string.settings_memory_forget_all),
                onClick = { onIntent(MemorySettingsIntent.OnForgetAll) },
                style = NotekeaButtonStyle.SECONDARY
            )
        }
    }

    if (state.isForgetting) {
        NotekeaConfirmDialog(
            title = stringResource(resource = Res.string.settings_memory_forget_all_title),
            message = stringResource(resource = Res.string.settings_memory_forget_all_message),
            confirmLabel = stringResource(resource = Res.string.settings_memory_forget),
            cancelLabel = stringResource(resource = Res.string.settings_memory_cancel),
            buttonOrder = buttonOrder,
            onConfirm = { onIntent(MemorySettingsIntent.OnForgetAllConfirm) },
            onDismiss = { onIntent(MemorySettingsIntent.OnForgetAllCancel) },
            isDestructive = true
        )
    }
}

@Composable
private fun Switch(
    state: MemorySettingsState,
    onIntent: (MemorySettingsIntent) -> Unit,
    spotlight: SettingsSpotlight?
) {
    val label = stringResource(resource = Res.string.settings_memory_on)

    NotekeaSpotlight(shownAt = spotlight?.serialFor(place = SettingsPlace.CHAT_MEMORY_ON)) {
        NotekeaSettingsGroup(
            rows = listOf {
                NotekeaSettingsSwitchRow(
                    label = label,
                    isOn = state.isOn,
                    onToggle = { onIntent(MemorySettingsIntent.OnToggle) },
                    detail = stringResource(resource = Res.string.settings_memory_on_detail)
                )
            }
        )
    }
}

// The simple fields, each a line of the memory the chat reads at every turn.
@Composable
private fun About(
    state: MemorySettingsState,
    onIntent: (MemorySettingsIntent) -> Unit
) {
    val change = { field: MemoryField, value: String ->
        onIntent(
            MemorySettingsIntent.OnFieldChange(
                field = field,
                value = value
            )
        )
    }

    NotekeaSettingsGroup(
        header = stringResource(resource = Res.string.settings_memory_about),
        rows = listOf(
            { FieldRow(Res.string.settings_memory_country, MemoryField.COUNTRY, state, change) },
            { FieldRow(Res.string.settings_memory_currency, MemoryField.CURRENCY, state, change) },
            { FieldRow(Res.string.settings_memory_language, MemoryField.LANGUAGE, state, change) },
            {
                NewNotesRow(
                    state = state,
                    onChoose = { change(MemoryField.NEW_NOTES, it) }
                )
            }
        )
    )
}

@Composable
private fun FieldRow(
    label: StringResource,
    field: MemoryField,
    state: MemorySettingsState,
    onChange: (MemoryField, String) -> Unit
) {
    NotekeaSettingsRow(label = stringResource(resource = label)) {
        NotekeaTextField(
            value = state.valueOf(field = field),
            onValueChange = { onChange(field, it) },
            modifier = Modifier.width(width = FIELD_WIDTH_DP.dp)
        )
    }
}

@Composable
private fun NewNotesRow(
    state: MemorySettingsState,
    onChoose: (String) -> Unit
) {
    val label = stringResource(resource = Res.string.settings_memory_new_notes)
    val root = stringResource(resource = Res.string.settings_folder_root)
    val current = state.valueOf(field = MemoryField.NEW_NOTES)

    val choices = (listOf("") + state.folders).map { folder ->
        NotekeaMenuItem(
            label = folder.ifEmpty { root },
            onClick = { onChoose(folder) },
            icon = NotekeaIcons.Folder,
            isChecked = folder == current
        )
    }

    NotekeaSettingsChoiceRow(
        label = label,
        choice = current.ifEmpty { root },
        choices = choices
    )
}

// What the chat learnt, a row a line with where it came from; empty, what it can learn.
@Composable
private fun Learnt(
    lines: List<MemoryLine>,
    onIntent: (MemorySettingsIntent) -> Unit
) {
    val header = stringResource(resource = Res.string.settings_memory_learnt)
    val footnote = stringResource(resource = Res.string.settings_memory_where)

    if (lines.isEmpty()) {
        NotekeaSettingsGroup(
            header = header,
            footnote = footnote,
            rows = listOf {
                val empty = stringResource(resource = Res.string.settings_memory_empty)
                NotekeaSettingsRow(label = empty)
            }
        )

        return
    }

    NotekeaSettingsGroup(
        header = header,
        footnote = footnote,
        rows = lines.map { line ->
            {
                LearntRow(
                    line = line,
                    onForget = { onIntent(MemorySettingsIntent.OnForget(text = line.text)) }
                )
            }
        }
    )
}

@Composable
private fun LearntRow(
    line: MemoryLine,
    onForget: () -> Unit
) {
    val forget = stringResource(resource = Res.string.settings_memory_forget)
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()

    NotekeaContextMenuArea(
        sections = listOf(
            listOf(
                NotekeaMenuItem(
                    label = forget,
                    onClick = onForget,
                    icon = NotekeaIcons.Delete
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .hoverable(interactionSource = interaction),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
        ) {
            NotekeaSettingsRow(
                label = line.text,
                detail = line.origin,
                modifier = Modifier.weight(weight = 1f)
            )

            if (isHovered) {
                NotekeaIconButton(
                    icon = NotekeaIcons.Delete,
                    label = forget,
                    onClick = onForget
                )
            }
        }
    }
}

@Preview
@Composable
private fun MemorySettingsPreview() {
    NotekeaPreviewThemes {
        MemorySettings(
            state = MemorySettingsState(
                hasVault = true,
                fields = mapOf(MemoryField.COUNTRY to "Colombia", MemoryField.CURRENCY to "COP"),
                lines = listOf(
                    MemoryLine(MemorySection.PEOPLE, "\"Laura\" is [[Laura Castro]]", "2026-11-15, chat: you chose"),
                    MemoryLine(MemorySection.SOURCES, "Trusted: fao.org", "2026-11-15, chat: you said")
                )
            ),
            onIntent = {},
            buttonOrder = DialogButtonOrder.CANCEL_THEN_PRIMARY,
            spotlight = null
        )
    }
}
