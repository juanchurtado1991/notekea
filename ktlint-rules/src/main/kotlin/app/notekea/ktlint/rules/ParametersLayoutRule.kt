package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_ID
import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_MAINTAINER
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.EQ
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LPAR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PRIMARY_CONSTRUCTOR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.RPAR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.SECONDARY_CONSTRUCTOR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_PARAMETER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfig
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.ifAutocorrectAllowed
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * How a function or a constructor lays out its parameters: three or more go one per line; a single one stays on
 * the declaration's line when the signature fits; two go either way, all on that line or one per line.
 */
class ParametersLayoutRule :
    Rule(
        ruleId = RuleId(RULE_ID),
        about = About(maintainer = RULE_SET_MAINTAINER),
        usesEditorConfigProperties = setOf(MAX_LINE_LENGTH_PROPERTY)
    ),
    RuleAutocorrectApproveHandler {
    private val owners = setOf(
        FUN,
        PRIMARY_CONSTRUCTOR,
        SECONDARY_CONSTRUCTOR
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
        val isDeclared =
            node.elementType == VALUE_PARAMETER_LIST && node.treeParent?.elementType in owners
        val hasComment = node.getChildren(null).any { it.elementType in commentTypes }
        val parameters = node.getChildren(null).filter { it.elementType == VALUE_PARAMETER }
        if (!isDeclared || hasComment || parameters.isEmpty()) {
            return
        }
        val isOnOneLine = node.getChildren(null).none { it.isLineBreak() }
        val fits = fitsOnOneLine(list = node, parameters = parameters)
        val goesOnOneLine = when (parameters.size) {
            1 -> fits
            2 -> fits && isOnOneLine
            else -> false
        }
        when {
            goesOnOneLine && !isOnOneLine -> emit(
                node.startOffset,
                JOIN_MESSAGE,
                true
            ).ifAutocorrectAllowed {
                parameters.first().setWhiteSpaceBefore()
                node.findChildByType(RPAR)?.setWhiteSpaceBefore()
            }

            !goesOnOneLine -> spread(list = node, parameters = parameters, emit = emit)
        }
    }

    private fun spread(
        list: ASTNode,
        parameters: List<ASTNode>,
        emit: (
            offset: Int,
            errorMessage: String,
            canBeAutoCorrected: Boolean
        ) -> AutocorrectDecision
    ) {
        val indent = list.lineIndent()
        parameters.filterNot { it.startsLine() }.forEach { parameter ->
            emit(parameter.startOffset, SPREAD_MESSAGE, true).ifAutocorrectAllowed {
                parameter.moveToNewLine(indent = indent + INDENT)
            }
        }
        val closing = list.findChildByType(RPAR)?.takeIf { !it.startsLine() } ?: return
        emit(closing.startOffset, CLOSING_MESSAGE, true).ifAutocorrectAllowed {
            closing.moveToNewLine(indent = indent)
        }
    }

    /** Whether the signature, up to the `=` of an expression body, fits on the line the list opens on. */
    private fun fitsOnOneLine(list: ASTNode, parameters: List<ASTNode>): Boolean {
        val opening = list.findChildByType(LPAR) ?: return false
        val closing = list.findChildByType(RPAR) ?: return false
        val inside = parameters.joinToString(separator = SEPARATOR) { it.text }
        val after = closing.restOfLine()
        val hasExpressionBody =
            list.treeParent.findChildByType(EQ) != null && after.contains(other = ASSIGNMENT)
        val tail = if (hasExpressionBody) {
            after.substringBefore(
                delimiter = ASSIGNMENT
            ) + ASSIGNMENT
        } else {
            after
        }
        val length =
            opening.lineLengthUpToEnd() + inside.length + closing.textLength + tail.trimEnd().length
        return !inside.contains(other = LINE_BREAK) && length <= maxLineLength
    }

    companion object {
        private const val ASSIGNMENT = " ="
        private const val CLOSING_MESSAGE = "The closing parenthesis goes on its own line"
        private const val JOIN_MESSAGE = "The parameters fit on the declaration's line"
        private const val RULE_ID = "$RULE_SET_ID:parameters-layout"
        private const val SEPARATOR = ", "
        private const val SPREAD_MESSAGE = "Each parameter goes on its own line"
    }
}
