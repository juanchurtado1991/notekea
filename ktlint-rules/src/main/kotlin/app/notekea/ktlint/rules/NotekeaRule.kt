package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_MAINTAINER
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfigProperty

/** How a rule reports what it found: where, the message, and whether it can correct it itself. */
typealias Emit = (
    offset: Int,
    errorMessage: String,
    canBeAutoCorrected: Boolean
) -> AutocorrectDecision

/** A rule of Notekea's set: [id] is its full id (`notekea:kdoc`), [properties] what it reads of `.editorconfig`. */
abstract class NotekeaRule(
    id: String,
    properties: Set<EditorConfigProperty<*>> = emptySet()
) : Rule(
    ruleId = RuleId(id),
    about = About(maintainer = RULE_SET_MAINTAINER),
    usesEditorConfigProperties = properties
),
    RuleAutocorrectApproveHandler
