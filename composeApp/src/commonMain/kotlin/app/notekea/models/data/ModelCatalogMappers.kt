package app.notekea.models.data

import app.notekea.models.domain.ChatModel
import app.notekea.models.domain.ModelCatalog
import app.notekea.models.domain.ModelFile
import app.notekea.models.domain.ModelSize

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
