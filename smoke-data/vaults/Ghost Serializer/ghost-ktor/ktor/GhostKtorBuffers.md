---
tags: [ghost-serializer, ktor, buffers, pooling]
source: ghost-ktor/src/commonMain/kotlin/com/ghost/serialization/ktor/GhostKtorBuffers.kt
kind: object
---

# GhostKtorBuffers

An internal helper that reads a Ktor body into a pooled byte buffer so converters can parse it without extra allocations.

## What it is about

All three Ktor converters (JSON, proto3 JSON and YAML) need the same first step: turn an asynchronous `ByteReadChannel` into a contiguous `ByteArray` that Ghost's readers can scan. `GhostKtorBuffers.readToScratch` is that shared loop, so the logic lives in one place. It is internal to `ghost-ktor`.

## How it works

The function takes a pooled scratch buffer from the core runtime with `acquireScratchBuffer`, starting at `RESPONSE_SCRATCH_INITIAL_SIZE`. It then loops: if the buffer is full, it calls `growScratchBuffer` (which doubles it, copying the used bytes); then `readAvailable` fills the rest and returns `-1` at the end of the channel. When the loop ends, the code calls the caller's `block` with the buffer and the filled length. A `finally` always gives the buffer back with `releaseScratchBuffer`, even on exceptions.

If the declared charset is not UTF-8, the bytes are decoded to a string and re-encoded to UTF-8, and `block` receives that fresh array. Ghost's readers expect UTF-8, so this step keeps them correct for other encodings, at the cost of an allocation only in that case.

The function is `suspend inline`, so the block is inlined into the converter and a `return` inside it (as used for an empty body) returns from the converter's function.

## The code

```kotlin
var scratch = acquireScratchBuffer(minSize = RESPONSE_SCRATCH_INITIAL_SIZE)
try {
    var offset = 0
    while (true) {
        if (offset == scratch.size) {
            scratch = growScratchBuffer(scratch = scratch, usedBytes = offset)
        }

        val read = content.readAvailable(scratch, offset, scratch.size - offset)
        if (read == -1) break
        offset += read
    }
    // …
} finally {
    releaseScratchBuffer(buffer = scratch)
}
```

## Related

- [[GhostContentConverter]]: uses it to read JSON bodies.
- [[GhostProtoContentConverter]]: uses it for proto3 JSON bodies.
- [[GhostYamlContentConverter]]: uses it for YAML bodies.
- [[GhostBufferGrowth]]: the growth policy behind the scratch buffer.
- [[GhostPools]]: the pooling layer that supplies and takes back buffers.
- [[GhostRetrofitBuffers]]: the Retrofit counterpart.
- [[InternalGhostApi]]: the opt-in the helpers require.
