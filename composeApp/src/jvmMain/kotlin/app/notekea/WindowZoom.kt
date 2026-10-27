package app.notekea

import app.notekea.core.appearance.domain.GetUiScaleUseCase
import app.notekea.core.appearance.domain.SaveUiScaleUseCase
import app.notekea.core.platform.domain.PlatformConventions

/** What the main window needs for its zoom: the [platformConventions]' shortcuts and where the zoom is kept. */
class WindowZoom(
    val platformConventions: PlatformConventions,
    val getUiScale: GetUiScaleUseCase,
    val saveUiScale: SaveUiScaleUseCase
)
