package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_ID
import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_MAINTAINER
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BLOCK
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LBRACE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.RBRACE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.RETURN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.WHITE_SPACE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.ifAutocorrectAllowed
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * A blank line separates a statement that spans several lines from its neighbours, and a `return` from the
 * statements before it. Statements of one line stay together unless the author separates them.
 */
class StatementSpacingRule :
    Rule(
        ruleId = RuleId(RULE_ID),
        about = About(maintainer = RULE_SET_MAINTAINER)
    ),
    RuleAutocorrectApproveHandler {
    private val braces = setOf(
        LBRACE,
        RBRACE
    )

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (
            offset: Int,
            errorMessage: String,
            canBeAutoCorrected: Boolean
        ) -> AutocorrectDecision
    ) {
        if (node.elementType != BLOCK) {
            return
        }
        val statements = node.codeChildren().filterNot { it.elementType in braces }
        statements.zipWithNext().forEach { (previous, next) ->
            val needsBlankLine = next.elementType == RETURN ||
                previous.text.contains(other = LINE_BREAK) ||
                next.text.contains(other = LINE_BREAK)
            val gap = gapBefore(statement = next, previous = previous)
            if (needsBlankLine && gap != null &&
                gap.text.count { it == LINE_BREAK_CHAR } < BLANK_LINE_BREAKS
            ) {
                emit(next.startOffset, ERROR_MESSAGE, true).ifAutocorrectAllowed {
                    (gap as LeafPsiElement).rawReplaceWithText(LINE_BREAK + gap.text)
                }
            }
        }
    }

    /**
     * The line break that separates [statement], with the comments written above it, from [previous]; null when
     * a blank line already stands somewhere between them or they share a line.
     */
    private fun gapBefore(statement: ASTNode, previous: ASTNode): ASTNode? {
        val between = generateSequence(seed = previous.treeNext) { it.treeNext }.takeWhile {
            it != statement
        }.toList()
        val breaks = between.filter { it.elementType == WHITE_SPACE && it.isLineBreak() }
        val hasBlankLine = breaks.any { gap ->
            gap.text.count { it == LINE_BREAK_CHAR } >= BLANK_LINE_BREAKS
        }
        return breaks.firstOrNull().takeIf { !hasBlankLine }
    }

    companion object {
        private const val BLANK_LINE_BREAKS = 2
        private const val ERROR_MESSAGE = "A blank line goes before a 'return' and around a statement of several lines"
        private const val LINE_BREAK_CHAR = '\n'
        private const val RULE_ID = "$RULE_SET_ID:statement-spacing"
    }
}
