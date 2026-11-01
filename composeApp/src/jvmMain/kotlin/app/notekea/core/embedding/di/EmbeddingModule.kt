package app.notekea.core.embedding.di

import app.notekea.core.embedding.data.LlamaTextEmbedder
import app.notekea.core.embedding.domain.TextEmbedder
import app.notekea.core.platform.domain.PlatformConventions
import org.koin.dsl.module
import java.nio.file.Path

/**
 * The embedding model (`ROADMAP.md` 2.2): the bundled one, in the resources Compose gives the app
 * (`compose.application.resources.dir`), with java-llama.cpp's native library in the app data's `natives` folder.
 */
val embeddingModule = module {
    single<TextEmbedder> {
        val resources = System.getProperty(RESOURCES_PROPERTY).orEmpty()
        LlamaTextEmbedder(
            modelPath = Path.of(resources, LlamaTextEmbedder.MODEL_FOLDER, LlamaTextEmbedder.MODEL_FILE).toString(),
            nativeFolder = Path.of(get<PlatformConventions>().appDataDirectory, NATIVE_FOLDER).toString(),
            dispatchers = get()
        )
    }
}

private const val NATIVE_FOLDER = "natives"
private const val RESOURCES_PROPERTY = "compose.application.resources.dir"
