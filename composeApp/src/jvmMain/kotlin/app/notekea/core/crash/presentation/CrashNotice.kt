package app.notekea.core.crash.presentation

import app.notekea.core.crash.domain.RecordedCrash

/** Tells the person that Notekea must close after [crash], and lets them copy or report it; returns once dismissed. */
interface CrashNotice {
    fun show(
        crash: RecordedCrash
    )
}
