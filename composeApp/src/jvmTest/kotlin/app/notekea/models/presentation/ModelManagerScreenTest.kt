package app.notekea.models.presentation

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.clickText
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.settings.AppearanceViewModel
import app.notekea.core.presentation.settings.SettingsDialog
import app.notekea.core.appearance.domain.FakeAppearanceStore
import app.notekea.core.presentation.texts
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.models.domain.FakeActiveModelStore
import app.notekea.models.domain.FakeHardwareProbe
import app.notekea.models.domain.FakeModelCatalogSource
import app.notekea.models.domain.FakeModelInstaller
import app.notekea.models.domain.FakeModelStorage
import app.notekea.models.domain.InstallProgress
import app.notekea.models.domain.InstallSession
import app.notekea.models.domain.ModelCatalogs
import app.notekea.models.domain.ModelSize
import app.notekea.models.domain.RecommendModelUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Settings → AI model (`ROADMAP.md` 3.1, design C), rendered headless inside Settings: its section opens from the
 * list, the recommended size downloads, pauses and resumes, the other sizes unfold, and deleting asks first.
 */
@OptIn(InternalComposeUiApi::class)
class ModelManagerScreenTest {
    private var frame = 0L
    private val storage = FakeModelStorage()
    private val activeModelStore = FakeActiveModelStore()
    private val installer = FakeModelInstaller()

    private fun viewModel() = ModelManagerViewModel(
        catalogSource = FakeModelCatalogSource(),
        hardwareProbe = FakeHardwareProbe(),
        storage = storage,
        activeModelStore = activeModelStore,
        installer = installer,
        recommendModel = RecommendModelUseCase(),
        platformConventions = PreviewPlatformConventions.linux
    )

    private fun render(
        check: (ImageComposeScene) -> Unit
    ) {
        val viewModel = viewModel()
        UiThread.run {
            ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) {
                    SettingsDialog(
                        onDismiss = {},
                        aiModelSection = { ModelManagerScreen(modifier = it, viewModel = viewModel) },
                        viewModel = AppearanceViewModel(appearanceStore = FakeAppearanceStore())
                    )
                }
            }.use { scene ->
                settle(scene = scene)
                scene.clickText(text = SECTION) { settle(scene = scene) }
                check(scene)
            }
        }
    }

    @Test
    fun `the AI model section shows the recommended size, which downloads, pauses and resumes`() {
        val texts = mutableListOf<List<String>>()
        render { scene ->
            texts += scene.texts()
            scene.clickText(text = DOWNLOAD) { settle(scene = scene) }
            installer.current.value = InstallSession(
                size = ModelSize.MEDIUM,
                progress = InstallProgress.Downloading(
                    doneBytes = 1_200_000_000,
                    totalBytes = 3_000_000_000,
                    bytesPerSecond = 24_500_000
                )
            )
            settle(scene = scene)
            texts += scene.texts()
            scene.clickText(text = PAUSE) { settle(scene = scene) }
            installer.current.value = installer.current.value?.copy(isPaused = true)
            settle(scene = scene)
            scene.clickText(text = RESUME) { settle(scene = scene) }
        }

        assertTrue(actual = "Medium · Model medium" in texts[0] && "Recommended" in texts[0])
        assertTrue(actual = "Model large" !in texts[0].joinToString())
        assertTrue(actual = "1.2 GB of 3.0 GB · 24.5 MB/s" in texts[1])
        assertEquals(expected = listOf(ModelSize.MEDIUM, ModelSize.MEDIUM), actual = installer.started)
        assertEquals(expected = 1, actual = installer.pauses)
    }

    @Test
    fun `the other sizes unfold with what this computer lacks for them`() {
        var texts = emptyList<String>()
        render { scene ->
            scene.clickText(text = OTHER_SIZES) { settle(scene = scene) }
            texts = scene.texts()
        }

        assertTrue(actual = "Large · Model large" in texts && "Small · Model small" in texts)
        assertTrue(actual = "Needs 32 GB of memory; this computer has 16 GB." in texts)
    }

    @Test
    fun `deleting the model in use asks first, and only the confirmation deletes it`() {
        storage.installed += ModelCatalogs.medium.file.fileName
        activeModelStore.kept.value = ModelSize.MEDIUM
        var asked = emptyList<String>()
        render { scene ->
            scene.clickText(text = DELETE) { settle(scene = scene) }
            asked = scene.texts()
            scene.clickText(text = DELETE, occurrence = 1) { settle(scene = scene) }
        }

        assertTrue(actual = "Delete Medium?" in asked)
        assertEquals(expected = listOf("medium.gguf"), actual = storage.deleted)
        assertEquals(expected = null, actual = activeModelStore.kept.value)
    }

    private fun settle(
        scene: ImageComposeScene
    ) {
        repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = (frame++) * FRAME_NANOS) }
    }

    companion object {
        private const val DELETE = "Delete"
        private const val DOWNLOAD = "Download"
        private const val FRAME_NANOS = 16_000_000L
        private const val HEIGHT = 700
        private const val OTHER_SIZES = "OTHER SIZES"
        private const val PAUSE = "Pause"
        private const val RESUME = "Resume"
        private const val SECTION = "AI model"
        private const val SETTLE_FRAMES = 8
        private const val WIDTH = 1000
    }
}
