package app.notekea.vault.presentation.welcome

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.vault.domain.VaultError
import kotlin.test.Test
import kotlin.test.assertEquals

/** Why a repository could not be brought, in plain words (import a vault from a repository, 2026-11-14). */
@OptIn(ExperimentalTestApi::class)
class VaultErrorMessageTest {
    @Test
    fun `each way an import fails says what happened and what to do`() = runComposeUiTest {
        val errors = listOf(
            VaultError.RepositoryBadAddress,
            VaultError.RepositoryRefused,
            VaultError.RepositoryOffline,
            VaultError.RepositoryFailed(message = "not a repository")
        )

        setContent {
            NotekeaTheme(darkTheme = false) {
                Column {
                    errors.forEach {
                        Text(
                            text = vaultErrorMessage(
                                error = it,
                                platformConventions = PreviewPlatformConventions.linux
                            )
                        )
                    }
                }
            }
        }

        assertEquals(
            expected = listOf(1, 1, 1, 1),
            actual = listOf("HTTPS address", "refused the token", "could not be reached", "not a repository").map {
                onAllNodesWithText(
                    text = it,
                    substring = true
                ).fetchSemanticsNodes().size
            }
        )
    }
}
