package app.notekea.vault.presentation.welcome

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.vault.domain.VaultError
import kotlin.test.Test
import kotlin.test.assertEquals

/** The welcome screen's import panel (the developer's choice of 2026-11-14): each problem under its own field. */
@OptIn(ExperimentalTestApi::class)
class ImportVaultPanelTest {
    @Test
    fun `every problem of an import has a field of the panel to be said under`() {
        assertEquals(
            expected = listOf(
                ImportProblemPlace.ADDRESS,
                ImportProblemPlace.ADDRESS,
                ImportProblemPlace.ADDRESS,
                ImportProblemPlace.TOKEN,
                ImportProblemPlace.NAME,
                ImportProblemPlace.NAME,
                ImportProblemPlace.LOCATION,
                ImportProblemPlace.LOCATION,
                ImportProblemPlace.LOCATION,
                null
            ),
            actual = listOf(
                VaultError.RepositoryBadAddress,
                VaultError.RepositoryOffline,
                VaultError.RepositoryFailed(message = "x"),
                VaultError.RepositoryRefused,
                VaultError.InvalidName,
                VaultError.FolderAlreadyExists(path = PATH),
                VaultError.CreationFailed(path = PATH),
                VaultError.FolderMissing(path = PATH),
                VaultError.PermissionDenied(path = PATH),
                null
            ).map { ImportProblemPlace.of(error = it) }
        )
    }

    @Test
    fun `while it imports, Import waits and Cancel still stops it`() = runComposeUiTest {
        val intents = mutableListOf<WelcomeIntent>()

        setContent {
            NotekeaTheme(darkTheme = false) {
                ImportVaultPanel(
                    form = WelcomePreviewData.importForm,
                    platformConventions = WelcomePreviewData.platformConventions,
                    onIntent = { intents += it },
                    error = null,
                    isBusy = true
                )
            }
        }

        onNodeWithText(text = "Import").performClick()
        onNodeWithText(text = "Cancel").performClick()
        waitForIdle()

        assertEquals(
            expected = listOf<Any>(listOf(WelcomeIntent.OnImportCancel), 1),
            actual = listOf<Any>(
                intents,
                onAllNodesWithText(text = "Bringing the repository…").fetchSemanticsNodes().size
            )
        )
    }

    @Test
    fun `a refused token is said under the token, not in a banner`() = runComposeUiTest {
        setContent {
            NotekeaTheme(darkTheme = false) {
                ImportVaultPanel(
                    form = WelcomePreviewData.importForm,
                    platformConventions = WelcomePreviewData.platformConventions,
                    onIntent = {},
                    error = VaultError.RepositoryRefused
                )
            }
        }

        val token = onNodeWithText(text = "Token")
            .fetchSemanticsNode()
            .boundsInRoot
            .top

        val name = onNodeWithText(text = "Folder name")
            .fetchSemanticsNode()
            .boundsInRoot
            .top

        val problem = onNodeWithText(
            text = "refused the token",
            substring = true
        )
            .fetchSemanticsNode()
            .boundsInRoot
            .top

        assertEquals(
            expected = true,
            actual = problem > token && problem < name
        )
    }

    private companion object {
        const val PATH = "/home/ana/Documents/notes"
    }
}
