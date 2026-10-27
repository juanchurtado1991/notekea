package app.notekea.core.appearance.domain

/** Remembers the app's zoom, within [UiScale]'s limits. */
class SaveUiScaleUseCase(private val uiScaleStore: UiScaleStore) {
    suspend operator fun invoke(
        scale: Float
    ) = uiScaleStore.setScale(scale = UiScale.clamp(scale = scale))
}
