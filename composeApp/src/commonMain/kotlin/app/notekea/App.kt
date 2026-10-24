package app.notekea

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.vault.presentation.welcome.WelcomeContent
import app.notekea.vault.presentation.welcome.WelcomePreviewData

/** Root of the window: the Notekea theme and background around the current screen. */
@Composable
fun App(
    content: @Composable () -> Unit
) {
    NotekeaTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            content = content
        )
    }
}

@Preview
@Composable
private fun AppPreview() {
    App {
        WelcomeContent(state = WelcomePreviewData.state(), onIntent = {}, openedVaultContent = { _, _ -> })
    }
}
