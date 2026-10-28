---
tags: [ghost-serializer, serialization, platform, jvm, android, pooling]
source: ghost-serialization/src/jvmAndroidMain/kotlin/com/ghost/serialization/Ghost.jvmAndroid.kt
kind: expect/actual
---

# Ghost.jvmAndroid

The shared JVM and Android actuals of the encode and decode helpers that `Ghost` uses to run its pooled readers and writers.

## What it is about

`Ghost`'s public functions are thin: they resolve a serializer and call `ghostInternalEncode*` or `ghostInternalUse*Reader` helpers declared as `expect`. This file implements them for the JVM and Android with `ThreadLocal` pools, so the common encode and decode paths allocate almost nothing after warm-up.

## How it works

Each thread owns one flat reader, one string reader, one streaming source reader, one `WriterSinkPair` (byte writer plus its buffer) and one `WriterStringPair` (char writer plus its buffer), all in `ThreadLocal`s created lazily. `acquireFlatWriterPair` and `acquireStringWriterPair` fetch the pair and `reset()` it, so the buffer stays allocated across calls and only grows once.

The encode helpers are `actual inline` with `crossinline` lambdas: the block writes with the pooled writer, then the result is taken as a copy (`toByteArray`), drained into an Okio sink in one `write`, built into a `String`, or discarded. Each ends by resetting the buffer. The decode helpers reuse the reader: `resetSlice`, `reset(...)` or a source prepared by `prepareUtf8JsonSource`, run the block, and call `requireEndOfInput()` so trailing garbage is rejected. `createAtomicMap` returns a `ConcurrentHashMap` and `runSynchronized` uses `synchronized`.

## The code

```kotlin
@InternalGhostApi
actual inline fun ghostInternalEncodeWithWriter(
    crossinline block: (GhostJsonWriter) -> Unit
): ByteArray {
    val pair = acquireFlatWriterPair()
    block(pair.writer)
    val result = pair.byteWriter.toByteArray()
    pair.byteWriter.reset()
    return result
}
```

`ghostInternalEncodeToString` builds `String(pair.charWriter.array, 0, pair.charWriter.size)` directly from the char buffer, avoiding Okio segments. `ghostInternalUseFlatReader` wraps the bytes with `withPreparedUtf8Json`, which handles encoding detection before the reader is reset on the slice.

## Related

- [[Ghost]]: declares the `expect` functions this file implements
- [[WriterSinkPair]]: the pooled byte writer pair
- [[WriterStringPair]]: the pooled string writer pair
- [[GhostJsonFlatReader]]: reader pooled for in-memory byte input
- [[GhostJsonStringReader]]: reader pooled for `String` input
- [[GhostJsonReader]]: streaming reader pooled for Okio sources
- [[Ghost.nonJvm]]: Native and Wasm version using `@ThreadLocal` globals
