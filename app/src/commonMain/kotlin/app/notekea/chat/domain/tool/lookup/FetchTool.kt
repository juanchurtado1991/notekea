package app.notekea.chat.domain.tool.lookup

import app.notekea.chat.domain.plan.PageReader
import app.notekea.chat.domain.plan.UrlsNamed
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolOrigin
import app.notekea.chat.domain.tool.ToolResult
import app.notekea.core.domain.Outcome

/**
 * A web page's article, only from an address the person's request writes whole (`GUIDELINES.md` §13): a note or a
 * page read on the way cannot have the chat fetch an address of its own. What comes back is someone else's text.
 */
class FetchTool(private val pageReader: PageReader) : ChatTool {
    override val name = NAME

    override val description =
        "a web page whose address the task writes; you get its article. Never another address."

    override val arguments = listOf(ToolArgument(name = URL))
    override val effect = ToolEffect.READS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = call.text(name = URL)

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val url = call.text(name = URL)

        // Only an address the task writes whole: not a piece of one, such as its site's root.
        val isWritten = url in UrlsNamed.writtenIn(text = context.task)

        if (!isWritten) {
            return ToolResult(forModel = "fetch \"$url\": refused, the task does not write this address.")
        }

        return when (val page = pageReader.read(url = url)) {
            is Outcome.Success -> ToolResult(
                forModel = "fetch \"$url\":\n${page.value.take(n = PAGE_CHARS)}",
                origin = ToolOrigin.UNTRUSTED
            )

            is Outcome.Failure -> ToolResult(
                forModel = "fetch \"$url\": could not be read (${page.error.name.lowercase()})."
            )
        }
    }

    companion object {
        const val NAME = "fetch"
        const val URL = "url"
        private const val PAGE_CHARS = 3_000
    }
}
