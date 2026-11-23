package app.notekea.chat.domain.tool.reading

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.links.LinkFinder
import app.notekea.core.notes.domain.links.LinkResolver

/**
 * The notes a note links to and the notes that link to it, as following a link and the backlinks panel find them:
 * how the person's notes hang together, for "what is related to…" (`docs/agent-loop.md`, step 4).
 */
class LinksTool(
    private val noteTextCache: NoteTextCache,
    private val linkFinder: LinkFinder
) : ChatTool {
    override val name = NAME

    override val description =
        "a note's name; you get the notes it links to and the notes that link to it."

    override val arguments = listOf(ToolArgument(name = NOTE))
    override val effect = ToolEffect.READS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String {
        val named = call.text(name = NOTE)

        return context.pathOf(name = named) ?: named
    }

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val name = call.text(name = NOTE)

        val path = context.noteNamed(name = name)
            ?: return ToolResult(forModel = "links \"$name\": no note of that name.")

        val notes = context.notes.filter { it.file.path.isMarkdown }
        val resolver = LinkResolver(notePaths = notes.map { it.file.path })

        val linked = noteTextCache
            .textsOf(
                vaultPath = context.vaultPath,
                notes = notes
            )
            .mapValues { (source, text) ->
                linkFinder
                    .find(markdown = text.body)
                    .mapNotNull {
                        resolver.closest(
                            link = it,
                            source = source
                        )
                    }
                    .distinct()
            }

        val to = linked[path].orEmpty().filter {
            it != path
        }

        val from = linked
            .filter { (source, targets) ->
                source != path && path in targets
            }
            .keys
            .sortedBy { it.value }

        return ToolResult(
            forModel = "links \"${path.nameWithoutExtension}\":\n" +
                "It links to: ${namesOf(notes = to)}\nLinked from: ${namesOf(notes = from)}"
        )
    }

    private fun namesOf(notes: List<RelativePath>): String = notes
        .take(n = MAX_NAMES)
        .joinToString { it.bare() }
        .ifEmpty { "no note" }

    companion object {
        const val NAME = "links"
        const val NOTE = "note"
        private const val MAX_NAMES = 40
    }
}
