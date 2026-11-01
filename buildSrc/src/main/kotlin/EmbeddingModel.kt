import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider

/**
 * The multilingual embedding model Notekea bundles (`ROADMAP.md` 2.2, `docs/spikes/2026-11-01-embedding-models.md`):
 * granite-embedding-107m-multilingual (IBM, Apache 2.0) in bartowski's Q8_0 conversion, 121 MB.
 */
object EmbeddingModel {
    const val FILE_NAME = "granite-embedding-107m-multilingual-Q8_0.gguf"
    const val URL = "https://huggingface.co/bartowski/granite-embedding-107m-multilingual-GGUF/resolve/" +
        "52fed1c818636b7cb8c4e4e3ae73189efeb78081/$FILE_NAME"
    const val SHA_256 = "15691dfe830257f569a1e3521977351d16eac18e48e414908dc98ed9b8455e07"

    /** Where the app finds it, inside its resources (`compose.application.resources.dir`). */
    const val RESOURCE_FOLDER = "models"

    /** The system property that tells the tests of the real model where it was downloaded. */
    const val TEST_PROPERTY = "notekea.embedding.model"

    /** The app resources' folder for every system, as Compose Desktop reads `appResourcesRootDir`. */
    const val COMMON_RESOURCES = "common"

    /**
     * Registers in [project] (composeApp) the task that downloads the model, once for the whole build, into
     * `.gradle/models` at the root: it survives `clean`, and CI caches that folder.
     */
    fun registerDownload(project: Project): TaskProvider<DownloadVerifiedFileTask> =
        project.tasks.register(TASK, DownloadVerifiedFileTask::class.java) {
            description = "Downloads the bundled embedding model and checks its SHA-256"
            url.set(URL)
            sha256.set(SHA_256)
            file.set(file(project))
        }

    /** Where the model is downloaded. */
    fun file(project: Project): RegularFile = project.rootProject.layout.projectDirectory.file(".gradle/models/$FILE_NAME")

    /** A task that puts the model into [project]'s app resources, the folder `appResourcesRootDir` points to. */
    fun appResources(project: Project): TaskProvider<Sync> =
        project.tasks.register("embeddingModelResources", Sync::class.java) {
            dependsOn(DOWNLOAD_TASK_PATH)
            from(file(project))
            into(project.layout.buildDirectory.dir("app-resources/$COMMON_RESOURCES/$RESOURCE_FOLDER"))
        }

    private const val DOWNLOAD_TASK_PATH = ":composeApp:downloadEmbeddingModel"
    private const val TASK = "downloadEmbeddingModel"
}
