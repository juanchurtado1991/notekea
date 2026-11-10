package app.notekea.query.presentation

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.NotekeaPromptField
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.resources.Res
import app.notekea.resources.query_describe
import app.notekea.resources.query_describe_fill
import app.notekea.resources.query_describe_wait
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_WIDTH_DP = 480

/**
 * "Describe what you want" (3.8): a request in plain words ("books I rated 4 or more") that the local model turns into
 * the rules below, which stay there to be checked and corrected before the table is trusted. Enter fills them.
 */
@Composable
internal fun QueryDescribeField(
    isDescribing: Boolean,
    onDescribe: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var request by rememberSaveable { mutableStateOf(value = "") }

    NotekeaPromptField(
        value = request,
        onValueChange = { request = it },
        placeholder = stringResource(resource = Res.string.query_describe),
        sendLabel = stringResource(resource = Res.string.query_describe_fill),
        stopLabel = stringResource(resource = Res.string.query_describe_wait),
        isRunning = isDescribing,
        canSend = request.isNotBlank() && !isDescribing,
        onSend = { onDescribe(request.trim()) },
        onStop = {},
        modifier = modifier
    )
}

@Preview
@Composable
private fun QueryDescribeFieldPreview() {
    NotekeaPreviewThemes {
        QueryDescribeField(
            isDescribing = false,
            onDescribe = {},
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)
        )
    }
}
