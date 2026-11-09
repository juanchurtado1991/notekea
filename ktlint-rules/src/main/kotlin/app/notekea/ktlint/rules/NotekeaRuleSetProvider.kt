package app.notekea.ktlint.rules

import com.pinterest.ktlint.cli.ruleset.core.api.RuleSetProviderV3
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.pinterest.ktlint.rule.engine.core.api.RuleSetId

/**
 * Notekea's custom ktlint rule set. Each rule can be disabled in `.editorconfig`
 * with `ktlint_notekea_<rule-id> = disabled`.
 */
class NotekeaRuleSetProvider : RuleSetProviderV3(id = RuleSetId(RULE_SET_ID)) {
    override fun getRuleProviders(): Set<RuleProvider> = setOf(
        RuleProvider { CallbackLambdaRule() },
        RuleProvider { ChainPerLineRule() },
        RuleProvider { CodeLineLengthRule() },
        RuleProvider { ExpressionOnAssignmentLineRule() },
        RuleProvider { KdocRule() },
        RuleProvider { LambdaBodyLineRule() },
        RuleProvider { LocalFunctionReferenceRule() },
        RuleProvider { LogicalOperandsPerLineRule() },
        RuleProvider { MaxFileLengthRule() },
        RuleProvider { MaxFunctionLengthRule() },
        RuleProvider { NameCompoundExpressionRule() },
        RuleProvider { NamedArgumentsPerLineRule() },
        RuleProvider { NoSamePackageImportRule() },
        RuleProvider { ParameterOrderRule() },
        RuleProvider { ParametersLayoutRule() },
        RuleProvider { StatementSpacingRule() }
    )

    companion object {
        internal const val RULE_SET_ID = "notekea"
        internal const val RULE_SET_MAINTAINER = "Notekea"
    }
}
