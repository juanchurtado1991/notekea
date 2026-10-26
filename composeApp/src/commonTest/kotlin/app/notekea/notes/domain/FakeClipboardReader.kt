package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/**
 * The clipboard as a test fills it: an [image]'s PNG bytes, or [text]; an [imageError] stands for an image the
 * clipboard offers but cannot give.
 */
class FakeClipboardReader(
    var image: ByteArray? = null,
    var text: String? = null,
    var imageError: NotesError? = null
) : ClipboardReader {
    override fun image(): Outcome<ByteArray?, NotesError> =
        imageError?.let { Outcome.Failure(error = it) } ?: Outcome.Success(value = image)

    override fun text(): String? = text
}
