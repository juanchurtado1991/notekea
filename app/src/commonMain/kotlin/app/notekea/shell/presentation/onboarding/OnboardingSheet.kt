package app.notekea.shell.presentation.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.NotekeaLogo
import app.notekea.core.presentation.designsystem.components.NotekeaSettingsGroup
import app.notekea.core.presentation.designsystem.components.NotekeaSettingsRow
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.onboarding_ai_choice
import app.notekea.resources.onboarding_ai_download
import app.notekea.resources.onboarding_ai_text
import app.notekea.resources.onboarding_ai_title
import app.notekea.resources.onboarding_ask_text
import app.notekea.resources.onboarding_ask_title
import app.notekea.resources.onboarding_back
import app.notekea.resources.onboarding_continue
import app.notekea.resources.onboarding_create_vault
import app.notekea.resources.onboarding_not_now
import app.notekea.resources.onboarding_notes_text
import app.notekea.resources.onboarding_notes_title
import app.notekea.resources.onboarding_open_folder
import app.notekea.resources.onboarding_private_text
import app.notekea.resources.onboarding_private_title
import app.notekea.resources.onboarding_related_text
import app.notekea.resources.onboarding_related_title
import app.notekea.resources.onboarding_sample_vault
import app.notekea.resources.onboarding_skip
import app.notekea.resources.onboarding_step
import app.notekea.resources.onboarding_welcome_title
import org.jetbrains.compose.resources.stringResource

private const val LOGO_DP = 56
private const val STEP_ICON_DP = 36
private const val FEATURE_ICON_DP = 24
private const val PREVIEW_MODEL = "Medium"
private const val PREVIEW_SIZE = "2.7 GB"

/**
 * The first-run welcome's sheet (5.1), as Apple's welcome windows: a big mark, a title, the step's rows, and its
 * buttons at the bottom with "n of 3". The steps follow each other in place.
 */
@Composable
internal fun OnboardingSheet(
    step: OnboardingStep,
    actions: OnboardingActions,
    aiChoice: OnboardingAiChoice?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = NotekeaDimensions.SpacingXl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)
    ) {
        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(weight = 1f)
        ) { shown ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)
            ) {
                when (shown) {
                    OnboardingStep.WELCOME -> WelcomeStep()
                    OnboardingStep.NOTES -> NotesStep(actions = actions)
                    OnboardingStep.AI -> AiStep(aiChoice = aiChoice)
                }
            }
        }

        Buttons(
            step = step,
            actions = actions,
            hasAi = aiChoice != null
        )

        Text(
            text = stringResource(
                Res.string.onboarding_step,
                step.ordinal + 1,
                OnboardingStep.entries.size
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ColumnScope.WelcomeStep() {
    NotekeaLogo(size = LOGO_DP.dp)
    Title(text = stringResource(resource = Res.string.onboarding_welcome_title))

    Feature(
        icon = NotekeaIcons.OnboardingPrivate,
        title = stringResource(resource = Res.string.onboarding_private_title),
        text = stringResource(resource = Res.string.onboarding_private_text)
    )

    Feature(
        icon = NotekeaIcons.OnboardingRelated,
        title = stringResource(resource = Res.string.onboarding_related_title),
        text = stringResource(resource = Res.string.onboarding_related_text)
    )

    Feature(
        icon = NotekeaIcons.OnboardingAsk,
        title = stringResource(resource = Res.string.onboarding_ask_title),
        text = stringResource(resource = Res.string.onboarding_ask_text)
    )
}

@Composable
private fun ColumnScope.NotesStep(actions: OnboardingActions) {
    StepIcon(icon = NotekeaIcons.Folder)
    Title(text = stringResource(resource = Res.string.onboarding_notes_title))
    Subtitle(text = stringResource(resource = Res.string.onboarding_notes_text))

    NotekeaSettingsGroup(
        rows = listOf(
            {
                Choice(
                    icon = NotekeaIcons.FolderOpen,
                    label = stringResource(resource = Res.string.onboarding_open_folder),
                    onClick = actions.onOpenFolder
                )
            },
            {
                Choice(
                    icon = NotekeaIcons.NewVault,
                    label = stringResource(resource = Res.string.onboarding_create_vault),
                    onClick = actions.onCreateVault
                )
            },
            {
                Choice(
                    icon = NotekeaIcons.SampleVault,
                    label = stringResource(resource = Res.string.onboarding_sample_vault),
                    onClick = actions.onSampleVault
                )
            }
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ColumnScope.AiStep(aiChoice: OnboardingAiChoice?) {
    StepIcon(icon = NotekeaIcons.ChatAnswer)
    Title(text = stringResource(resource = Res.string.onboarding_ai_title))
    Subtitle(text = stringResource(resource = Res.string.onboarding_ai_text))

    aiChoice?.let {
        NotekeaSettingsGroup(
            rows = listOf({
                NotekeaSettingsRow(
                    label = stringResource(Res.string.onboarding_ai_choice, it.name, it.size)
                )
            }),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Buttons(
    step: OnboardingStep,
    actions: OnboardingActions,
    hasAi: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (step != OnboardingStep.WELCOME) {
            TextButton(
                text = stringResource(resource = Res.string.onboarding_back),
                onClick = actions.onBack
            )
        }

        Spacer(modifier = Modifier.weight(weight = 1f))

        when (step) {
            OnboardingStep.WELCOME -> {
                TextButton(
                    text = stringResource(resource = Res.string.onboarding_skip),
                    onClick = actions.onFinish
                )

                NotekeaButton(
                    text = stringResource(Res.string.onboarding_continue),
                    onClick = actions.onContinue
                )
            }

            OnboardingStep.NOTES -> {
                TextButton(
                    text = stringResource(resource = Res.string.onboarding_skip),
                    onClick = actions.onFinish
                )
            }

            OnboardingStep.AI -> {
                TextButton(
                    text = stringResource(resource = Res.string.onboarding_not_now),
                    onClick = actions.onFinish
                )

                if (hasAi) {
                    NotekeaButton(
                        text = stringResource(Res.string.onboarding_ai_download),
                        onClick = actions.onDownload
                    )
                }
            }
        }
    }
}

@Composable
private fun TextButton(
    text: String,
    onClick: () -> Unit
) {
    NotekeaButton(
        text = text,
        onClick = onClick,
        style = NotekeaButtonStyle.TEXT
    )
}

@Composable
private fun Title(text: String) {
    Text(
        text = text,
        style = MaterialTheme
            .typography
            .titleLarge
            .copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun Subtitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun StepIcon(icon: ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(size = STEP_ICON_DP.dp),
        tint = MaterialTheme.colorScheme.primary
    )
}

// One promise of the welcome: an icon in the theme's color, a bold title and a line under it.
@Composable
private fun Feature(
    icon: ImageVector,
    title: String,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(size = FEATURE_ICON_DP.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Column {
            Text(
                text = title,
                style = MaterialTheme
                    .typography
                    .bodyMedium
                    .copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// A way to choose the notes, a row that acts on a click.
@Composable
private fun Choice(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(
                horizontal = NotekeaDimensions.SpacingM,
                vertical = NotekeaDimensions.SpacingS
            ),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(size = NotekeaDimensions.IconSize),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = label,
            modifier = Modifier.weight(weight = 1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Icon(
            imageVector = NotekeaIcons.Expand,
            contentDescription = null,
            modifier = Modifier.size(size = NotekeaDimensions.IconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
private fun OnboardingSheetPreview() {
    NotekeaPreviewThemes {
        OnboardingSheet(
            step = OnboardingStep.WELCOME,
            actions = OnboardingActions(),
            aiChoice = OnboardingAiChoice(
                name = PREVIEW_MODEL,
                size = PREVIEW_SIZE
            )
        )
    }
}
