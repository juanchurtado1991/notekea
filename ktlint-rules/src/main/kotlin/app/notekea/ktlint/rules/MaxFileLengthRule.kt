package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_ID
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FILE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Lint-only rule: a file may not exceed [MAX_FILE_LINES] lines of code (imports, KDoc and previews counted, blank
 * lines and lines of brackets alone not), production code and tests alike. A file that grows is split by sub-feature.
 */
class MaxFileLengthRule : NotekeaRule(id = RULE_ID) {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: Emit
    ) {
        if (node.elementType != FILE) {
            return
        }
        val lines = codeLineCount(text = node.text)
        if (lines > MAX_FILE_LINES) {
            emit(node.startOffset, errorMessage(lines = lines), false)
        }
    }

    companion object {
        internal const val MAX_FILE_LINES = 350
        private const val RULE_ID = "$RULE_SET_ID:max-file-length"

        internal fun errorMessage(lines: Int): String =
            "File has $lines lines of code, the maximum is $MAX_FILE_LINES. Split it by sub-feature"
    }
}
