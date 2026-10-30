package com.testbuddy.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.testbuddy.Suite
import java.nio.file.Path

/**
 * The manager window's content: the app, its version and the machine at the top, the Automatic, Manual and Report
 * tabs, and the tab's screen.
 */
@Composable
internal fun ManagerContent(
    suite: Suite,
    machine: String,
    dataFolder: Path,
    state: ManagerState,
    actions: ManagerActions
) {
    Column(modifier = Modifier.fillMaxSize().background(color = Color.White)) {
        TitleBar(title = "${suite.appName} QA", subtitle = "${suite.version} · $machine")
        Tabs(selected = state.tab, onSelect = actions.onTab)
        Column(
            modifier = Modifier.fillMaxSize().padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(space = 10.dp)
        ) {
            when (state.tab) {
                ManagerTab.AUTOMATIC -> AutomaticTab(cases = suite.automated, state = state, actions = actions)

                ManagerTab.MANUAL -> ManualTab(
                    appName = suite.appName,
                    cases = suite.manual,
                    dataFolder = dataFolder,
                    state = state,
                    actions = actions
                )

                ManagerTab.REPORT -> ReportTab(state = state, actions = actions)
            }
        }
    }
}

@Composable
private fun TitleBar(
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(
            color = SelfTestColors.Page
        ).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(
                size = 22.dp
            ).background(color = SelfTestColors.Running, shape = RoundedCornerShape(size = 6.dp)),
            contentAlignment = Alignment.Center
        ) { Text(text = "TB", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, fontSize = 10.sp, color = SelfTestColors.Muted, maxLines = 1)
        }
    }
}

@Composable
private fun Tabs(
    selected: ManagerTab,
    onSelect: (ManagerTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
            .background(color = SelfTestColors.Quiet, shape = RoundedCornerShape(size = 8.dp)).padding(all = 3.dp)
    ) {
        ManagerTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier.weight(weight = 1f)
                    .background(
                        color = if (isSelected) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(size = 6.dp)
                    )
                    .clickable { onSelect(tab) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}
