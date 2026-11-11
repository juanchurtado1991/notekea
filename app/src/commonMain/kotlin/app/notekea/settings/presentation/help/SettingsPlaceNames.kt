package app.notekea.settings.presentation.help

import app.notekea.core.settings.domain.SettingsPlace
import app.notekea.resources.Res
import app.notekea.resources.appearance_mode
import app.notekea.resources.appearance_reduce_motion
import app.notekea.resources.appearance_theme
import app.notekea.resources.models_other_sizes
import app.notekea.resources.settings_place_model_size
import app.notekea.resources.settings_place_models_folder
import app.notekea.resources.sync_auto
import app.notekea.resources.sync_connect
import app.notekea.resources.sync_interval
import app.notekea.resources.sync_token_label
import app.notekea.resources.sync_url_label
import org.jetbrains.compose.resources.StringResource

/** A setting's name on Show, as its screen labels it ("Access token"). */
internal fun nameOf(place: SettingsPlace): StringResource = when (place) {
    SettingsPlace.APPEARANCE_MODE -> Res.string.appearance_mode
    SettingsPlace.APPEARANCE_REDUCE_MOTION -> Res.string.appearance_reduce_motion
    SettingsPlace.APPEARANCE_THEME -> Res.string.appearance_theme
    SettingsPlace.AI_MODEL_SIZE -> Res.string.settings_place_model_size
    SettingsPlace.AI_MODEL_OTHER_SIZES -> Res.string.models_other_sizes
    SettingsPlace.AI_MODEL_FOLDER -> Res.string.settings_place_models_folder
    SettingsPlace.SYNC_ADDRESS -> Res.string.sync_url_label
    SettingsPlace.SYNC_TOKEN -> Res.string.sync_token_label
    SettingsPlace.SYNC_AUTO -> Res.string.sync_auto
    SettingsPlace.SYNC_INTERVAL -> Res.string.sync_interval
    SettingsPlace.SYNC_CONNECT -> Res.string.sync_connect
}
