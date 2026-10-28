---
tags: [ghost-serialization, serialization, yaml, pooling]
source: ghost-serialization/src/nonJvmMain/kotlin/com/ghost/serialization/yaml/GhostYamlPools.nonJvm.kt
kind: expect/actual
---

# GhostYamlPools.nonJvm

The Kotlin/Native and wasm implementation of the YAML reader/writer pool, based on thread-local top-level variables.

## What it is about

It is the `nonJvmMain` `actual` of the two functions declared in [[GhostYamlPools]]. Like its JVM sibling [[GhostYamlPools.jvmAndroid]], it keeps one reader and one writer around so that decoding and encoding YAML do not allocate new parsers on every call. Only the way of making the cache "per thread" differs.

## How it works

Instead of `java.lang.ThreadLocal`, this file uses `kotlin.native.concurrent.ThreadLocal`, an annotation placed on top-level `var`s. Each thread of a native program then sees its own copy of the variable, starting as `null`.

- On the first call the variable is `null`: a new object is created and stored.
- On later calls the cached object is reused. The reader is repointed with `reset(newData = bytes)`; the writer and its buffer are cleared with `reset()`.
- The `block` then runs with the cached instance.

As with the JVM version, the objects must not be kept after `block` returns, and nested use on the same thread would reset an object still in use.

## The code

The three caches are annotated top-level variables:

```kotlin
@ThreadLocal
private var cachedReader: GhostYamlFlatReader? = null

@ThreadLocal
private var cachedWriter: GhostYamlWriter? = null

@ThreadLocal
private var cachedWriterBuffer: FlatByteArrayWriter? = null
```

The writer function builds the writer and buffer as a pair, and resets both when they exist:

```kotlin
if (writer == null || buffer == null) {
    buffer = FlatByteArrayWriter()
    writer = GhostYamlWriter(flatBuffer = buffer)
    cachedWriter = writer
    cachedWriterBuffer = buffer
} else {
    writer.reset()
    buffer.reset()
}
return block(writer, buffer)
```

## Related

- [[GhostYamlPools]]: the `expect` declarations implemented here.
- [[GhostYamlPools.jvmAndroid]]: the JVM and Android actual using `ThreadLocal`.
- [[GhostYamlFlatReader]]: the reader that is cached.
- [[GhostYamlWriter]]: the writer that is cached.
- [[FlatByteArrayWriter]]: the buffer paired with the writer.
- [[GhostProtoPools.nonJvm]]: the same approach for Protobuf.
