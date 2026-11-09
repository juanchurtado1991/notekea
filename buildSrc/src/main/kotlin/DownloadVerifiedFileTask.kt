import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

/**
 * Downloads [url] to [file] once and checks it is the file it must be: its SHA-256 is [sha256]. A file already there
 * with that hash is kept (it is never downloaded again), one with another hash is replaced, and a download that does
 * not match or breaks halfway fails the build and leaves nothing behind. A server that stops answering fails it after
 * [timeoutMillis], never hangs it. Used for the embedding model the app bundles.
 */
abstract class DownloadVerifiedFileTask : DefaultTask() {
    @get:Input
    abstract val url: Property<String>

    @get:Input
    abstract val sha256: Property<String>

    @get:OutputFile
    abstract val file: RegularFileProperty

    /** How long connecting, and then each read, may wait for the server. */
    @get:Internal
    abstract val timeoutMillis: Property<Int>

    init {
        timeoutMillis.convention(DEFAULT_TIMEOUT_MILLIS)
    }

    @TaskAction
    fun download() {
        val target = file.get().asFile
        if (target.isFile && sha256Of(file = target) == sha256.get()) return
        target.parentFile.mkdirs()
        val partial = File(target.parentFile, target.name + PARTIAL_SUFFIX)
        try {
            fetch(partial = partial)
            val actual = sha256Of(file = partial)
            if (actual != sha256.get()) {
                error("${url.get()} has SHA-256 $actual, not ${sha256.get()}")
            }
            Files.move(partial.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } finally {
            partial.delete()
        }
    }

    private fun fetch(partial: File) {
        val connection = URI(url.get()).toURL().openConnection()
        connection.connectTimeout = timeoutMillis.get()
        connection.readTimeout = timeoutMillis.get()
        connection.getInputStream().use { input -> partial.outputStream().use { input.copyTo(it) } }
    }

    private fun sha256Of(file: File): String {
        val digest = MessageDigest.getInstance(SHA_256)
        file.inputStream().use { input ->
            val buffer = ByteArray(BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString(separator = "") { "%02x".format(it) }
    }

    private companion object {
        const val BUFFER_SIZE = 1 shl 16
        const val DEFAULT_TIMEOUT_MILLIS = 60_000
        const val PARTIAL_SUFFIX = ".part"
        const val SHA_256 = "SHA-256"
    }
}
