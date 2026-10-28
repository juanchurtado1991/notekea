---
tags: [ghost-serializer, serialization, parser, swar, native]
source: ghost-serialization/src/nonJvmMain/kotlin/com/ghost/serialization/parser/bytes/GhostSwar.nonJvm.kt
kind: expect/actual
---

# GhostSwar.nonJvm

The `ghostReadLong8` actual shared by Kotlin/Native (iOS and other native targets) and Kotlin/Wasm.

## What it is about

Every platform has to provide the eight-byte read that [[GhostSwar]] declares. The JVM uses a `VarHandle`; this source set, which covers the non-JVM targets, uses the portable scalar loop instead. It is the shortest of the three actuals and exists so that the common code can stay free of platform checks.

## How it works

The function forwards to `ghostReadLong8Scalar`, an `inline` function in the common file that reads the eight bytes one at a time and combines them with shifts. A comment in the source explains the decision: on these targets, including Safari for the Wasm byte channel, the scalar version benchmarked competitively, so no special intrinsic is used. As elsewhere, byte order is irrelevant, because the function only supports symmetric comparisons such as predicted-key matching and space-run detection.

Since the helper is inline, the call disappears at compile time and the result is the same machine code a hand-written loop would give.

## The code

```kotlin
internal actual fun ghostReadLong8(
    data: ByteArray,
    index: Int
): Long = ghostReadLong8Scalar(
    data = data,
    index = index
)
```

The caller must guarantee `index + 8 <= data.size`, as on the other platforms.

## Related

- [[GhostSwar]]: the `expect` declaration, the scalar helper and the length masks.
- [[GhostSwar.jvm]]: the JVM actual with a `VarHandle` view.
- [[GhostSwar.android]]: the Android actual, written the same way as this one.
- [[GhostSource.apple]]: a platform source in the same area of the parser, for Apple targets.
- [[GhostSource.wasmJs]]: the Wasm platform source, one of the targets this actual serves.
