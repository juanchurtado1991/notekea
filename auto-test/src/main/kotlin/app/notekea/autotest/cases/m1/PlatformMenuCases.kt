package app.notekea.autotest.cases.m1

import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

/** The case of 1.1 about the system's app menu on macOS (`MVP_SMOKE_TEST.md`). */
internal fun SuiteBuilder.menuCases() {
    manual(
        id = "1.1-10",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)
        title(text = "The app menu on macOS")

        steps(
            text =
            "Only on macOS; on another system mark it passed.\n1. In Notekea, open the Playground Vault " +
                "(see TEST DATA above).\n2. Open the Notekea menu at the top of the screen and click About Notekea, " +
                "then Settings…"
        )

        expected(
            text =
            "macOS's Notekea menu opens the same About and Settings as the tool rail, and Notekea adds no " +
                "other menu to the bar."
        )
    }
}
