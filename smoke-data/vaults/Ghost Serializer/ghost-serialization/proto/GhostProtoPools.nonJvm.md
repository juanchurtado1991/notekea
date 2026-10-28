---
tags: [ghost-serializer, serialization, proto, pooling]
source: ghost-serialization/src/nonJvmMain/kotlin/com/ghost/serialization/proto/GhostProtoPools.nonJvm.kt
kind: expect/actual
---

# GhostProtoPools.nonJvm

The native and wasm implementation of `ghostProtoInternalUseFlatReader`, caching one proto reader in a thread-local variable.

## What it is about

It is the second `actual` of the contract declared in [[GhostProtoPools]], used on Kotlin/Native and wasm where `java.lang.ThreadLocal` does not exist. Its job is identical to the JVM version: hand out a reusable [[GhostProtoJsonFlatReader]] so that proto3 JSON decoding stays allocation-light.

## How it works

A private top-level `var cachedProtoReader` annotated with `kotlin.native.concurrent.ThreadLocal` plays the role of the pool. On Kotlin/Native the annotation gives every thread its own copy of the variable; on single-threaded targets such as wasm it is simply a global. The function creates the reader on first use, calls `resetSlice` with the bytes, offset and length, runs the block and then calls `requireEndOfInput()` so that trailing garbage is rejected.

Compared with the JVM variant there is no `get`/`set` call and no boxing: the nullable variable is read and written directly, which is both shorter and a little cheaper.

## The code

```kotlin
@ThreadLocal
private var cachedProtoReader: GhostProtoJsonFlatReader? = null
```

```kotlin
    val reader = cachedProtoReader
        ?: GhostProtoJsonFlatReader(rawData = bytes)
            .also { cachedProtoReader = it }

    reader.resetSlice(
        buffer = bytes,
        offset = offset,
        length = length
    )

    return block(reader).also { reader.requireEndOfInput() }
}
```

The body is otherwise identical to the one in [[GhostProtoPools.jvmAndroid]], which makes the two easy to keep in sync.

## Related

- [[GhostProtoPools]]: the `expect` declaration this file implements
- [[GhostProtoPools.jvmAndroid]]: the JVM and Android counterpart using `ThreadLocal`
- [[GhostProtoJsonFlatReader]]: the reader that is cached and reset here
- [[GhostPools.nonJvm]]: the equivalent pool for the generic JSON channel on native and wasm
- [[GhostYamlPools.nonJvm]]: the equivalent pool for the YAML channel on native and wasm
