package app.notekea.notes.domain

/** The clipboard as a test fills it: an [image]'s PNG bytes, or [text]. */
class FakeClipboardReader(
    var image: ByteArray? = null,
    var text: String? = null
) : ClipboardReader {
    override fun image(): ByteArray? = image

    override fun text(): String? = text
}
