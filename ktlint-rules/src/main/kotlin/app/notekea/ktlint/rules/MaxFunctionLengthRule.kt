package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_ID
import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_MAINTAINER
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BLOCK
import com.pinterest.ktlint.rule.engine.core.api.ElementType.EQ
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Lint-only rule: the body of a function (a block, or what follows `=`) may not exceed [MAX_FUNCTION_BODY_LINES]
 * lines of code: comments count, blank lines and lines of brackets alone do not, nor does the signature.
 * A long function is split into granular, self-descriptive helpers.
 */
class MaxFunctionLengthRule :
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
        if (node.elementType != FUN) {
            return
        }
        val body =
            node.findChildByType(BLOCK)?.text ?: expressionBodyText(function = node) ?: return
        val bodyLines = codeLineCount(text = body)
        if (bodyLines > MAX_FUNCTION_BODY_LINES) {
            emit(node.startOffset, errorMessage(bodyLines = bodyLines), false)
        }
    }

    private fun expressionBodyText(function: ASTNode): String? {
        val assignment = function.findChildByType(EQ) ?: return null

        return function.text.substring(startIndex = assignment.startOffset - function.startOffset)
    }

    companion object {
        internal const val MAX_FUNCTION_BODY_LINES = 60
        private const val RULE_ID = "$RULE_SET_ID:max-function-length"

        internal fun errorMessage(bodyLines: Int): String =
            "Function body has $bodyLines lines of code, the maximum is $MAX_FUNCTION_BODY_LINES. " +
                "Extract granular, self-descriptive functions"
    }
}
