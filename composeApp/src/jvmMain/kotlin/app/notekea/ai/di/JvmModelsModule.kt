package app.notekea.ai.di

import app.notekea.core.notes.data.readCommand
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.ai.data.CoroutineModelInstaller
import app.notekea.ai.data.JvmHardwareProbe
import app.notekea.ai.data.KtorModelDownloader
import app.notekea.ai.data.ModelFolder
import app.notekea.ai.data.NioModelStorage
import app.notekea.ai.domain.HardwareProbe
import app.notekea.ai.domain.ModelDownloader
import app.notekea.ai.domain.ModelInstaller
import app.notekea.ai.domain.ModelStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.dsl.onClose
import java.nio.file.Path

/**
 * The desktop side of the model manager: the `models` folder of the app data, downloads with Ktor on OkHttp, the
 * hardware read from the JVM, and the installer's coroutines, cancelled with Koin (the Self-Test starts it per case).
 */
val jvmModelsModule = module {
    single { ModelFolder(root = Path.of(get<PlatformConventions>().appDataDirectory, MODELS_FOLDER)) }
    single { HttpClient(engineFactory = OkHttp) { expectSuccess = false } } onClose { it?.close() }
    single<ModelStorage> { NioModelStorage(modelFolder = get(), dispatchers = get()) }
    single<ModelDownloader> { KtorModelDownloader(client = get(), folder = get(), dispatchers = get()) }
    single<HardwareProbe> {
        JvmHardwareProbe(
            operatingSystem = get<PlatformConventions>().operatingSystem,
            readCommand = ::readCommand,
            dispatchers = get()
        )
    }
    single {
        CoroutineModelInstaller(
            install = get(),
            catalogSource = get(),
            storage = get(),
            activeModelStore = get(),
            dispatchers = get(),
            clock = get()
        )
    } onClose { it?.close() } bind ModelInstaller::class
}

private const val MODELS_FOLDER = "models"
