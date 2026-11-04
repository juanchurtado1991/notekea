package app.notekea.core.notes.data

import okio.FileHandle

/** A [FileHandle] over [delegate] that tells [onFlush] when its content is sent to the disk. */
class RecordingFileHandle(
    private val delegate: FileHandle,
    private val onFlush: () -> Unit
) : FileHandle(readWrite = true) {
    override fun protectedRead(
        fileOffset: Long,
        array: ByteArray,
        arrayOffset: Int,
        byteCount: Int
    ): Int = delegate.read(fileOffset = fileOffset, array = array, arrayOffset = arrayOffset, byteCount = byteCount)

    override fun protectedWrite(
        fileOffset: Long,
        array: ByteArray,
        arrayOffset: Int,
        byteCount: Int
    ) = delegate.write(fileOffset = fileOffset, array = array, arrayOffset = arrayOffset, byteCount = byteCount)

    override fun protectedFlush() {
        delegate.flush()
        onFlush()
    }

    override fun protectedResize(
        size: Long
    ) = delegate.resize(size = size)

    override fun protectedSize(): Long = delegate.size()

    override fun protectedClose() = delegate.close()
}
