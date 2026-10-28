package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_ID
import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_MAINTAINER
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FILE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Lint-only rule: a file may not exceed [MAX_FILE_LINES] lines, everything counted (imports, KDoc, previews, blank
 * lines), production code and tests alike (`GUIDELINES.md` §6). A file that grows is split by sub-feature.
 */
class MaxFileLengthRule :
    Rule(
        ruleId = RuleId(RULE_ID),
        about = About(maintainer = RULE_SET_MAINTAINER)
    ),
    RuleAutocorrectApproveHandler {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision
    ) {
        if (node.elementType != FILE) {
            return
        }
        val lines = node.text.trimEnd(NEWLINE).count { it == NEWLINE } + 1
        if (lines > MAX_FILE_LINES) {
            emit(node.startOffset, errorMessage(lines = lines), false)
        }
    }

    private fun errorMessage(
        lines: Int
    ): String = "File has $lines lines, the maximum is $MAX_FILE_LINES. Split it by sub-feature"

    companion object {
        internal const val MAX_FILE_LINES = 350
        private const val NEWLINE = '\n'
        private const val RULE_ID = "$RULE_SET_ID:max-file-length"
    }
}
