package app.notekea.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BOOLEAN_CONSTANT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CHARACTER_CONSTANT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.DIVEQ
import com.pinterest.ktlint.rule.engine.core.api.ElementType.EQ
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FLOAT_CONSTANT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUNCTION_LITERAL
import com.pinterest.ktlint.rule.engine.core.api.ElementType.INTEGER_CONSTANT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.MINUSEQ
import com.pinterest.ktlint.rule.engine.core.api.ElementType.MULTEQ
import com.pinterest.ktlint.rule.engine.core.api.ElementType.NULL
import com.pinterest.ktlint.rule.engine.core.api.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PLUSEQ
import com.pinterest.ktlint.rule.engine.core.api.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.STRING_TEMPLATE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.THIS_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT_NAME
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

private const val MAX_CHAIN_LINKS = 2
private const val MAX_NAMED_ARGUMENTS = 1

private val assignments = setOf(
    DIVEQ,
    EQ,
    MINUSEQ,
    MULTEQ,
    PLUSEQ
)
private val nameTypes = setOf(
    BOOLEAN_CONSTANT,
    CHARACTER_CONSTANT,
    FLOAT_CONSTANT,
    INTEGER_CONSTANT,
    NULL,
    REFERENCE_EXPRESSION,
    STRING_TEMPLATE,
    THIS_EXPRESSION
)

/** Whether the expression is a name, a constant or a path of properties: `it.file.path`. */
internal fun ASTNode.isPropertyPath(): Boolean = when (elementType) {
    in nameTypes -> true

    in chainTypes -> codeChildren().let { parts ->
        parts.first().isPropertyPath() && parts.last().elementType == REFERENCE_EXPRESSION
    }

    else -> false
}

/**
 * Whether a lambda may hold the statement on its own line: a property path, or a call or an assignment on one
 * line that no other rule would break (no lambda inside, no chain of three `.`, no call with two named arguments).
 */
internal fun ASTNode.isShortStatement(): Boolean {
    val isAssignment = elementType == BINARY_EXPRESSION &&
        findChildByType(OPERATION_REFERENCE)?.firstChildNode?.elementType in assignments
    val isCall = elementType == CALL_EXPRESSION || elementType in chainTypes
    val parts = descendants()
    val staysWhole = !text.contains(other = LINE_BREAK) &&
        parts.none { it.elementType == FUNCTION_LITERAL } &&
        parts.none {
            it.chainLinks() > MAX_CHAIN_LINKS || it.namedArguments() > MAX_NAMED_ARGUMENTS
        }
    return isPropertyPath() || ((isAssignment || isCall) && staysWhole)
}

private fun ASTNode.descendants(): List<ASTNode> =
    listOf(this) + getChildren(null).flatMap { it.descendants() }

/** How many `.` the chain that ends at the node has, its receiver's included. */
private fun ASTNode.chainLinks(): Int =
    generateSequence(seed = this.takeIf { it.elementType in chainTypes }) { link ->
        link.firstChildNode.takeIf { it.elementType in chainTypes }
    }.count()

private fun ASTNode.namedArguments(): Int = if (elementType == VALUE_ARGUMENT_LIST) {
    getChildren(null).count {
        it.elementType == VALUE_ARGUMENT &&
            it.findChildByType(VALUE_ARGUMENT_NAME) != null
    }
} else {
    0
}
