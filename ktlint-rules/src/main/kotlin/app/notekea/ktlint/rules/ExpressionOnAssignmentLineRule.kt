package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_ID
import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_MAINTAINER
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.EQ
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PROPERTY_ACCESSOR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.STRING_TEMPLATE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.WHITE_SPACE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.Rule.VisitorModifier.RunAfterRule.Mode.REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfig
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.ifAutocorrectAllowed
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Where the expression after the `=` of a function, a property or a getter starts: on the line of the `=` while
 * its first line fits, on the next line otherwise. Three or more operands of `&&` or `||` always start on the next
 * line, and a comment or a raw string of several lines stays where it is.
 */
class ExpressionOnAssignmentLineRule :
    Rule(
        ruleId = RuleId(RULE_ID),
        about = About(maintainer = RULE_SET_MAINTAINER),
        visitorModifiers = setOf(
            VisitorModifier.RunAfterRule(
                ruleId = RuleId("$RULE_SET_ID:parameters-layout"),
                mode = REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED
            )
        ),
        usesEditorConfigProperties = setOf(MAX_LINE_LENGTH_PROPERTY)
    ),
    RuleAutocorrectApproveHandler {
    private val assignmentOwners = setOf(
        FUN,
        PROPERTY,
        PROPERTY_ACCESSOR
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
        val isDeclarationAssignment =
            node.elementType == EQ && node.treeParent?.elementType in assignmentOwners
        val expression = generateSequence(seed = node.treeNext) { it.treeNext }
            .firstOrNull { it.elementType != WHITE_SPACE }
        if (!isDeclarationAssignment || expression == null ||
            expression.elementType in commentTypes
        ) {
            return
        }
        val isOnNextLine = expression.startsLine()
        val isOperandList = expression.isLogicalExpression() &&
            expression.logicalOperators().size + 1 >= MIN_LOGICAL_OPERANDS
        val fits =
            !isOperandList && fitsOnAssignmentLine(assignment = node, expression = expression)
        when {
            isOnNextLine && fits && !startsWithMultilineString(expression = expression) ->
                emit(node.startOffset, JOIN_MESSAGE, true).ifAutocorrectAllowed {
                    expression.setWhiteSpaceBefore(text = SINGLE_SPACE)
                }

            !isOnNextLine && !fits -> emit(
                node.startOffset,
                WRAP_MESSAGE,
                true
            ).ifAutocorrectAllowed {
                expression.moveToNewLine(indent = node.lineIndent() + INDENT)
            }
        }
    }

    private fun fitsOnAssignmentLine(assignment: ASTNode, expression: ASTNode): Boolean {
        val firstLine = expression.text.substringBefore(delimiter = LINE_BREAK)
        val trailing = if (firstLine == expression.text) expression.restOfLine().trimEnd() else ""
        return assignment.lineLengthUpToEnd() + SINGLE_SPACE.length + firstLine.length +
            trailing.length <= maxLineLength
    }

    private fun startsWithMultilineString(expression: ASTNode): Boolean {
        val leadingString = generateSequence(seed = expression) { it.firstChildNode }
            .firstOrNull { it.elementType == STRING_TEMPLATE }
        return leadingString?.text?.contains(other = LINE_BREAK) == true
    }

    companion object {
        private const val JOIN_MESSAGE = "Expression should start on the same line as '='"
        private const val RULE_ID = "$RULE_SET_ID:expression-on-assignment-line"
        private const val SINGLE_SPACE = " "
        private const val WRAP_MESSAGE = "Expression that does not fit after '=' should start on the next line"
    }
}
