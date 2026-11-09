package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_ID
import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_MAINTAINER
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BLOCK
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CALLABLE_REFERENCE_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.COLONCOLON
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Lint-only rule: a function declared inside another one is called from a lambda (`onClick = { close() }`), never
 * passed by reference (`onClick = ::close`). Two such references are equal whatever they captured, so Compose
 * skips the composable that gets a new one and it keeps calling the old one, with the values of an earlier composition.
 */
class LocalFunctionReferenceRule :
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
        if (node.elementType != CALLABLE_REFERENCE_EXPRESSION) return
        if (node.firstChildNode.elementType != COLONCOLON) return

        val name = node.lastChildNode.text

        val isLocal = node
            .ancestors()
            .filter { it.elementType == BLOCK }
            .any { it.declares(name = name) }

        if (isLocal) emit(node.startOffset, ERROR_MESSAGE, false)
    }

    private fun ASTNode.declares(name: String): Boolean = getChildren(null).any {
        it.elementType == FUN && it.findChildByType(IDENTIFIER)?.text == name
    }

    companion object {
        internal const val ERROR_MESSAGE =
            "A function declared inside another one is called from a lambda (onClick = { close() }), " +
                "not passed by reference: Compose would keep calling an old one"
        private const val RULE_ID = "$RULE_SET_ID:local-function-reference"
    }
}
