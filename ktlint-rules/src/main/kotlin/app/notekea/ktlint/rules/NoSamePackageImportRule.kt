package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_ID
import app.notekea.ktlint.rules.NotekeaRuleSetProvider.Companion.RULE_SET_MAINTAINER
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IMPORT_ALIAS
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IMPORT_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.WHITE_SPACE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.ifAutocorrectAllowed
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/** A file does not import what its own package declares: the name is already in scope. */
class NoSamePackageImportRule :
    Rule(
        ruleId = RuleId(RULE_ID),
        about = About(maintainer = RULE_SET_MAINTAINER)
    ),
    RuleAutocorrectApproveHandler {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (
            offset: Int,
            errorMessage: String,
            canBeAutoCorrected: Boolean
        ) -> AutocorrectDecision
    ) {
        if (node.elementType != IMPORT_DIRECTIVE || node.findChildByType(IMPORT_ALIAS) != null) {
            return
        }
        val packageName = node.fileNode().findChildByType(
            PACKAGE_DIRECTIVE
        )?.text.orEmpty().removePrefix(PACKAGE)
        val imported = node.text.removePrefix(IMPORT).trim()
        val isFromOwnPackage = packageName.isNotBlank() &&
            imported.substringBeforeLast(delimiter = SEPARATOR, missingDelimiterValue = "") ==
            packageName.trim()
        if (isFromOwnPackage && !imported.endsWith(suffix = WILDCARD)) {
            emit(node.startOffset, ERROR_MESSAGE, true).ifAutocorrectAllowed {
                node.treeNext?.takeIf {
                    it.elementType == WHITE_SPACE
                }?.let(node.treeParent::removeChild)
                node.treeParent.removeChild(node)
            }
        }
    }

    companion object {
        private const val ERROR_MESSAGE = "A declaration of the file's own package needs no import"
        private const val IMPORT = "import"
        private const val PACKAGE = "package"
        private const val RULE_ID = "$RULE_SET_ID:no-same-package-import"
        private const val SEPARATOR = "."
        private const val WILDCARD = "*"
    }
}
