package app.notekea.connections.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.TestTags
import app.notekea.core.presentation.designsystem.components.NotekeaSkeletonRow
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes

private val ROW_WIDTHS = listOf(0.7f, 0.5f, 0.6f)

/** What stands in for a Connections tab's rows the first time they are found. */
@Composable
fun ConnectionsSkeleton() {
    Column(modifier = Modifier.testTag(tag = TestTags.CONNECTIONS_LOADING)) {
        ROW_WIDTHS.forEach { NotekeaSkeletonRow(widthFraction = it) }
    }
}

@Preview
@Composable
private fun ConnectionsSkeletonPreview() {
    NotekeaPreviewThemes { ConnectionsSkeleton() }
}
