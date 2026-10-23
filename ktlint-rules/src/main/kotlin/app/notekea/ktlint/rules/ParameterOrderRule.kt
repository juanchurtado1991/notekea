package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_ID
import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_MAINTAINER
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.ACTUAL_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.EQ
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUNCTION_TYPE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.NULLABLE_TYPE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.OVERRIDE_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PRIMARY_CONSTRUCTOR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.SECONDARY_CONSTRUCTOR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_PARAMETER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Lint-only rule: parameters of functions and constructors are ordered mandatory, nullable, then with a default
 * value. A trailing function-typed parameter is exempt so trailing-lambda syntax keeps working, and `override` /
 * `actual` declarations are skipped because their signature is dictated by another declaration.
 * No autocorrect: reordering parameters would break positional call sites.
 */
class ParameterOrderRule :
    Rule(
        ruleId = RuleId(RULE_ID),
        about = About(maintainer = RULE_SET_MAINTAINER)
    ),
    RuleAutocorrectApproveHandler {
    private val checkedOwners = setOf(
        FUN,
        PRIMARY_CONSTRUCTOR,
        SECONDARY_CONSTRUCTOR
    )
    private val signatureFixingModifiers = setOf(
        ACTUAL_KEYWORD,
        OVERRIDE_KEYWORD
    )

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision
    ) {
        val owner = node.treeParent
        val isCheckedParameterList = node.elementType == VALUE_PARAMETER_LIST &&
            owner?.elementType in checkedOwners &&
            !hasFixedSignature(owner = owner)
        if (!isCheckedParameterList) {
            return
        }
        val parameters = node.getChildren(null).filter { it.elementType == VALUE_PARAMETER }
        val isTrailingLambda = parameters.lastOrNull()?.let { isFunctionTyped(parameter = it) } == true
        val checkedParameters = if (isTrailingLambda) parameters.dropLast(n = 1) else parameters
        var highestKind = ParameterKind.MANDATORY
        checkedParameters.forEach { parameter ->
            val kind = kindOf(parameter = parameter)
            if (kind < highestKind) {
                emit(parameter.startOffset, ERROR_MESSAGE, false)
            } else {
                highestKind = kind
            }
        }
    }

    private fun hasFixedSignature(
        owner: ASTNode
    ): Boolean {
        val declaration = if (owner.elementType == PRIMARY_CONSTRUCTOR) owner.treeParent else owner
        val modifiers = declaration?.findChildByType(MODIFIER_LIST) ?: return false
        return modifiers.getChildren(null).any { it.elementType in signatureFixingModifiers }
    }

    private fun isFunctionTyped(
        parameter: ASTNode
    ): Boolean {
        val typeReference = parameter.findChildByType(TYPE_REFERENCE) ?: return false
        val nullableType = typeReference.findChildByType(NULLABLE_TYPE)
        return typeReference.findChildByType(FUNCTION_TYPE) != null ||
            nullableType?.findChildByType(FUNCTION_TYPE) != null
    }

    private fun kindOf(
        parameter: ASTNode
    ): ParameterKind {
        val hasDefaultValue = parameter.findChildByType(EQ) != null
        val isNullable = parameter.findChildByType(TYPE_REFERENCE)?.findChildByType(NULLABLE_TYPE) != null
        return when {
            hasDefaultValue -> ParameterKind.DEFAULT
            isNullable -> ParameterKind.NULLABLE
            else -> ParameterKind.MANDATORY
        }
    }

    companion object {
        private const val ERROR_MESSAGE = "Parameters must be ordered: mandatory, then nullable, " +
            "then with a default value"
        private const val RULE_ID = "$RULE_SET_ID:parameter-order"
    }
}
