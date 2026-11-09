package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_ID
import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_MAINTAINER
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BLOCK
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUNCTION_LITERAL
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LBRACE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.RBRACE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfig
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.ifAutocorrectAllowed
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * A lambda of one short statement (a property path, a call or an assignment, see [isShortStatement]) goes on one
 * line when it fits: `reduce { it.copy(tree = tree) }`. Any other body goes on its own lines. A lambda inside a
 * condition or a string stays as it is.
 */
class LambdaBodyLineRule :
    Rule(
        ruleId = RuleId(RULE_ID),
        about = About(maintainer = RULE_SET_MAINTAINER),
        usesEditorConfigProperties = setOf(MAX_LINE_LENGTH_PROPERTY)
    ),
    RuleAutocorrectApproveHandler {
    private val lineBreaks = Regex(pattern = "\\s*\\n\\s*")
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
        val block = node.takeIf { it.elementType == FUNCTION_LITERAL }?.findChildByType(BLOCK)
        val statements = block?.codeChildren().orEmpty()
        if (statements.isEmpty() || node.isInsideString() || node.isInCondition()) {
            return
        }
        val parts = node.getChildren(null) + block?.getChildren(null).orEmpty()
        val isShort = statements.size == 1 && parts.none { it.elementType in commentTypes } &&
            statements.first().isShortStatement()
        val isOnOneLine = !node.text.contains(other = LINE_BREAK)
        when {
            isOnOneLine && !(
                isShort && fitsAsItIs(
                    lambda = node,
                    statement = statements.first()
                )
                ) ->
                emit(node.startOffset, SPLIT_MESSAGE, true).ifAutocorrectAllowed {
                    val indent = node.lineIndent()
                    statements.first().moveToNewLine(indent = indent + INDENT)
                    node.findChildByType(RBRACE)?.moveToNewLine(indent = indent)
                }

            !isOnOneLine && isShort && fitsJoined(lambda = node) ->
                emit(node.startOffset, JOIN_MESSAGE, true).ifAutocorrectAllowed {
                    parts.filter { it.isLineBreak() }.forEach {
                        (it as LeafPsiElement).rawReplaceWithText(SINGLE_SPACE)
                    }
                }
        }
    }

    /** A property path stays on its line even when the line is too long: splitting it would not help. */
    private fun fitsAsItIs(lambda: ASTNode, statement: ASTNode): Boolean =
        statement.isPropertyPath() ||
            lambda.lineLengthUpToEnd() + lambda.restOfLine().trimEnd().length <= maxLineLength

    private fun fitsJoined(lambda: ASTNode): Boolean {
        val start = lambda.findChildByType(LBRACE)?.lineLengthUpToEnd() ?: return false
        val joined = lambda.text.replace(regex = lineBreaks, replacement = SINGLE_SPACE)
        return start - 1 + joined.length + lambda.restOfLine().trimEnd().length <= maxLineLength
    }

    companion object {
        internal const val JOIN_MESSAGE = "A lambda of one short statement that fits goes on one line"
        private const val RULE_ID = "$RULE_SET_ID:lambda-body-line"
        private const val SINGLE_SPACE = " "
        internal const val SPLIT_MESSAGE = "A lambda whose body is not one short statement goes on several lines"
    }
}
