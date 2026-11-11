package app.notekea.shell.presentation.onboarding

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.ai.domain.ModelSize
import app.notekea.ai.presentation.ModelManagerIntent
import app.notekea.ai.presentation.ModelManagerViewModel
import app.notekea.core.presentation.designsystem.components.NotekeaDialog
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.formatBytes
import app.notekea.resources.Res
import app.notekea.resources.welcome_new_vault_default_name
import app.notekea.vault.presentation.welcome.WelcomeIntent
import app.notekea.vault.presentation.welcome.WelcomeViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val SHEET_WIDTH_DP = 460
private const val SHEET_HEIGHT_DP = 440

/**
 * The first-run welcome over the window (5.1): a sheet the app shows until it is finished or skipped; its notes step
 * acts through the welcome screen's own view model, and its AI step through Settings → AI model's.
 */
@Composable
fun OnboardingHost(viewModel: OnboardingViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()

    if (state.isShown) {
        OnboardingSteps(
            state = state,
            onIntent = viewModel::onIntent
        )
    }
}

@Composable
private fun OnboardingSteps(
    state: OnboardingState,
    onIntent: (OnboardingIntent) -> Unit,
    welcome: WelcomeViewModel = koinViewModel(),
    models: ModelManagerViewModel = koinViewModel()
) {
    val welcomeState by welcome.state.collectAsState()
    val modelState by models.state.collectAsState()
    val suggestedName = stringResource(resource = Res.string.welcome_new_vault_default_name)
    val openedVault = welcomeState.openedVault

    LaunchedEffect(openedVault) {
        if (openedVault != null) onIntent(OnboardingIntent.OnVaultOpened)
    }

    if (state.isWaiting) return

    val recommended = modelState.models.firstOrNull {
        it.size == modelState.recommended
    }

    val hasModel = modelState.active != null

    val aiChoice = recommended?.takeIf {
        !hasModel
    }?.let {
        OnboardingAiChoice(
            name = it.name,
            size = formatBytes(bytes = it.downloadBytes)
        )
    }

    val actions = OnboardingActions(
        onContinue = { onIntent(OnboardingIntent.OnContinue) },
        onBack = { onIntent(OnboardingIntent.OnBack) },
        onFinish = { onIntent(OnboardingIntent.OnFinish) },
        onOpenFolder = { welcome.onIntent(WelcomeIntent.OnOpenFolderClick) },
        onCreateVault = {
            createVault(
                welcome = welcome,
                name = suggestedName,
                onIntent = onIntent
            )
        },
        onSampleVault = { welcome.onIntent(WelcomeIntent.OnSampleVaultClick) },
        onDownload = {
            download(
                models = models,
                size = recommended?.size,
                onIntent = onIntent
            )
        }
    )

    NotekeaDialog(
        onDismiss = { onIntent(OnboardingIntent.OnFinish) },
        modifier = Modifier.size(
            width = SHEET_WIDTH_DP.dp,
            height = SHEET_HEIGHT_DP.dp
        )
    ) {
        OnboardingSheet(
            step = state.step,
            actions = actions,
            aiChoice = aiChoice
        )
    }
}

// The welcome screen's new vault form takes over until the vault opens, the sheet waiting behind it.
private fun createVault(
    welcome: WelcomeViewModel,
    name: String,
    onIntent: (OnboardingIntent) -> Unit
) {
    onIntent(OnboardingIntent.OnWait)
    welcome.onIntent(WelcomeIntent.OnCreateVaultClick(suggestedName = name))
}

// The recommended model downloads in the background, as from Settings → AI model, and the welcome ends.
private fun download(
    models: ModelManagerViewModel,
    size: ModelSize?,
    onIntent: (OnboardingIntent) -> Unit
) {
    size?.let { models.onIntent(ModelManagerIntent.OnInstall(size = it)) }
    onIntent(OnboardingIntent.OnFinish)
}

@Preview
@Composable
private fun OnboardingHostPreview() {
    NotekeaPreviewThemes {
        OnboardingSheet(
            step = OnboardingStep.NOTES,
            actions = OnboardingActions(),
            aiChoice = null
        )
    }
}
