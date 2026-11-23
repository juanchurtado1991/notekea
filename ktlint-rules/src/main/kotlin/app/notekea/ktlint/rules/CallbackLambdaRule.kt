package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_ID
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BLOCK
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUNCTION_LITERAL
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Lint-only rule: a callback passed by name (`onClick = { … }`) of two or more statements is a named function: a
 * member passed by reference (`onClick = ::close`), a function declared in place called from the lambda
 * (`onClick = { close() }`).
 */
class CallbackLambdaRule : NotekeaRule(id = RULE_ID) {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: Emit
    ) {
        val isCallback =
            node.elementType == FUNCTION_LITERAL && node.isCallbackLambda() &&
                !node.isInsideString()
        val statements = if (isCallback) {
            node.findChildByType(
                BLOCK
            )?.codeChildren().orEmpty()
        } else {
            emptyList()
        }
        if (statements.size > 1) emit(node.startOffset, ERROR_MESSAGE, false)
    }

    companion object {
        internal const val ERROR_MESSAGE =
            "A callback of several statements is a named function: onClick = ::close, or { close() } when declared in place"
        private const val RULE_ID = "$RULE_SET_ID:callback-lambda"
    }
}
