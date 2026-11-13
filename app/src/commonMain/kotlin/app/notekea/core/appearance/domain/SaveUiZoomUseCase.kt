package app.notekea.core.appearance.domain

/** Remembers the app's zoom, within [UiZoomLevels]'s limits. */
class SaveUiZoomUseCase(private val uiZoomStore: UiZoomStore) {
    suspend operator fun invoke(scale: Float) = uiZoomStore.setScale(
        scale = UiZoomLevels.clamp(scale = scale)
    )
}
