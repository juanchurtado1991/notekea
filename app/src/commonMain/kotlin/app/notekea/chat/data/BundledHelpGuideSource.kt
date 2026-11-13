package app.notekea.chat.data

import app.notekea.chat.domain.help.HelpGuide
import app.notekea.chat.domain.help.HelpGuideSource
import app.notekea.core.domain.AppDispatchers
import app.notekea.resources.Res
import kotlinx.coroutines.withContext

/** [HelpGuideSource] on `files/guide/guide.md` in the app's resources, read once: the guide ships with the app. */
class BundledHelpGuideSource(private val dispatchers: AppDispatchers) : HelpGuideSource {
    private var guide: HelpGuide? = null

    override suspend fun load(): HelpGuide = guide ?: withContext(context = dispatchers.io) {
        HelpGuide.of(markdown = Res.readBytes(path = GUIDE_PATH).decodeToString())
    }.also { guide = it }

    companion object {
        const val GUIDE_PATH = "files/guide/guide.md"
    }
}
