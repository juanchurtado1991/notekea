package app.notekea.chat.domain.tool

/**
 * A tool whose answer hangs on arguments its target leaves out (a tally's figures, a digest's question): what makes
 * a call the same call as one made before is told by [sameAs], not by the target.
 */
interface DistinctCalls {
    fun sameAs(
        call: ToolCall,
        context: ToolContext
    ): String
}
