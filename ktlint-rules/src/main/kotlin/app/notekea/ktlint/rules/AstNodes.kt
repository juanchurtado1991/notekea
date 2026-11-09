package app.notekea.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.ElementType.ANDAND
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BLOCK
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS_BODY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CONDITION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.EOL_COMMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUNCTION_LITERAL
import com.pinterest.ktlint.rule.engine.core.api.ElementType.KDOC
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LOOP_RANGE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.OROR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.SAFE_ACCESS_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.STRING_TEMPLATE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT_NAME
import com.pinterest.ktlint.rule.engine.core.api.ElementType.WHEN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.WHEN_CONDITION_IN_RANGE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.WHEN_CONDITION_WITH_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.WHEN_ENTRY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.WHITE_SPACE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl

internal const val INDENT = "    "
internal const val LINE_BREAK = "\n"
internal const val MIN_LOGICAL_OPERANDS = 3

internal val chainTypes = setOf(
    DOT_QUALIFIED_EXPRESSION,
    SAFE_ACCESS_EXPRESSION
)
internal val commentTypes = setOf(
    BLOCK_COMMENT,
    EOL_COMMENT,
    KDOC
)
private val callbackName = Regex(pattern = "on[A-Z0-9]\\w*")
private val conditionTypes = setOf(
    CONDITION,
    LOOP_RANGE,
    WHEN_CONDITION_IN_RANGE,
    WHEN_CONDITION_WITH_EXPRESSION
)
private val conditionBoundaries = setOf(
    BLOCK,
    CLASS_BODY,
    FUNCTION_LITERAL
)
private val logicalOperators = setOf(
    ANDAND,
    OROR
)

internal fun ASTNode.ancestors(): Sequence<ASTNode> = generateSequence(seed = treeParent) {
    it.treeParent
}

internal fun ASTNode.codeChildren(): List<ASTNode> = getChildren(null).filter {
    it.elementType != WHITE_SPACE && it.elementType !in commentTypes
}

internal fun ASTNode.firstLeaf(): ASTNode = generateSequence(seed = this) {
    it.firstChildNode
}.last()

internal fun ASTNode.lastLeaf(): ASTNode = generateSequence(seed = this) { it.lastChildNode }.last()

internal fun ASTNode.previousLeaf(): ASTNode? {
    val withPrevious = generateSequence(seed = this) { it.treeParent }.firstOrNull {
        it.treePrev != null
    }
    return withPrevious?.treePrev?.lastLeaf()
}

internal fun ASTNode.followingLeaf(): ASTNode? {
    val withNext = generateSequence(seed = this) {
        it.treeParent
    }.firstOrNull { it.treeNext != null }
    return withNext?.treeNext?.firstLeaf()
}

internal fun ASTNode.isLineBreak(): Boolean =
    elementType == WHITE_SPACE && text.contains(LINE_BREAK)

/** Whether nothing but indentation stands before the node on its line. */
internal fun ASTNode.startsLine(): Boolean = firstLeaf().previousLeaf()?.isLineBreak() ?: true

/** The indentation of the line the node starts on. */
internal fun ASTNode.lineIndent(): String {
    val lineBreak = generateSequence(seed = firstLeaf().previousLeaf()) { it.previousLeaf() }
        .firstOrNull { it.isLineBreak() }
    return lineBreak?.text?.substringAfterLast(delimiter = LINE_BREAK).orEmpty()
}

internal fun ASTNode.isInsideString(): Boolean = ancestors().any {
    it.elementType == STRING_TEMPLATE
}

internal fun ASTNode.isLogicalExpression(): Boolean = elementType == BINARY_EXPRESSION &&
    findChildByType(OPERATION_REFERENCE)?.firstChildNode?.elementType in logicalOperators

/** The `&&` and `||` of a logical expression and of the logical expressions it is made of, in order. */
internal fun ASTNode.logicalOperators(): List<ASTNode> = getChildren(null).flatMap { child ->
    when {
        child.elementType == OPERATION_REFERENCE -> listOf(child)
        child.isLogicalExpression() -> child.logicalOperators()
        else -> emptyList()
    }
}

/** Whether the lambda is the value of a named argument that is a callback: `onClick = { … }`. */
internal fun ASTNode.isCallbackLambda(): Boolean {
    val argument = treeParent?.treeParent?.takeIf { it.elementType == VALUE_ARGUMENT }
    val name = argument?.findChildByType(VALUE_ARGUMENT_NAME)?.text.orEmpty()
    return callbackName.matches(input = name)
}

/** The root of the file's tree. */
internal fun ASTNode.fileNode(): ASTNode = generateSequence(seed = this) { it.treeParent }.last()

/** The length the node's line has up to and including the node's last character. */
internal fun ASTNode.lineLengthUpToEnd(): Int {
    val end = startOffset + textLength
    val text = fileNode().text
    return end -
        (
            text.lastIndexOf(
                string = LINE_BREAK,
                startIndex = (end - 1).coerceAtLeast(minimumValue = 0)
            ) +
                1
            )
}

/** The text that follows the node on the line it ends on. */
internal fun ASTNode.restOfLine(): String {
    val end = startOffset + textLength
    return fileNode().text.substring(startIndex = end).substringBefore(delimiter = LINE_BREAK)
}

/**
 * Whether the node is part of a condition: of an `if`, a loop or a `when`, or an operand of `&&` or `||`.
 * A lambda or a block inside the condition starts ordinary code again.
 */
internal fun ASTNode.isInCondition(): Boolean {
    var child = this
    ancestors().forEach { ancestor ->
        val isSubject = ancestor.elementType == WHEN && child.elementType != WHEN_ENTRY
        when {
            ancestor.elementType in conditionBoundaries -> return false
            ancestor.elementType in conditionTypes || isSubject || ancestor.isLogicalExpression() -> return true
        }
        child = ancestor
    }
    return false
}

/**
 * How many lines of [text] hold code or words: a blank line and a line of brackets alone do not count, so a limit
 * on length does not depend on how the code is laid out.
 */
internal fun codeLineCount(text: String): Int = text
    .lineSequence()
    .count { line -> line.any(Char::isLetterOrDigit) }

/** Puts the node at the start of a new line indented by [indent]. */
internal fun ASTNode.moveToNewLine(indent: String) {
    setWhiteSpaceBefore(text = LINE_BREAK + indent)
}

/** Joins the node to what stands before it, leaving [text] between them (nothing by default). */
internal fun ASTNode.setWhiteSpaceBefore(text: String = "") {
    val leaf = firstLeaf()
    val previous = leaf.previousLeaf()
    when {
        previous?.elementType == WHITE_SPACE && text.isEmpty() -> previous.treeParent.removeChild(
            previous
        )

        previous?.elementType == WHITE_SPACE -> (previous as LeafPsiElement).rawReplaceWithText(
            text
        )

        text.isNotEmpty() -> treeParent.addChild(PsiWhiteSpaceImpl(text), this)
    }
}
