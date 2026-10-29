package app.notekea.selftest

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively

/**
 * The test data every case gets a fresh copy of: the vaults of `smoke-data/vaults` (Personal Notes, Work Obsidian,
 * the Ghost Serializer vault…) next to what [SmokeScenarios.prepare] builds (the Playground Vault, the Daily Vault…),
 * all in one folder. It is built once per process, from the data bundled in the Self-Test.
 */
@OptIn(ExperimentalPathApi::class)
object SmokeData {
    private const val TEMP_PREFIX = "notekea-self-test"

    val folder: Path by lazy { build() }

    private fun build(): Path {
        val root = Files.createTempDirectory(TEMP_PREFIX)
        val bundle = BundledSmokeData.extract(target = root.resolve("bundle"))
        val data = root.resolve("data")
        SmokeScenarios(smokeData = bundle, scratch = data).prepare()
        Files.list(bundle.resolve("vaults")).use { vaults ->
            vaults.forEach {
                it.copyToRecursively(
                    target = data.resolve(it.fileName.toString()),
                    followLinks = false,
                    overwrite = true
                )
            }
        }
        return data
    }
}
