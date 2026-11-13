package app.notekea.shell.presentation.onboarding

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.ai.domain.ModelSize
import app.notekea.ai.presentation.ModelManagerIntent
import app.notekea.ai.presentation.ModelManagerViewModel
import app.notekea.core.presentation.designsystem.components.NotekeaDialog
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.formatBytes
import app.notekea.core.presentation.platform.LocalWindowLoading
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
fun OnboardingHost(
    viewModel: OnboardingViewModel = koinViewModel(),
    welcome: WelcomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val welcomeState by welcome.state.collectAsState()

    val isOpen = OnboardingGate.isOpen(
        isShown = state.isShown,
        isStarting = welcomeState.isLoading,
        openedVault = welcomeState.openedVault?.path,
        loadedVault = LocalWindowLoading.current.loadedVault
    )

    if (isOpen) {
        OnboardingSteps(
            state = state,
            onIntent = viewModel::onIntent,
            welcome = welcome
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

    // Also when the notes step is first reached with a vault already open (restored at startup): it has nothing to
    // ask. Once per vault, so Back from the AI step still reaches the notes.
    var passed by remember(openedVault) { mutableStateOf(value = false) }

    LaunchedEffect(openedVault, state.step) {
        if (openedVault != null &&
            !passed &&
            state.step == OnboardingStep.NOTES
        ) {
            passed = true
            onIntent(OnboardingIntent.OnVaultOpened)
        }
    }

    // The new vault form was cancelled: the sheet comes back instead of staying out of the way for good.
    val hasForm = welcomeState.newVaultForm != null

    LaunchedEffect(hasForm) {
        if (!hasForm && openedVault == null) onIntent(OnboardingIntent.OnWaitEnd)
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
