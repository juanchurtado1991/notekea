package app.notekea.autotest.cases.m1

import app.notekea.autotest.openVault
import com.testbuddy.CaseScope

/** Opens the Playground Vault and waits for its Welcome note: where most cases start. */
internal fun CaseScope.openPlayground() = openVault(
    name = PLAYGROUND,
    shownText = WELCOME
)

/** Opens the Personal Notes vault and waits for its Welcome note. */
internal fun CaseScope.openPersonalNotes() = openVault(
    name = PERSONAL_NOTES,
    shownText = WELCOME
)

/** Opens the Sample Vault and waits for its Home note. */
internal fun CaseScope.openSample() = openVault(
    name = SAMPLE,
    shownText = HOME
)
