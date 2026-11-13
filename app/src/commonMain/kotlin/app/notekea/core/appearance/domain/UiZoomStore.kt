package app.notekea.core.appearance.domain

/** Where the app's zoom ([UiZoomLevels]) is kept between runs. */
interface UiZoomStore {
    suspend fun scale(): Float

    suspend fun setScale(scale: Float)
}
