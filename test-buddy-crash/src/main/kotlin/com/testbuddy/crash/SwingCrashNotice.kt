package com.testbuddy.crash

import javax.swing.JOptionPane

/**
 * The crash notice as a plain Swing dialog, only when the Compose notice itself fails: it looks foreign to the app,
 * but it does not depend on Compose. Same texts and actions as [CrashNotice]; it returns once Close is chosen.
 */
internal class SwingCrashNotice(private val config: CrashConfig) {
    fun show(crash: CrashReport) {
        val texts = config.texts
        val message = texts.message + "\n\n" + (crash.path?.let { texts.saved + "\n" + it } ?: texts.notSaved)
        val options = listOfNotNull(texts.copy, config.issueFormUrl?.let { texts.report }, texts.close).toTypedArray()
        while (true) {
            val choice = JOptionPane.showOptionDialog(
                null,
                message,
                texts.title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                options,
                options.last()
            )
            when (options.getOrNull(choice)) {
                texts.copy -> CrashActions.copy(text = crash.text)
                texts.report -> config.issueFormUrl?.let { CrashActions.report(text = crash.text, url = it) }
                else -> return
            }
        }
    }
}
