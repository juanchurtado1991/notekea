package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_ID
import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_MAINTAINER
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.WHITE_SPACE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.ifAutocorrectAllowed
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * An expression of three or more operands joined by `&&` or `||` puts one operand per line, the operator at the
 * end of the line. An expression inside a string stays as it is.
 */
class LogicalOperandsPerLineRule :
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
        val isTopExpression =
            node.isLogicalExpression() && node.treeParent?.isLogicalExpression() != true
        if (!isTopExpression || node.isInsideString()) {
            return
        }
        val operators = node.logicalOperators()
        if (operators.size + 1 < MIN_LOGICAL_OPERANDS) {
            return
        }
        val indent = node.lineIndent() + INDENT
        operators
            .mapNotNull { operator ->
                operator.treeParent.codeChildren().last().takeIf { !it.startsLine() }
            }
            .filter { it.firstLeaf().previousLeaf()?.let(::isCodeOrSpace) == true }
            .forEach { operand ->
                emit(operand.startOffset, ERROR_MESSAGE, true).ifAutocorrectAllowed {
                    operand.moveToNewLine(indent = indent)
                }
            }
    }

    /** False after a comment, which keeps the operand where it is. */
    private fun isCodeOrSpace(leaf: ASTNode): Boolean {
        val before = if (leaf.elementType == WHITE_SPACE) leaf.previousLeaf() else leaf
        return before?.elementType !in commentTypes
    }

    companion object {
        private const val ERROR_MESSAGE = "Each operand of three or more joined by '&&' or '||' goes on its own line"
        private const val RULE_ID = "$RULE_SET_ID:logical-operands-per-line"
    }
}
