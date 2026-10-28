---
tags: [ghost-serializer, serialization, parser, swar, performance]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/bytes/GhostSwar.kt
kind: expect/actual
---

# GhostSwar

The common side of Ghost's "SIMD within a register" byte reading: an `expect` function that loads eight bytes into one `Long`, a portable fallback, and a table of length masks.

## What it is about

SWAR (SIMD within a register) means treating a 64-bit `Long` as eight parallel byte lanes, so one comparison inspects eight bytes. This file declares the single platform primitive, `ghostReadLong8`, that every byte-channel hot path builds on: field-name matching, whitespace skipping and string scanning. Each platform supplies its own `actual`, while the shared scalar assembly and the mask table live here.

## How it works

`ghostReadLong8` reads `LONG_BYTES` (8) bytes at an index. Its byte order is deliberately "unspecified": callers only ever compare two values produced by the same function, or a value against a byte-symmetric constant such as a run of spaces, so the order cancels out. That freedom lets the JVM use a native-order `VarHandle` view (one unaligned load), while Android and the other targets assemble the word byte by byte.

The length masks solve the partial-word problem. To compare only the first `n` bytes of a field name, the code needs a mask that keeps lanes `0 until n`. Instead of assuming a layout, the table is built by calling `ghostReadLong8` itself on an array whose first `n` bytes are `0xFF`. The mask therefore always agrees with whatever order the platform chose. It is a plain initializer, computed once, because it is indexed on every predicted-key compare and a getter would allocate on each access.

## The code

`ghostReadLong8` is the platform contract. Callers must guarantee `index + 8 <= data.size`.

```kotlin
internal expect fun ghostReadLong8(data: ByteArray, index: Int): Long
```

`ghostReadLong8Scalar` is shared by Android, Kotlin/Native and Kotlin/Wasm. It is `inline` so that each actual compiles to exactly the loop that used to be copied into every platform file, with no extra call on the hot path:

```kotlin
internal inline fun ghostReadLong8Scalar(
    data: ByteArray,
    index: Int
): Long = (data[index].toLong() and LONG_BYTE_MASK) or
    ((data[index + LONG_BYTE_OFFSET_1].toLong() and LONG_BYTE_MASK) shl SHIFT_8) or
    // …
    ((data[index + LONG_BYTE_OFFSET_7].toLong() and LONG_BYTE_MASK) shl SHIFT_56)
```

`ghostSWARLengthMasks` holds nine entries, for lengths 0 to 8:

```kotlin
internal val ghostSWARLengthMasks: LongArray = LongArray(LONG_BYTES + 1) { number ->
    if (number == 0) {
        0L
    } else {
        ghostReadLong8(
            data = ByteArray(LONG_BYTES) { index ->
                if (index < number) BYTE_MASK.toByte() else 0
            },
            index = 0
        )
    }
}
```

## Related

- [[GhostSwar.jvm]]: the JVM actual, a single `VarHandle` load.
- [[GhostSwar.android]]: the Android actual, using the scalar assembly.
- [[GhostSwar.nonJvm]]: the Kotlin/Native and Wasm actual, also scalar.
- [[GhostJsonScanConstants]]: supplies the byte offsets, shifts and `LONG_BYTES`.
- [[GhostJsonFlatReaderSubsystem]]: uses the masks for the predicted-key fast path.
- [[GhostCharSwar]]: the UTF-16 counterpart for the `String` reader, which needs no `expect`.
- [[StreamingGhostSource]]: uses `ghostReadLong8` to skip runs of spaces and clean string windows.
