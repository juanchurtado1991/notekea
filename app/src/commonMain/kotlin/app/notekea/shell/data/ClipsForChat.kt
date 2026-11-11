package app.notekea.shell.data

import app.notekea.chat.domain.NoteDraft
import app.notekea.chat.domain.PageProblem
import app.notekea.chat.domain.WebPageClipper
import app.notekea.clips.domain.ClipError
import app.notekea.clips.domain.ClipStep
import app.notekea.clips.domain.ClipWebPageUseCase
import app.notekea.clips.domain.WebClipNote
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.LocalCalendar
import kotlin.time.Clock

/** The chat's [WebPageClipper], made of the clips feature (4.4): the page clipped into the note it becomes. */
class ClipsForChat(
    private val clipWebPage: ClipWebPageUseCase,
    private val localCalendar: LocalCalendar,
    private val clock: Clock
) : WebPageClipper {
    override suspend fun clip(url: String): Outcome<NoteDraft, PageProblem> {
        var last: ClipStep = ClipStep.Fetching
        clipWebPage(url = url).collect { last = it }

        return when (val step = last) {
            is ClipStep.Done -> Outcome.Success(
                value = NoteDraft(
                    path = WebClipNote.pathOf(title = step.clip.title),
                    text = WebClipNote.textOf(
                        clip = step.clip,
                        today = today()
                    )
                )
            )

            is ClipStep.Failed -> Outcome.Failure(error = problemOf(error = step.error))

            else -> Outcome.Failure(error = PageProblem.REFUSED)
        }
    }

    private fun today(): String {
        val date = localCalendar.momentOf(instant = clock.now()).date

        return "${date.year}-${date.month.toString().padStart(2, '0')}-${date.day.toString().padStart(2, '0')}"
    }

    private fun problemOf(error: ClipError): PageProblem = when (error) {
        ClipError.BadAddress -> PageProblem.BAD_ADDRESS
        ClipError.LocalAddress -> PageProblem.LOCAL_ADDRESS
        ClipError.Offline -> PageProblem.OFFLINE
        ClipError.TooSlow -> PageProblem.TOO_SLOW
        ClipError.TooLarge -> PageProblem.TOO_LARGE
        is ClipError.Refused -> PageProblem.REFUSED
        ClipError.NotArticle -> PageProblem.NOT_ARTICLE
    }
}
