package app.notekea.core.crash.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.crash.domain.RecordedCrash
import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaButtonStyle
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.core.presentation.theme.notekeaCodeTextStyle
import app.notekea.resources.Res
import app.notekea.resources.crash_close
import app.notekea.resources.crash_copied
import app.notekea.resources.crash_copy
import app.notekea.resources.crash_message
import app.notekea.resources.crash_report_github
import app.notekea.resources.crash_report_not_saved
import app.notekea.resources.crash_report_saved
import app.notekea.resources.crash_title
import org.jetbrains.compose.resources.stringResource

private const val ICON_SIZE_DP = 28
private const val PREVIEW_HEIGHT_DP = 300
private const val PREVIEW_PATH = "/home/ana/.local/share/notekea/crashes/crash-2026-10-26T10-15-30Z.txt"
private const val PREVIEW_WIDTH_DP = 560

/**
 * What the crash notice says (`qa/README.md`): Notekea must close, the open note is saved first, where the report is
 * (selectable, to find it later), and the actions: Copy details (confirmed under the buttons), Report on GitHub and
 * Close Notekea, the primary one, placed as the platform orders dialog buttons. It fills the dialog window it is in.
 */
@Composable
fun CrashNoticeContent(
    crash: RecordedCrash,
    buttonOrder: DialogButtonOrder,
    onCopy: () -> Unit,
    onReport: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isCopied by remember(crash) { mutableStateOf(value = false) }
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier.padding(all = NotekeaDimensions.SpacingXl),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)
        ) {
            CrashHeading()
            Text(
                text = stringResource(resource = Res.string.crash_message),
                style = MaterialTheme.typography.bodyMedium
            )
            ReportLocation(path = crash.path)
            Spacer(modifier = Modifier.weight(weight = 1f))
            CrashButtons(
                buttonOrder = buttonOrder,
                onCopy = {
                    onCopy()
                    isCopied = true
                },
                onReport = onReport,
                onClose = onClose
            )
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                if (isCopied) {
                    Text(
                        text = stringResource(resource = Res.string.crash_copied),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CrashHeading() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = NotekeaIcons.Crash,
            contentDescription = null,
            modifier = Modifier.size(size = ICON_SIZE_DP.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Text(text = stringResource(resource = Res.string.crash_title), style = MaterialTheme.typography.titleLarge)
    }
}

/** Where the report was saved, selectable, or that it could not be (it can still be copied). */
@Composable
private fun ReportLocation(
    path: String?
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    if (path == null) {
        Text(
            text = stringResource(resource = Res.string.crash_report_not_saved),
            style = MaterialTheme.typography.bodySmall,
            color = muted
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
        Text(
            text = stringResource(resource = Res.string.crash_report_saved),
            style = MaterialTheme.typography.bodySmall,
            color = muted
        )
        SelectionContainer {
            Text(text = path, style = notekeaCodeTextStyle().copy(color = muted))
        }
    }
}

@Composable
private fun CrashButtons(
    buttonOrder: DialogButtonOrder,
    onCopy: () -> Unit,
    onReport: () -> Unit,
    onClose: () -> Unit
) {
    val close: @Composable () -> Unit = {
        NotekeaButton(text = stringResource(resource = Res.string.crash_close), onClick = onClose)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (buttonOrder == DialogButtonOrder.PRIMARY_THEN_CANCEL) close()
        NotekeaButton(
            text = stringResource(resource = Res.string.crash_copy),
            onClick = onCopy,
            style = NotekeaButtonStyle.SECONDARY
        )
        NotekeaButton(
            text = stringResource(resource = Res.string.crash_report_github),
            onClick = onReport,
            style = NotekeaButtonStyle.SECONDARY
        )
        Spacer(modifier = Modifier.weight(weight = 1f))
        if (buttonOrder == DialogButtonOrder.CANCEL_THEN_PRIMARY) close()
    }
}

@Composable
private fun CrashNoticePreviewFrame(
    path: String?
) {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            CrashNoticeContent(
                crash = RecordedCrash(text = "", path = path),
                buttonOrder = DialogButtonOrder.CANCEL_THEN_PRIMARY,
                onCopy = {},
                onReport = {},
                onClose = {}
            )
        }
    }
}

@Preview
@Composable
private fun CrashNoticeContentPreview() {
    CrashNoticePreviewFrame(path = PREVIEW_PATH)
}

@Preview
@Composable
private fun CrashNoticeNotSavedPreview() {
    CrashNoticePreviewFrame(path = null)
}
