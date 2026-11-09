package app.notekea.models.domain

/**
 * Picks the chat model for this machine (`ROADMAP.md` 3.1): the largest of the catalog that [ChatModel.fits] its
 * memory or its GPU, and the smallest when none does, so the person has nothing technical to choose.
 */
class RecommendModelUseCase {
    operator fun invoke(
        catalog: ModelCatalog,
        hardware: Hardware
    ): ChatModel = catalog.models.lastOrNull { it.fits(hardware = hardware) } ?: catalog.models.first()
}
