package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_ID
import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_MAINTAINER
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.RPAR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT_NAME
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.ifAutocorrectAllowed
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * A call with two or more named arguments puts every argument on its own line. A call inside a condition, a
 * string or an annotation stays as it is.
 */
class NamedArgumentsPerLineRule :
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
        if (node.elementType != VALUE_ARGUMENT_LIST ||
            node.treeParent?.elementType == ANNOTATION_ENTRY
        ) {
            return
        }
        val arguments = node.getChildren(null).filter { it.elementType == VALUE_ARGUMENT }
        val namedCount = arguments.count { it.findChildByType(VALUE_ARGUMENT_NAME) != null }
        if (namedCount < MIN_NAMED_ARGUMENTS || node.isInsideString() || node.isInCondition()) {
            return
        }
        val indent = node.lineIndent()
        arguments.filterNot { it.startsLine() }.forEach { argument ->
            emit(argument.startOffset, ERROR_MESSAGE, true).ifAutocorrectAllowed {
                argument.moveToNewLine(indent = indent + INDENT)
            }
        }
        val closing = node.findChildByType(RPAR)?.takeIf { !it.startsLine() } ?: return
        emit(closing.startOffset, CLOSING_MESSAGE, true).ifAutocorrectAllowed {
            closing.moveToNewLine(indent = indent)
        }
    }

    companion object {
        private const val CLOSING_MESSAGE = "The closing parenthesis goes on its own line"
        private const val ERROR_MESSAGE = "Each argument of a call with two named arguments goes on its own line"
        private const val MIN_NAMED_ARGUMENTS = 2
        private const val RULE_ID = "$RULE_SET_ID:named-arguments-per-line"
    }
}
