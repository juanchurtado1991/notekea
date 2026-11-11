package app.notekea.shell.presentation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.notekea.core.notes.domain.RelativePath

/**
 * The passage of a document to show (4.5): a citation, a search result or a related card asks for it, and the
 * document's tab scrolls to it and marks it; [DocumentReveal.count] tells the same passage asked again.
 */
@Stable
internal class DocumentFocus {
    var request by mutableStateOf<DocumentReveal?>(value = null)
        private set

    fun reveal(
        path: RelativePath,
        range: IntRange
    ) {
        request = DocumentReveal(
            path = path,
            range = range,
            count = (request?.count ?: 0) + 1
        )
    }
}
