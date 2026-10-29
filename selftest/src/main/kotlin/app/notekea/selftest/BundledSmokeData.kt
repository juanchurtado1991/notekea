package app.notekea.selftest

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * The smoke data the build bundles into the Self-Test (`smoke-data/` of the repository: the vaults and the files the
 * scenarios copy), written out to a folder. Its `index.txt` lists the files, since a jar cannot list a folder.
 */
object BundledSmokeData {
    private const val ROOT = "/smoke-data/"
    private const val INDEX = "index.txt"

    fun extract(target: Path): Path {
        index().forEach { relative ->
            val file = target.resolve(relative)
            Files.createDirectories(file.parent)
            resource(path = relative).use { Files.copy(it, file, StandardCopyOption.REPLACE_EXISTING) }
        }
        return target
    }

    private fun index(): List<String> = resource(path = INDEX).bufferedReader().readLines().filter(String::isNotBlank)

    private fun resource(path: String) =
        requireNotNull(BundledSmokeData::class.java.getResourceAsStream(ROOT + path)) { "The smoke data lacks $path" }
}
