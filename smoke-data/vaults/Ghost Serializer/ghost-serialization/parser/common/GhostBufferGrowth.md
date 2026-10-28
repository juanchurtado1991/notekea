---
tags: [ghost-serialization, serialization, buffers, pooling]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/GhostBufferGrowth.kt
kind: functions
---

# GhostBufferGrowth

A single internal function that doubles a temporary scratch buffer, copying what was written and handing the old one back to the pool.

## What it is about

When a reader unescapes a string it writes the decoded bytes into a scratch `ByteArray` that it borrowed from a pool. If the string turns out to be longer than the buffer, the buffer must grow. `growBuffer` is the shared way to do that, so the flat reader and the streaming reader grow identically and neither allocates a throwaway array.

## How it works

`growBuffer(outBuffer, outPos)`:

1. Asks the pool for a buffer with `acquireScratchBuffer(minSize = outBuffer.size * BUFFER_SCALE_FACTOR)`. The factor is 2 in `GhostJsonWriterConstants`, so the new buffer is at least twice the old size.
2. Inside `apply`, copies the first `outPos` bytes (the ones already produced) from the old buffer into the new one with `copyInto`.
3. Releases the old buffer with `releaseScratchBuffer`, so another caller can reuse it.
4. Returns the new buffer.

Because both acquire and release are pool operations, repeated growth on a hot path usually reuses existing arrays rather than creating garbage; the file opts into `@InternalGhostApi` to call them. The caller must stop using the old reference after the call, which is why the function returns the replacement.

## The code

```kotlin
internal fun growBuffer(
    outBuffer: ByteArray,
    outPos: Int
): ByteArray = acquireScratchBuffer(
    minSize = outBuffer.size * WR.BUFFER_SCALE_FACTOR
).apply {
    outBuffer.copyInto(
        destination = this,
        destinationOffset = 0,
        startIndex = 0,
        endIndex = outPos
    )
    releaseScratchBuffer(buffer = outBuffer)
}
```

It matches the `grow: (ByteArray, Int) -> ByteArray` adapter taken by the quoted-string slow path kernel, which calls it whenever fewer bytes remain than a code point needs.

## Related

- [[GhostJsonQuotedStringSlow]]: its `readQuotedStringSlowCore` takes a `grow` adapter that readers satisfy with this function.
- [[GhostJsonFlatReaderStrings]]: the flat reader's string code that uses it.
- [[GhostJsonReader]]: the streaming reader that uses it.
- [[GhostPools]]: where the scratch-buffer pool lives.
- [[GhostJsonWriterConstants]]: defines `BUFFER_SCALE_FACTOR`.
