---
tags: [ghost-serialization, serialization, parser, wasm]
source: ghost-serialization/src/wasmJsMain/kotlin/com/ghost/serialization/parser/common/GhostSource.wasmJs.kt
kind: expect/actual
---

# GhostSource.wasmJs

The Wasm-JS `actual` of `createByteArraySource`: it wraps a byte array in the plain `ByteArrayGhostSource`.

## What it is about

Ghost's byte readers talk to their input through the `GhostSource` abstraction. The factory `createByteArraySource(data)` is declared `expect` in [[GhostSource]] so each platform can return the best implementation. For Wasm there is no platform-specific trick available, so this file returns the generic one.

## How it works

The function takes a `ByteArray` and returns `ByteArrayGhostSource(data = data)` typed as `GhostSource`. Nothing is copied or validated; the source reads the array in place. All decoding is therefore done by the common implementation, using Kotlin's `decodeToString` for UTF-8 ranges.

Contrast this with the JVM and Android actual, which returns a subclass that decodes 7-bit ASCII ranges with the ISO-8859-1 charset to skip validation. Apple targets share the same trivial actual as Wasm. The function is annotated `@InternalGhostApi`, meaning it is public so generated code and other modules can reach it, but it is not part of the supported public surface.

## The code

```kotlin
@InternalGhostApi
actual fun createByteArraySource(
    data: ByteArray
): GhostSource = ByteArrayGhostSource(data = data)
```

That is the whole file apart from the package and imports. Its value is structural: because the factory is an `expect` function, common readers such as [[GhostJsonFlatReader]] can call `createByteArraySource` without knowing which platform they are on, and platforms can later specialize without touching the readers.

## Related

- [[GhostSource]]: declares the `expect` factory and the interface being created.
- [[ByteArrayGhostSource]]: the concrete class returned here.
- [[GhostSource.jvmAndroid]]: the JVM and Android actual, which returns a faster subclass.
- [[GhostSource.apple]]: the Apple actual, identical to this one.
- [[GhostJsonFlatReader]]: one of the callers that builds a source from a byte array.
