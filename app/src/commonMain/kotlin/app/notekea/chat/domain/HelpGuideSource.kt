package app.notekea.chat.domain

/** Where the app's guide comes from: the file shipped with the app. */
fun interface HelpGuideSource {
    suspend fun load(): HelpGuide
}
