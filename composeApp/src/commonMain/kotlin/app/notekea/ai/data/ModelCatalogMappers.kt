package app.notekea.ai.data

import app.notekea.ai.domain.ChatModel
import app.notekea.ai.domain.ModelCatalog
import app.notekea.ai.domain.ModelFile
import app.notekea.ai.domain.ModelSize

/** The catalog, keeping only the models of a size this version knows, smallest first. */
fun ModelCatalogDto.toDomain(): ModelCatalog = ModelCatalog(
    models = models.mapNotNull { it.toDomain() }.sortedBy { it.size },
    reranker = reranker.toDomain()
)

/** The model, or null when its size is not one this version knows. */
fun ChatModelDto.toDomain(): ChatModel? = ModelSize.fromId(id = size)?.let {
    ChatModel(
        size = it,
        name = name,
        file = file.toDomain(),
        minimumMemoryGb = minimumMemoryGb,
        minimumMemoryWithGpuGb = minimumMemoryWithGpuGb,
        minimumVideoMemoryGb = minimumVideoMemoryGb
    )
}

fun ModelFileDto.toDomain(): ModelFile = ModelFile(
    fileName = fileName,
    url = url,
    sizeBytes = sizeBytes,
    sha256 = sha256.lowercase()
)
