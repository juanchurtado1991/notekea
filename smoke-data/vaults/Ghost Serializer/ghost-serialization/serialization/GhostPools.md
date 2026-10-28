---
tags: [ghost-serializer, serialization, pooling, performance]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/GhostPools.kt
kind: functions
---

# GhostPools

The tiered, per-thread pool of reusable byte buffers that Ghost uses to avoid allocating on its hot paths.

## What it is about

Encoding and decoding need temporary byte arrays: scratch space for numbers, copies of network bodies, buffers for reading a source. Allocating them per call would create garbage on every request. `GhostPools` lets code borrow a buffer of roughly the size it needs and return it. It is used by the writers, the readers, `Ghost` itself and the Retrofit, Ktor and Spring integrations.

## How it works

There are six size tiers: the default scratch size (`SCRATCH_BUFFER_SIZE`), then 1 KiB, 16 KiB, 64 KiB, 512 KiB and 4 MiB. `getLocalPool()` is an `expect` function: each platform supplies the pool for the current thread (a `ThreadLocal` on the JVM, a dedicated main-thread pool on Android, a single instance on Native and Wasm), so no locking is needed.

`acquireScratchBuffer(minSize)` picks the smallest tier that fits. If the pool holds a buffer of that tier, it is taken out of the pool (the slot is set to `null`) and returned; otherwise a new array of exactly the tier size is allocated. A request larger than 4 MiB gets an exact-size array that will never be pooled. `releaseScratchBuffer` stores the buffer back by its size, and ignores sizes that are not a tier. Each tier holds at most one buffer, so a nested acquire in the same tier simply allocates.

## The code

```kotlin
internal class GhostPool {
    var scratch: ByteArray? = null
    var small: ByteArray? = null
    var medium: ByteArray? = null
    var large: ByteArray? = null
    var xlarge: ByteArray? = null
    var xxlarge: ByteArray? = null
}

internal expect fun getLocalPool(): GhostPool
```

- `RESPONSE_SCRATCH_INITIAL_SIZE` is 512 KiB, equal to a tier, so response buffers go back into the pool.
- `growScratchBuffer(scratch, usedBytes)` acquires a buffer twice as large, copies the used part, and releases the old one. The Retrofit and Ktor read loops share it.

## Related

- [[GhostPools.jvm]]: JVM actual with a `ThreadLocal`
- [[GhostPools.android]]: Android actual with a main-thread shortcut
- [[GhostPools.nonJvm]]: Native and Wasm actual
- [[Ghost]]: uses the pool when reading a `BufferedSource`
- [[GhostJsonWriter]]: acquires its scratch buffer from the pool
- [[GhostBufferGrowth]]: another helper for growing buffers
- [[GhostRetrofitBuffers]]: integration helper that reads bodies into pooled buffers
