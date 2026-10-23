package app.notekea.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BLOCK
import com.pinterest.ktlint.rule.engine.core.api.ElementType.EQ
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

internal const val MAX_FUNCTION_BODY_LINES = 40

private const val NEWLINE = '\n'
private const val RULE_ID = "$RULE_SET_ID:max-function-length"

/**
 * Lint-only rule: the body of a function (from `{` to `}`, or from `=` to the end of an expression body)
 * may not exceed [MAX_FUNCTION_BODY_LINES] lines, blank lines and comments included.
 * The signature is not counted because the project puts one parameter per line.
 * Long functions must be split into granular, self-descriptive helpers instead.
 */
class MaxFunctionLengthRule :
    Rule(
        ruleId = RuleId(RULE_ID),
        about = About(maintainer = RULE_SET_MAINTAINER)
    ),
    RuleAutocorrectApproveHandler {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision
    ) {
        if (node.elementType != FUN) {
            return
        }
        val bodyLines = bodyLineCount(function = node) ?: return
        if (bodyLines > MAX_FUNCTION_BODY_LINES) {
            emit(node.startOffset, errorMessage(bodyLines = bodyLines), false)
        }
    }

    private fun bodyLineCount(
        function: ASTNode
    ): Int? {
        val blockBody = function.findChildByType(BLOCK)
        val bodyText = blockBody?.text ?: expressionBodyText(function = function) ?: return null
        return bodyText.count { it == NEWLINE } + 1
    }

    private fun errorMessage(
        bodyLines: Int
    ): String = "Function body has $bodyLines lines, the maximum is $MAX_FUNCTION_BODY_LINES. " +
        "Extract granular, self-descriptive functions"

    private fun expressionBodyText(
        function: ASTNode
    ): String? {
        val assignment = function.findChildByType(EQ) ?: return null
        val functionText = function.text
        val bodyStart = assignment.startOffset - function.startOffset
        return functionText.substring(startIndex = bodyStart)
    }
}
