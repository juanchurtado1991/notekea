package com.testbuddy.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.testbuddy.ReportEnvironment
import com.testbuddy.Suite

/** The Self-Test window's page: the header and the current step, in one centered column (design A, the wizard). */
@Composable
internal fun SelfTestContent(
    suite: Suite,
    environment: ReportEnvironment,
    state: SelfTestState,
    session: SelfTestActions
) {
    Box(
        modifier = Modifier.fillMaxSize().background(color = SelfTestColors.Page).verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.widthIn(max = 720.dp).padding(all = 32.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            StepHeader(
                title = "${suite.appName} Self-Test",
                subtitle = "${suite.appName} ${suite.version} · ${environment.system} · ${environment.screen}",
                step = state.step
            )
            when (state.step) {
                SelfTestStep.AUTOMATIC -> AutomaticStep(
                    appName = suite.appName,
                    cases = suite.automated,
                    state = state,
                    onStart = session.onStart,
                    onStop = session.onStop,
                    onNext = session.onNext
                )

                SelfTestStep.MANUAL -> ManualStep(
                    cases = suite.manual,
                    index = state.manualIndex,
                    actions = session.manual
                )

                SelfTestStep.REPORT -> ReportStep(appName = suite.appName, state = state, actions = session.report)
            }
        }
    }
}

/** Everything the window's buttons do, gathered so the page stays a picture of the state. */
internal class SelfTestActions(
    val onStart: () -> Unit,
    val onStop: () -> Unit,
    val onNext: () -> Unit,
    val manual: ManualActions,
    val report: ReportActions
)
