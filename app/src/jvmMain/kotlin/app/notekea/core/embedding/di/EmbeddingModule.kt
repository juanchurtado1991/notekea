package app.notekea.core.embedding.di

import app.notekea.core.embedding.data.LlamaTextEmbedder
import app.notekea.core.embedding.domain.TextEmbedder
import app.notekea.core.platform.domain.PlatformConventions
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.dsl.onClose
import java.nio.file.Path

/**
 * The embedding model: the bundled one, in the resources Compose gives the app
 * (`compose.application.resources.dir`), with java-llama.cpp's native library in the app data's `natives` folder,
 * closed with Koin so its timer and memory do not outlive it (the Auto-Test starts Koin once per case).
 */
val embeddingModule = module {
    single {
        val resources = System.getProperty(RESOURCES_PROPERTY).orEmpty()

        LlamaTextEmbedder(
            modelPath = Path.of(
                resources,
                LlamaTextEmbedder.MODEL_FOLDER,
                LlamaTextEmbedder.MODEL_FILE
            ).toString(),
            nativeFolder = Path.of(
                get<PlatformConventions>().appDataDirectory,
                NATIVE_FOLDER
            ).toString()
        )
    } onClose { it?.close() } bind TextEmbedder::class
}

private const val NATIVE_FOLDER = "natives"
private const val RESOURCES_PROPERTY = "compose.application.resources.dir"
