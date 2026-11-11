package app.notekea.autotest

import app.notekea.ai.data.BundledModelCatalogSource
import app.notekea.ai.domain.ModelCatalog
import app.notekea.ai.domain.ModelFile
import app.notekea.ai.domain.ModelSize
import app.notekea.core.data.JvmAppDispatchers
import com.testbuddy.Precondition
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * The chat models a case can use, already on this machine: those of the folder `-PchatModels` names (development), or
 * else those Notekea QA downloaded in Settings → AI model (a tester). Nothing is downloaded: without one, the cases
 * that ask the model are skipped.
 */
object ChatModels {
    private const val FOLDER_PROPERTY = "notekea.autotest.chatModels"
    private const val QA_MODELS = "models"
    private const val MISSING = "a chat model: download Medium in Notekea QA's Settings → AI model"

    private val catalog: ModelCatalog by lazy {
        runBlocking { BundledModelCatalogSource(dispatchers = JvmAppDispatchers()).load() }
    }

    /** What a case that asks any model needs. */
    val any = Precondition(missing = MISSING) {
        preferred() != null
    }

    /** What a case that needs Medium needs (Deep thinking, agent mode). */
    val medium = Precondition(missing = MISSING) { has(size = ModelSize.MEDIUM) }

    /** The size a case uses: Medium, as recommended, else the one there is. */
    fun preferred(): ModelSize? =
        listOf(ModelSize.MEDIUM, ModelSize.SMALL, ModelSize.LARGE).firstOrNull { has(size = it) }

    /** Puts the files of [size] and the reranker in [folder], as a download would, without copying when it can. */
    fun install(
        size: ModelSize,
        folder: Path
    ) {
        Files.createDirectories(folder)

        filesOf(size = size).forEach {
            link(
                file = it,
                folder = folder
            )
        }
    }

    private fun has(size: ModelSize): Boolean = filesOf(size = size).all { file ->
        val path = source().resolve(file.fileName)
        Files.isRegularFile(path) && Files.size(path) == file.sizeBytes
    }

    private fun filesOf(size: ModelSize): List<ModelFile> = catalog
        .models
        .filter {
            it.size == size
        }
        .map { it.file } + catalog.reranker

    private fun source(): Path = System.getProperty(FOLDER_PROPERTY)?.let { Path.of(it) }
        ?: QaAppData.current().resolve(QA_MODELS)

    // A hard link where the file system allows it, a symbolic one where not, and a copy as the last resort.
    private fun link(
        file: ModelFile,
        folder: Path
    ) {
        val from = source().resolve(file.fileName)
        val to = folder.resolve(file.fileName)
        if (Files.exists(to)) return

        try {
            Files.createLink(to, from)
        } catch (_: IOException) {
            try {
                Files.createSymbolicLink(to, from)
            } catch (_: IOException) {
                Files.copy(from, to)
            }
        } catch (_: UnsupportedOperationException) {
            Files.copy(from, to)
        }
    }
}
