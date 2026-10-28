---
tags: [ghost-serializer, serialization, proto, pooling]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/GhostProtoPools.kt
kind: expect/actual
---

# GhostProtoPools

The common declaration of `ghostProtoInternalUseFlatReader`, the function that lends a pooled proto reader to a block of code.

## What it is about

Every proto3 decode in Ghost starts with a reader positioned on a byte array. Creating a new reader for each call would allocate on the hot path, so this file declares the platform-neutral contract: give me bytes, an offset and a length, and I will run your block with a reusable [[GhostProtoJsonFlatReader]]. It is the proto counterpart of the pooling helpers that already exist for the JSON and YAML channels, and it is marked `@InternalGhostApi` because only Ghost's own facades and integrations should call it.

## How it works

The declaration is an `expect fun`, so there is no body here. The JVM and Android source set and the native/wasm source set each provide an `actual` that keeps one reader per thread, resets it onto the requested slice and runs the block. The default arguments live on the `expect` side: `offset` defaults to 0 and `length` defaults to the rest of the array, so most callers pass only `bytes` and a lambda.

The generic return type `T` lets the caller return the decoded object straight out of the block without any intermediate holder, which keeps the pooled reader from escaping its scope.

## The code

Everything is in one declaration:

```kotlin
@InternalGhostApi
expect fun <T> ghostProtoInternalUseFlatReader(
    bytes: ByteArray,
    offset: Int = 0,
    length: Int = bytes.size - offset,
    block: (GhostProtoJsonFlatReader) -> T
): T
```

The `block` parameter is last so callers can use trailing-lambda syntax. The reader is only valid inside the block, because after it returns the same instance is reused for the next call on that thread.

## Related

- [[GhostProtoPools.jvmAndroid]]: the JVM and Android `actual`, backed by a `ThreadLocal`
- [[GhostProtoPools.nonJvm]]: the native and wasm `actual`, backed by a `@ThreadLocal` top-level variable
- [[GhostProtoJsonFlatReader]]: the reader type that is pooled and lent to the block
- [[GhostProto]]: the public facade that calls this function for every byte-array decode
- [[GhostPools]]: the equivalent pooling helper for the plain JSON channel
- [[GhostYamlPools]]: the equivalent pooling helper for the YAML channel
- [[InternalGhostApi]]: the opt-in annotation that guards this function
