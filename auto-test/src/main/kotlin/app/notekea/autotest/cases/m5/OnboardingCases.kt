package app.notekea.autotest.cases.m5

import app.notekea.autotest.notekea
import app.notekea.autotest.relaunch
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val WELCOME_TITLE = "Welcome to Notekea"

/**
 * The cases of 5.1, the first-run welcome (`MVP_SMOKE_TEST.md`, the developer's design of 2026-11-11): its steps with
 * the sample vault, automatic; its look and the AI download, by eye.
 */
fun SuiteBuilder.onboardingCases() {
    welcomeWithSample()
    welcomeLook()
}

private fun SuiteBuilder.welcomeWithSample() {
    automated(
        id = "5.1-01",
        title = "The first-run welcome opens the sample vault, then offers the AI, and is not shown again"
    ) {
        notekea().setWelcomeSeen(isSeen = false)
        relaunch()
        expectText(text = WELCOME_TITLE)
        click(text = "Continue")
        expectText(text = "Choose your notes")
        click(text = "Try the sample vault")
        expectText(text = "Get the AI")
        click(text = "Not now")
        expectNoText(text = "Get the AI")

        expectText(
            text = "Notekea Sample Vault",
            substring = true
        )

        relaunch()
        expectNoText(text = WELCOME_TITLE)
    }
}

private fun SuiteBuilder.welcomeLook() {
    manual(
        id = "5.1-02",
        reason = ManualReason.EYES
    ) {
        title(text = "The first-run welcome looks like Apple's, and Download and start downloads the recommended model")

        steps(
            text =
            "1. Install Notekea QA fresh (or Settings → General → Welcome → Show again, then quit and start it).\n" +
                "2. Look at each step; press Esc on the first, then start again.\n3. Choose Open a folder… with " +
                "your own notes; on Get the AI click Download and start."
        )

        expected(
            text =
            "Step 2: a sheet over the welcome screen with the logo, three promises with icons, Continue and Skip, " +
                "\"1 of 3\"; Esc closes it and it does not come back. Step 3: the vault opens, the AI step names " +
                "the size that fits this computer, and the download shows in the status bar while the vault is " +
                "indexed."
        )
    }
}
