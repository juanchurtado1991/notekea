---
tags: [ghost-serialization, serialization, parser, apple]
source: ghost-serialization/src/appleMain/kotlin/com/ghost/serialization/parser/common/GhostSource.apple.kt
kind: expect/actual
---

# GhostSource.apple

The Apple (iOS, macOS and friends) `actual` of `createByteArraySource`, returning the standard `ByteArrayGhostSource`.

## What it is about

Every platform must provide an `actual` for the factory declared in [[GhostSource]]. On Apple targets nothing platform-specific improves on the generic byte-array source, so this file is a thin adapter: it takes the bytes and returns a source over them. It matters because it lets the shared readers build their input through one call regardless of target.

## How it works

`createByteArraySource(data)` constructs `ByteArrayGhostSource(data = data)` and returns it as the `GhostSource` interface. There is no copying, no charset trick and no lazy work: the returned object holds a reference to the array and answers `get`, `contentEquals`, `decodeToString` and the scanning methods against it.

Because `ByteArrayGhostSource` extends [[AbstractGhostSource]], string decoding on Apple goes through Kotlin's UTF-8 `decodeToString`. The JVM and Android actual can replace that with an ISO-8859-1 copy for pure-ASCII ranges because Java strings can be built directly from bytes; on Apple there is no equivalent shortcut in use here.

The declaration carries `@InternalGhostApi`: it is visible across modules for generated code, but not a stable API.

## The code

```kotlin
@InternalGhostApi
actual fun createByteArraySource(
    data: ByteArray
): GhostSource = ByteArrayGhostSource(data = data)
```

The body is identical to the one in [[GhostSource.wasmJs]]; the two are kept as separate files only because Kotlin Multiplatform needs an `actual` in each source set.

## Related

- [[GhostSource]]: the interface and `expect` factory implemented here.
- [[ByteArrayGhostSource]]: the class instantiated.
- [[GhostSource.wasmJs]]: the Wasm actual with the same body.
- [[GhostSource.jvmAndroid]]: the JVM and Android actual with a specialized subclass.
- [[GhostJsonFlatReader]]: a reader that builds sources through this factory.
