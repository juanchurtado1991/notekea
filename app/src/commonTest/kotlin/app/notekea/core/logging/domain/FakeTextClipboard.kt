package app.notekea.core.logging.domain

/** [TextClipboard] that keeps what was copied, last copy last. */
class FakeTextClipboard : TextClipboard {
    val copied = mutableListOf<String>()

    override fun copy(
        text: String
    ) {
        copied += text
    }
}
