package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_ID
import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_MAINTAINER
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CONDITION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUNCTION_LITERAL
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IF
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IS_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.NULL
import com.pinterest.ktlint.rule.engine.core.api.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PARENTHESIZED
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT_NAME
import com.pinterest.ktlint.rule.engine.core.api.ElementType.WHEN
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Lint-only rule: a compound expression gets a name before it is used. A condition joined with `&&` or `||` that
 * makes two or more calls, or is over [MAX_CONDITION_LENGTH] characters and checks no type or `null` (the compiler
 * needs those in place), goes to a `val` of its own. So does an `if` or a `when` written as a named argument, unless
 * it fits on one line, calls nothing and nothing is called on it (`color = if (isSelected) primary else outline`).
 */
class NameCompoundExpressionRule :
    Rule(
        ruleId = RuleId(RULE_ID),
        about = About(maintainer = RULE_SET_MAINTAINER)
    ),
    RuleAutocorrectApproveHandler {
    private val castChecks = setOf(
        IS_EXPRESSION,
        NULL
    )
    private val whitespace = Regex(pattern = "\\s+")

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (
            offset: Int,
            errorMessage: String,
            canBeAutoCorrected: Boolean
        ) -> AutocorrectDecision
    ) {
        when (node.elementType) {
            CONDITION -> visitCondition(condition = node, emit = emit)
            VALUE_ARGUMENT -> visitArgument(argument = node, emit = emit)
        }
    }

    private fun visitCondition(
        condition: ASTNode,
        emit: (
            offset: Int,
            errorMessage: String,
            canBeAutoCorrected: Boolean
        ) -> AutocorrectDecision
    ) {
        val expression = condition.codeChildren().singleOrNull() ?: return
        val length = expression.text.replace(regex = whitespace, replacement = " ").length
        val operands = operandsOf(expression = expression)
        val isLong = (length > MAX_CONDITION_LENGTH && operands.none(::checksTypeOrNull)) ||
            operands.count(::hasCall) >= MIN_CALLS
        if (expression.isLogicalExpression() && isLong) {
            emit(expression.startOffset, CONDITION_MESSAGE, false)
        }
    }

    private fun visitArgument(
        argument: ASTNode,
        emit: (
            offset: Int,
            errorMessage: String,
            canBeAutoCorrected: Boolean
        ) -> AutocorrectDecision
    ) {
        if (argument.findChildByType(VALUE_ARGUMENT_NAME) == null) {
            return
        }
        val expression = argument.codeChildren().last()
        val branch = unwrapped(expression = chainRoot(expression = expression))
        val isBranch = branch.elementType == IF || branch.elementType == WHEN
        if (isBranch &&
            (branch != expression || branch.textContains(NEWLINE) || hasCall(operand = branch))
        ) {
            emit(expression.startOffset, BRANCH_MESSAGE, false)
        }
    }

    /** The operands of a logical expression, those of the logical expressions it is made of included. */
    private fun operandsOf(expression: ASTNode): List<ASTNode> = expression
        .codeChildren()
        .filter { it.elementType != OPERATION_REFERENCE }
        .flatMap { if (it.isLogicalExpression()) operandsOf(expression = it) else listOf(it) }

    /** Whether the operand calls something outside a lambda. */
    private fun hasCall(operand: ASTNode): Boolean = when (operand.elementType) {
        CALL_EXPRESSION -> true
        FUNCTION_LITERAL -> false
        else -> operand.getChildren(null).any(::hasCall)
    }

    private fun checksTypeOrNull(operand: ASTNode): Boolean = operand.elementType in castChecks ||
        operand.getChildren(null).any(::checksTypeOrNull)

    /** What a chain starts at; the expression itself when it is not a chain. */
    private fun chainRoot(expression: ASTNode): ASTNode {
        val isChain = expression.elementType in chainTypes

        return if (isChain) {
            chainRoot(
                expression = expression.codeChildren().first()
            )
        } else {
            expression
        }
    }

    private fun unwrapped(expression: ASTNode): ASTNode {
        val inner = expression.takeIf {
            it.elementType == PARENTHESIZED
        }?.codeChildren()?.getOrNull(index = 1)

        return if (inner == null) expression else unwrapped(expression = inner)
    }

    companion object {
        internal const val BRANCH_MESSAGE = "Give the 'if' or 'when' a name: put it in a 'val' before the call"
        internal const val CONDITION_MESSAGE = "Give the condition a name: put it in a 'val' before it is tested"
        internal const val MAX_CONDITION_LENGTH = 40
        private const val MIN_CALLS = 2
        private const val NEWLINE = '\n'
        private const val RULE_ID = "$RULE_SET_ID:name-compound-expression"
    }
}
