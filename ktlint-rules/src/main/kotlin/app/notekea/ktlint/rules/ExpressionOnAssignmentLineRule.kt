package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_ID
import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_MAINTAINER
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.EOL_COMMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.EQ
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.KDOC
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PROPERTY_ACCESSOR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.STRING_TEMPLATE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.WHITE_SPACE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfig
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.ifAutocorrectAllowed
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * Keeps the expression on the same line as the `=` of a function expression body, a property initializer
 * or a getter, as long as the first line of the expression still fits within `max_line_length`.
 * Autocorrect only joins the lines; the standard `indent` rule re-indents the rest of a multi-line expression.
 * Multi-line raw strings are skipped because the standard `string-template-indent` rule requires them to start
 * on their own line; joining them would make both rules fight during formatting.
 */
class ExpressionOnAssignmentLineRule :
    Rule(
        ruleId = RuleId(RULE_ID),
        about = About(maintainer = RULE_SET_MAINTAINER),
        usesEditorConfigProperties = setOf(MAX_LINE_LENGTH_PROPERTY)
    ),
    RuleAutocorrectApproveHandler {
    private val assignmentOwners = setOf(
        FUN,
        PROPERTY,
        PROPERTY_ACCESSOR
    )
    private val commentTypes = setOf(
        BLOCK_COMMENT,
        EOL_COMMENT,
        KDOC
    )
    private var maxLineLength = Int.MAX_VALUE

    override fun beforeFirstNode(
        editorConfig: EditorConfig
    ) {
        maxLineLength = editorConfig[MAX_LINE_LENGTH_PROPERTY]
    }

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision
    ) {
        val isDeclarationAssignment = node.elementType == EQ && node.treeParent?.elementType in assignmentOwners
        if (!isDeclarationAssignment) {
            return
        }
        val whitespace = node.treeNext ?: return
        val isExpressionOnNextLine = whitespace.elementType == WHITE_SPACE && whitespace.text.contains(NEWLINE)
        val expression = whitespace.treeNext ?: return
        val canJoin = isExpressionOnNextLine &&
            expression.elementType !in commentTypes &&
            !startsWithMultilineString(expression = expression) &&
            fitsOnAssignmentLine(
                assignment = node,
                expression = expression
            )
        if (canJoin) {
            emit(node.startOffset, ERROR_MESSAGE, true).ifAutocorrectAllowed {
                (whitespace as LeafPsiElement).rawReplaceWithText(SINGLE_SPACE)
            }
        }
    }

    private fun fitsOnAssignmentLine(
        assignment: ASTNode,
        expression: ASTNode
    ): Boolean {
        val fileText = rootOf(node = assignment).text
        val lineStart = fileText.lastIndexOf(
            char = NEWLINE,
            startIndex = assignment.startOffset - 1
        ) + 1
        val assignmentLineLength = assignment.startOffset + assignment.textLength - lineStart
        val joinedLineLength = assignmentLineLength + SINGLE_SPACE.length +
            expression.text.substringBefore(delimiter = NEWLINE).length
        return joinedLineLength <= maxLineLength
    }

    private fun rootOf(
        node: ASTNode
    ): ASTNode = generateSequence(seed = node) { it.treeParent }.last()

    private fun startsWithMultilineString(
        expression: ASTNode
    ): Boolean {
        val leadingString = generateSequence(seed = expression) { it.firstChildNode }
            .firstOrNull { it.elementType == STRING_TEMPLATE }
        return leadingString?.text?.contains(NEWLINE) == true
    }

    companion object {
        private const val ERROR_MESSAGE = "Expression should start on the same line as '='"
        private const val NEWLINE = '\n'
        private const val RULE_ID = "$RULE_SET_ID:expression-on-assignment-line"
        private const val SINGLE_SPACE = " "
    }
}
