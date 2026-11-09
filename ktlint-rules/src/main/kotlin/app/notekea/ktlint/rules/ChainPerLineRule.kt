package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_ID
import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_MAINTAINER
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.ARRAY_ACCESS_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.DOT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IMPORT_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LAMBDA_ARGUMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.POSTFIX_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.SAFE_ACCESS
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT_LIST
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.ifAutocorrectAllowed
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * A chain of three or more `.` that calls something goes one segment per line, the receiver alone on the first;
 * so does a call on a receiver that takes both arguments in parentheses and a trailing lambda of several lines
 * (`logger.e(throwable = throwable) { MESSAGE }` stays on its line). A qualified name
 * (`Clock.System`) is one receiver, and a chain inside a condition or a string stays as it is.
 */
class ChainPerLineRule :
    Rule(
        ruleId = RuleId(RULE_ID),
        about = About(maintainer = RULE_SET_MAINTAINER)
    ),
    RuleAutocorrectApproveHandler {
    private val receiverWrappers = setOf(
        ARRAY_ACCESS_EXPRESSION,
        POSTFIX_EXPRESSION
    )
    private val operatorTypes = setOf(
        DOT,
        SAFE_ACCESS
    )
    private val skippedOwners = setOf(
        ANNOTATION_ENTRY,
        IMPORT_DIRECTIVE,
        PACKAGE_DIRECTIVE
    )

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (
            offset: Int,
            errorMessage: String,
            canBeAutoCorrected: Boolean
        ) -> AutocorrectDecision
    ) {
        if (!isCheckedChain(node = node)) {
            return
        }
        val links = linksOf(top = node)
        val hasCall = links.any { selectorOf(link = it).elementType == CALL_EXPRESSION } ||
            receiverOf(link = links.first()).elementType == CALL_EXPRESSION
        val isLongChain = links.size >= MIN_LINKS && hasCall
        if (!isLongChain && links.none { takesArgumentsAndLambda(call = selectorOf(link = it)) }) {
            return
        }
        val indent = node.lineIndent() + INDENT
        links
            .map { link -> link.getChildren(null).first { it.elementType in operatorTypes } }
            .filterNot { it.startsLine() }
            .forEach { operator ->
                emit(operator.startOffset, ERROR_MESSAGE, true).ifAutocorrectAllowed {
                    operator.moveToNewLine(indent = indent)
                }
            }
    }

    private fun isCheckedChain(node: ASTNode): Boolean {
        if (node.elementType !in chainTypes) {
            return false
        }
        val owner = generateSequence(seed = node) { child ->
            child.treeParent?.takeIf {
                it.elementType in receiverWrappers &&
                    it.firstChildNode == child
            }
        }.last()
        val isReceiver =
            owner.treeParent?.elementType in chainTypes && owner.treeParent.firstChildNode == owner
        return !isReceiver &&
            node.ancestors().none { it.elementType in skippedOwners } &&
            !node.isInsideString() &&
            !node.isInCondition()
    }

    /** The links of the chain from its receiver outwards, without those that only qualify a name. */
    private fun linksOf(top: ASTNode): List<ASTNode> {
        val links = generateSequence(seed = top) { link ->
            receiverOf(link = link).takeIf { it.elementType in chainTypes }
        }.toList().reversed()
        val isQualified = isTypeName(node = receiverOf(link = links.first()))
        val qualifiers = if (isQualified) {
            links.takeWhile {
                isTypeName(node = selectorOf(link = it))
            }.size
        } else {
            0
        }
        return links.drop(n = qualifiers).ifEmpty { links.takeLast(n = 1) }
    }

    private fun isTypeName(node: ASTNode): Boolean = node.elementType == REFERENCE_EXPRESSION &&
        node.text.first().isUpperCase()

    private fun receiverOf(link: ASTNode): ASTNode = generateSequence(
        seed = link.firstChildNode
    ) { receiver ->
        receiver.firstChildNode.takeIf { receiver.elementType in receiverWrappers }
    }.last()

    private fun selectorOf(link: ASTNode): ASTNode = link.lastChildNode

    private fun takesArgumentsAndLambda(call: ASTNode): Boolean =
        call.elementType == CALL_EXPRESSION &&
            call.findChildByType(VALUE_ARGUMENT_LIST)?.findChildByType(VALUE_ARGUMENT) != null &&
            call.findChildByType(LAMBDA_ARGUMENT)?.textContains('\n') == true

    companion object {
        private const val ERROR_MESSAGE = "Each segment of the chain goes on its own line"
        private const val MIN_LINKS = 3
        private const val RULE_ID = "$RULE_SET_ID:chain-per-line"
    }
}
