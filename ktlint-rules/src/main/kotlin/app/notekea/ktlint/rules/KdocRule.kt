package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_ID
import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_MAINTAINER
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.KDOC
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Lint-only rule: a KDoc is short and says what the code does. It has at most [MAX_LINES] lines of text, is not
 * empty, does not only repeat the name it documents (nor a tag its parameter) and does not cite the past.
 */
class KdocRule :
    Rule(
        ruleId = RuleId(RULE_ID),
        about = About(maintainer = RULE_SET_MAINTAINER)
    ),
    RuleAutocorrectApproveHandler {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (
            offset: Int,
            errorMessage: String,
            canBeAutoCorrected: Boolean
        ) -> AutocorrectDecision
    ) {
        if (node.elementType != KDOC) {
            return
        }
        val lines = textLines(kdoc = node)
        val text = lines.joinToString(separator = " ")
        val name = node.treeParent?.findChildByType(IDENTIFIER)?.text
        val message = when {
            lines.isEmpty() -> EMPTY_MESSAGE
            lines.size > MAX_LINES -> longMessage(lines = lines.size)
            name != null && wordsOf(text = text) == wordsOf(text = name) -> NAME_MESSAGE
            lines.any(::isEmptyTag) -> TAG_MESSAGE
            pastCitation.containsMatchIn(input = text) -> PAST_MESSAGE
            else -> return
        }
        emit(node.startOffset, message, false)
    }

    /** The lines of the KDoc that say something, without the comment's own marks. */
    private fun textLines(kdoc: ASTNode): List<String> = kdoc
        .text
        .removePrefix(prefix = OPENING)
        .removeSuffix(suffix = CLOSING)
        .lines()
        .map { line -> line.trim().removePrefix(prefix = LINE_MARK).trim() }
        .filter { it.isNotEmpty() }

    /** Whether the line is a `@param` or `@return` that says nothing but the name. */
    private fun isEmptyTag(line: String): Boolean {
        val tag = tagLine.matchEntire(input = line) ?: return false
        val name = tag.groupValues[1] + tag.groupValues[2]
        val told = wordsOf(text = tag.groupValues[3])

        return told.isEmpty() || told == wordsOf(text = name)
    }

    /** The words of a sentence or of a name in camel case, in lower case, without articles. */
    private fun wordsOf(text: String): List<String> = wordBreak
        .replace(input = text, replacement = " ")
        .lowercase()
        .split(notLetters)
        .filter { it.isNotEmpty() && it !in articles }

    companion object {
        internal const val EMPTY_MESSAGE = "An empty KDoc says nothing: write it or remove it"
        internal const val MAX_LINES = 3
        internal const val NAME_MESSAGE = "The KDoc only repeats the name: say what the name does not, or remove it"
        internal const val PAST_MESSAGE = "A KDoc says what the code does, not its history (roadmap, smoke, issue)"
        internal const val TAG_MESSAGE = "The tag only repeats the name: say what the name does not, or remove it"
        private const val CLOSING = "*/"
        private const val LINE_MARK = "*"
        private const val OPENING = "/**"
        private const val RULE_ID = "$RULE_SET_ID:kdoc"
        private val articles = setOf("a", "an", "the")
        private val notLetters = Regex(pattern = "[^a-z0-9]+")
        private val pastCitation = Regex(
            pattern = """ROADMAP|\bsmoke (finding|request)|\d+\.\d+(-\d+)? smoke\b|\b(issue|PR|pull request) #\d+""",
            option = RegexOption.IGNORE_CASE
        )
        private val tagLine = Regex(pattern = """@(?:(?:param|property)\s+(\w+)|return())\b(.*)""")
        private val wordBreak = Regex(pattern = "(?<=[a-z0-9])(?=[A-Z])")

        internal fun longMessage(lines: Int): String =
            "The KDoc has $lines lines of text, the maximum is $MAX_LINES: keep what the code does not say"
    }
}
