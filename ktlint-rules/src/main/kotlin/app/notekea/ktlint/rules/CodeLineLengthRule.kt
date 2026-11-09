package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_ID
import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_MAINTAINER
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.EOL_COMMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FILE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IMPORT_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.KDOC
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.STRING_TEMPLATE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfig
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Lint-only rule: a line of code fits in `max_line_length`. What cannot be broken without hurting it may pass the
 * limit: a string, a test's name in backticks, an import; a comment has up to [MAX_COMMENT_LENGTH] characters.
 */
class CodeLineLengthRule :
    Rule(
        ruleId = RuleId(RULE_ID),
        about = About(maintainer = RULE_SET_MAINTAINER),
        usesEditorConfigProperties = setOf(MAX_LINE_LENGTH_PROPERTY)
    ),
    RuleAutocorrectApproveHandler {
    private val comments = setOf(
        KDOC,
        EOL_COMMENT,
        BLOCK_COMMENT
    )
    private val unbreakable = setOf(
        STRING_TEMPLATE,
        IMPORT_DIRECTIVE,
        PACKAGE_DIRECTIVE
    )
    private var maxLineLength = Int.MAX_VALUE

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        maxLineLength = editorConfig[MAX_LINE_LENGTH_PROPERTY]
    }

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (
            offset: Int,
            errorMessage: String,
            canBeAutoCorrected: Boolean
        ) -> AutocorrectDecision
    ) {
        if (node.elementType != FILE) return
        var lineStart = 0

        node.text.lineSequence().forEach { line ->
            val limit = limitOf(
                file = node,
                lineStart = lineStart,
                line = line
            )

            if (line.length > limit) emit(lineStart + limit, errorMessage(limit = limit), false)
            lineStart += line.length + 1
        }
    }

    /** The most characters the line may have, going by what stands where the code's limit falls. */
    private fun limitOf(
        file: ASTNode,
        lineStart: Int,
        line: String
    ): Int {
        if (line.length <= maxLineLength) return maxLineLength
        val atLimit = file.findLeafElementAt(lineStart + maxLineLength) ?: return maxLineLength

        // What only closes or joins the line (`() =`, `" +`, `",`) is judged by what stands before it.
        val leaf = generateSequence(seed = atLimit) { it.leafBefore() }.first {
            it.startOffset < lineStart || !it.text.all(TAIL::contains)
        }

        val around = generateSequence(seed = leaf) { it.treeParent }.map { it.elementType }
        val isTestName = leaf.elementType == IDENTIFIER && leaf.text.startsWith(prefix = "`")

        return when {
            isTestName || around.any { it in unbreakable } -> Int.MAX_VALUE
            around.any { it in comments } -> MAX_COMMENT_LENGTH
            else -> maxLineLength
        }
    }

    private fun ASTNode.leafBefore(): ASTNode? = generateSequence(seed = this) { it.treeParent }
        .firstNotNullOfOrNull { it.treePrev }
        ?.lastLeaf()

    private fun ASTNode.lastLeaf(): ASTNode = lastChildNode?.lastLeaf() ?: this

    companion object {
        internal const val MAX_COMMENT_LENGTH = 120
        private const val TAIL = " \t(){}=+,"
        private const val RULE_ID = "$RULE_SET_ID:code-line-length"

        internal fun errorMessage(limit: Int): String = "Line is longer than $limit characters"
    }
}
