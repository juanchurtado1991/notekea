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
    GENERAL_LANGUAGE(
        topic = SettingsTopic.GENERAL,
        words = listOf("language", "idioma", "english", "inglés", "spanish", "español", "lenguaje")
    ),
    GENERAL_OPEN_LAST_VAULT(
        topic = SettingsTopic.GENERAL,
        words = listOf("last vault", "at startup", "when it starts", "último vault", "al iniciar", "al abrir")
    ),
    GENERAL_SHOW_WELCOME(
        topic = SettingsTopic.GENERAL,
        words = listOf("welcome", "onboarding", "bienvenida", "tutorial", "first run", "primeros pasos")
    ),
    EDITOR_TEXT_SIZE(
        topic = SettingsTopic.EDITOR,
        words = listOf("text size", "font size", "bigger text", "smaller text", "tamaño de letra", "letra", "fuente")
    ),
    EDITOR_LINE_WIDTH(
        topic = SettingsTopic.EDITOR,
        words = listOf("line width", "wide", "narrow", "ancho de línea", "ancho", "angosto", "columna")
    ),
    FILES_ATTACHMENTS(
        topic = SettingsTopic.FILES,
        words = listOf("attachments folder", "attachments", "adjuntos", "carpeta de adjuntos", "images folder")
    ),
    FILES_DAILY_FOLDER(
        topic = SettingsTopic.FILES,
        words = listOf("daily notes folder", "daily notes", "notas diarias", "carpeta de notas diarias", "journal")
    ),
    FILES_DAILY_FORMAT(
        topic = SettingsTopic.FILES,
        words = listOf("date format", "formato de fecha", "yyyy", "fecha de la nota")
    ),
    FILES_TEMPLATES(
        topic = SettingsTopic.FILES,
        words = listOf("templates folder", "templates", "plantillas", "carpeta de plantillas")
    ),
    SHORTCUTS_LIST(
        topic = SettingsTopic.SHORTCUTS,
        words = listOf("shortcuts", "keyboard shortcuts", "atajos", "atajos de teclado", "teclas")
    ),
    ABOUT_VERSION(
        topic = SettingsTopic.ABOUT,
        words = listOf("version", "versión", "which version", "qué versión", "update", "actualizar")
    ),
    ABOUT_DIAGNOSTICS(
        topic = SettingsTopic.ABOUT,
        words = listOf("diagnostics", "diagnóstico", "report a bug", "reportar un error", "logs", "registros")
    ),
    ABOUT_PRIVACY(
        topic = SettingsTopic.ABOUT,
        words = listOf("privacy", "privacidad", "my data", "mis datos", "telemetry", "telemetría", "licenses", "licencias")
    ),
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
