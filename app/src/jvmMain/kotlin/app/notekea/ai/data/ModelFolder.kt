package app.notekea.ai.data

import app.notekea.ai.domain.ModelFile
import java.nio.file.Path

/** Where each model file lives in the models folder [root]: under its own name once verified, apart until then. */
class ModelFolder(val root: Path) {
    fun installed(file: ModelFile): Path = root.resolve(file.fileName)

    fun partial(file: ModelFile): Path = root.resolve(file.fileName + PARTIAL_SUFFIX)

    private companion object {
        const val PARTIAL_SUFFIX = ".part"
    }
}
