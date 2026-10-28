---
tags: [ghost-serializer, serialization, platform, native, wasm, pooling]
source: ghost-serialization/src/nonJvmMain/kotlin/com/ghost/serialization/Ghost.nonJvm.kt
kind: expect/actual
---

# Ghost.nonJvm

The Native and Wasm actuals of the encode and decode helpers, using `@ThreadLocal` globals instead of `ThreadLocal` objects.

## What it is about

This is the non-JVM version of what `Ghost.jvmAndroid` does for the JVM: it supplies the pooled readers and writers that `Ghost` calls, so repeated encodes and decodes reuse the same objects. It also supplies the Native and Wasm implementation of registry discovery and the registration hint.

## How it works

The reader and writer instances are top-level variables marked `@ThreadLocal`, which on Kotlin/Native gives one per thread and on Wasm is simply a global. They are created lazily on first use. `acquireFlatWriterPair` and `acquireStringWriterPair` fetch or create the pair, then reset the writer and its buffer. The encode helpers and decode helpers follow the same structure as on the JVM (encode into the pooled buffer, then copy, drain to a sink, build a string with `concatToString`, or discard; decode by resetting the pooled reader and checking `requireEndOfInput`).

`discoverRegistries()` returns an empty list, because there is no ServiceLoader or reflection. `serializerRegistrationHint` is the message appended to "not found" errors, explaining that the Gradle plugin `com.ghostserializer.ghost` links serializers at compile time, and that without the plugin, or with `autoRegistration = false`, the app must call `Ghost.addRegistry(...)` at startup.

## The code

```kotlin
@ThreadLocal
private var cachedFlatReader: GhostJsonFlatReader? = null

actual fun discoverRegistries(): Iterable<GhostRegistry> = emptyList()

@PublishedApi
internal fun acquireFlatWriterPair(): WriterSinkPair {
    val pair = cachedWriterPair
        ?: WriterSinkPair()
            .also { cachedWriterPair = it }

    pair.writer.reset()
    pair.byteWriter.reset()
    return pair
}
```

## Related

- [[Ghost]]: declares the `expect` functions
- [[Ghost.jvmAndroid]]: JVM and Android version using `ThreadLocal` objects
- [[WriterSinkPair]]: byte writer pair cached per thread
- [[WriterStringPair]]: string writer pair cached per thread
- [[GhostJsonFlatReader]]: flat reader cached per thread
- [[GhostPlugin]]: the Gradle plugin that applies the compiler plugin that creates the compile-time link
