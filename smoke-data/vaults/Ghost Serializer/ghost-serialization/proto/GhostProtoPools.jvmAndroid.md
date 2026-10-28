---
tags: [ghost-serializer, serialization, proto, pooling]
source: ghost-serialization/src/jvmAndroidMain/kotlin/com/ghost/serialization/proto/GhostProtoPools.jvmAndroid.kt
kind: expect/actual
---

# GhostProtoPools.jvmAndroid

The JVM and Android implementation of `ghostProtoInternalUseFlatReader`, which keeps one proto reader per thread.

## What it is about

This is the `actual` half of the pooling contract declared in [[GhostProtoPools]]. On the JVM and Android, threads are real and cheap to key on, so a `ThreadLocal` holds a single [[GhostProtoJsonFlatReader]] for each thread. It exists so that decoding a proto3 JSON message does not allocate a new reader (and its internal buffers) every time.

## How it works

On each call the function reads the thread-local; if it is empty it builds a reader around the incoming `bytes` and stores it. It then calls `resetSlice` with the buffer, offset and length so the reader looks at the new data, runs the caller's block, and finally calls `requireEndOfInput()` on the reader.

That last step is a correctness check as well as a cleanup: after the block has decoded one value, any trailing non-whitespace input is reported as an error instead of being silently ignored. The trade-off is that the reader is shared, so the block must not keep a reference to it or call back into the same function re-entrantly with data it still needs.

## The code

```kotlin
private val flatProtoReaderPool = ThreadLocal<GhostProtoJsonFlatReader>()
```

```kotlin
    val reader = flatProtoReaderPool.get()
        ?: GhostProtoJsonFlatReader(rawData = bytes)
            .also { flatProtoReaderPool.set(it) }

    reader.resetSlice(
        buffer = bytes,
        offset = offset,
        length = length
    )

    return block(reader).also { reader.requireEndOfInput() }
}
```

The `?:` fallback creates the reader lazily the first time a thread needs one. Note that `block(reader).also { ... }` runs the end-of-input check only after the block returned a value.

## Related

- [[GhostProtoPools]]: the `expect` declaration this file implements
- [[GhostProtoPools.nonJvm]]: the sibling `actual` for native and wasm targets
- [[GhostProtoJsonFlatReader]]: the reader that is cached and reset here
- [[GhostPools.jvm]]: the JVM actual of the generic JSON pool, built on the same idea
- [[GhostProto]]: the facade that reaches this code through the common function
