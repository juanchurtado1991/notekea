package com.testbuddy.crash

/** What the crash notice says; [english] fills them in for an app called `appName`. */
public data class CrashTexts(
    val title: String,
    val message: String,
    val saved: String,
    val notSaved: String,
    val copy: String,
    val copied: String,
    val report: String,
    val close: String
) {
    public companion object {
        public fun english(appName: String): CrashTexts = CrashTexts(
            title = "$appName needs to close",
            message = "Something went wrong and $appName has to close. Your open work is saved before it closes.\n\n" +
                "To help fix it, click Report on GitHub and paste the details (already copied) into the form, or " +
                "copy them and send them to us.",
            saved = "A report was saved to:",
            notSaved = "The report could not be saved, but you can still copy it.",
            copy = "Copy details",
            copied = "Copied to the clipboard",
            report = "Report on GitHub",
            close = "Close $appName"
        )
    }
}
