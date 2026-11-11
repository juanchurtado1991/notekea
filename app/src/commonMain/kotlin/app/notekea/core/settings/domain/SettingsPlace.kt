package app.notekea.core.settings.domain

/**
 * Every setting a person can be shown, as an Android intent names a screen: its section ([topic]) and the [words]
 * people use for it, in English and Spanish. A new setting gets its place here and its spotlight on screen
 * (`GUIDELINES.md`, Settings), which `SettingsPlacesTest` checks.
 */
enum class SettingsPlace(
    val topic: SettingsTopic,
    val words: List<String>
) {
    APPEARANCE_MODE(
        topic = SettingsTopic.APPEARANCE,
        words = listOf("mode", "dark mode", "light mode", "modo", "modo oscuro", "modo claro", "oscuro", "claro")
    ),
    APPEARANCE_REDUCE_MOTION(
        topic = SettingsTopic.APPEARANCE,
        words = listOf("reduce motion", "motion", "animations", "animaciones", "movimiento", "reducir movimiento")
    ),
    APPEARANCE_THEME(
        topic = SettingsTopic.APPEARANCE,
        words = listOf("theme", "themes", "color theme", "colors", "tema", "temas", "colores")
    ),
    AI_MODEL_SIZE(
        topic = SettingsTopic.AI_MODEL,
        words = listOf("download the model", "download", "recommended", "descargar", "descargo", "recomendado")
    ),
    AI_MODEL_OTHER_SIZES(
        topic = SettingsTopic.AI_MODEL,
        words = listOf("other sizes", "small", "large", "otros tamaños", "pequeño", "grande", "tamaños")
    ),
    AI_MODEL_FOLDER(
        topic = SettingsTopic.AI_MODEL,
        words = listOf("models folder", "where are the models", "carpeta de modelos", "dónde se guardan")
    ),
    SYNC_ADDRESS(
        topic = SettingsTopic.SYNC,
        words = listOf("repository address", "address", "https address", "url", "dirección", "direccion")
    ),
    SYNC_TOKEN(
        topic = SettingsTopic.SYNC,
        words = listOf("token", "access token", "personal access token", "contraseña", "password", "llave")
    ),
    SYNC_AUTO(
        topic = SettingsTopic.SYNC,
        words = listOf("sync by itself", "automatically", "automatic sync", "automático", "automáticamente")
    ),
    SYNC_INTERVAL(
        topic = SettingsTopic.SYNC,
        words = listOf("look for changes", "how often", "minutes", "cada cuánto", "minutos", "intervalo")
    ),
    SYNC_CONNECT(
        topic = SettingsTopic.SYNC,
        words = listOf("connect", "sync now", "stop syncing", "conectar", "conecto", "sincronizar ahora")
    )
}
