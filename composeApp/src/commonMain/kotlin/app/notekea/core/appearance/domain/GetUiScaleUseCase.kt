package app.notekea.core.appearance.domain

/** The zoom the app was left at, within [UiScale]'s limits. */
class GetUiScaleUseCase(private val uiScaleStore: UiScaleStore) {
    suspend operator fun invoke(): Float = UiScale.clamp(scale = uiScaleStore.scale())
}
