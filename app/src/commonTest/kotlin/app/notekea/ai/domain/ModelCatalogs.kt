package app.notekea.ai.domain

/** A catalog shaped like the bundled one, with small files so tests can count their bytes. */
object ModelCatalogs {
    val reranker = ModelFile(
        fileName = "reranker.gguf",
        url = "https://models.test/reranker.gguf",
        sizeBytes = 40,
        sha256 = "r"
    )
    val small = model(
        size = ModelSize.SMALL,
        memoryGb = 8,
        withGpuGb = 8,
        videoGb = 2,
        bytes = 100
    )
    val medium = model(
        size = ModelSize.MEDIUM,
        memoryGb = 16,
        withGpuGb = 8,
        videoGb = 4,
        bytes = 200
    )
    val large = model(
        size = ModelSize.LARGE,
        memoryGb = 32,
        withGpuGb = 16,
        videoGb = 8,
        bytes = 300
    )
    val catalog = ModelCatalog(
        models = listOf(small, medium, large),
        reranker = reranker
    )

    private fun model(
        size: ModelSize,
        memoryGb: Int,
        withGpuGb: Int,
        videoGb: Int,
        bytes: Long
    ) = ChatModel(
        size = size,
        name = "Model ${size.id}",
        file = ModelFile(
            fileName = "${size.id}.gguf",
            url = "https://models.test/${size.id}.gguf",
            sizeBytes = bytes,
            sha256 = size.id
        ),
        minimumMemoryGb = memoryGb,
        minimumMemoryWithGpuGb = withGpuGb,
        minimumVideoMemoryGb = videoGb
    )
}
