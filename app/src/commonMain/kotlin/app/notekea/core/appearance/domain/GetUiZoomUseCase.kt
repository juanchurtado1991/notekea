package app.notekea.core.appearance.domain

/** The zoom the app was left at, within [UiZoomLevels]'s limits. */
class GetUiZoomUseCase(private val uiZoomStore: UiZoomStore) {
    suspend operator fun invoke(): Float = UiZoomLevels.clamp(scale = uiZoomStore.scale())
}
