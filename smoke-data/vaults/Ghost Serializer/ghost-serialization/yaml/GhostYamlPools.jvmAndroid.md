---
tags: [ghost-serialization, serialization, yaml, pooling]
source: ghost-serialization/src/jvmAndroidMain/kotlin/com/ghost/serialization/yaml/GhostYamlPools.jvmAndroid.kt
kind: expect/actual
---

# GhostYamlPools.jvmAndroid

The JVM and Android implementation of the YAML reader/writer pool: one reusable reader and one reusable writer per thread.

## What it is about

Every YAML decode or encode needs a `GhostYamlFlatReader` or a `GhostYamlWriter` plus its byte buffer. Creating them per call would allocate on the hot path, so Ghost keeps one set per thread and hands it to a lambda. This file is the `jvmAndroidMain` `actual` of the functions declared in [[GhostYamlPools]]; it uses `ThreadLocal`. The Kotlin/Native and wasm variant is [[GhostYamlPools.nonJvm]].

## How it works

1. The first call on a thread finds the `ThreadLocal` empty, builds a new object and stores it.
2. Later calls on the same thread find it and call `reset` on it instead: the reader is pointed at the new bytes with `reset(newData = bytes)`, the writer and its buffer are cleared.
3. The caller's `block` runs with that instance and its result is returned.

Because the instance is per thread, no locking is needed. The price is that the reader and writer must not escape the `block` (they would be reset by the next call), and that `block` must not call the same pool function re-entrantly on the same thread, since the inner call would reset the object the outer call is still using.

## The code

Three thread locals hold the cached objects. The writer and its backing `FlatByteArrayWriter` are stored separately, and both must be present to be reused:

```kotlin
private val flatReaderPool = ThreadLocal<GhostYamlFlatReader>()
private val flatWriterPool = ThreadLocal<GhostYamlWriter>()
private val flatWriterBufferPool = ThreadLocal<FlatByteArrayWriter>()
```

`ghostYamlInternalUseFlatReader` creates or resets the reader:

```kotlin
var reader = flatReaderPool.get()
if (reader == null) {
    reader = GhostYamlFlatReader(rawData = bytes)
    flatReaderPool.set(reader)
} else {
    reader.reset(newData = bytes)
}
return block(reader)
```

`ghostYamlInternalUseFlatWriter` does the same for the pair: if either is missing it builds a fresh `FlatByteArrayWriter` and a `GhostYamlWriter(flatBuffer = buffer)`; otherwise it calls `writer.reset()` and `buffer.reset()`. The block receives both so the caller can read the encoded bytes from the buffer.

## Related

- [[GhostYamlPools]]: the `expect` declarations this file implements.
- [[GhostYamlPools.nonJvm]]: the actual for native and wasm targets.
- [[GhostYamlFlatReader]]: the reader pooled here.
- [[GhostYamlWriter]]: the writer pooled here.
- [[FlatByteArrayWriter]]: the byte buffer behind the writer.
- [[GhostYamlExtensions]]: public entry points that go through the pool.
- [[GhostProtoPools.jvmAndroid]]: the same pooling pattern for the Protobuf channel.
