package app.notekea.core.appearance.domain

/** Where the app's zoom ([UiScale]) is kept between runs. */
interface UiScaleStore {
    suspend fun scale(): Float

    suspend fun setScale(
        scale: Float
    )
}
