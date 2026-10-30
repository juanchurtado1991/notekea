package app.notekea.selftest

import app.notekea.core.platform.data.JvmSystemEnvironment
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.ResolvePlatformConventionsUseCase
import java.nio.file.Path

/** Notekea QA's data folder: next to Notekea's own, named after it with " QA", so the two never share settings. */
object QaAppData {
    fun of(conventions: PlatformConventions): Path = Path.of(conventions.appDataDirectory).let {
        it.resolveSibling(it.fileName.toString() + SUFFIX)
    }

    /** This machine's Notekea QA data folder. */
    fun current(): Path = of(
        conventions = ResolvePlatformConventionsUseCase(systemEnvironment = JvmSystemEnvironment())()
    )

    private const val SUFFIX = " QA"
}
