---
tags: [ghost-serializer, serialization, parser, swar, jvm]
source: ghost-serialization/src/jvmMain/kotlin/com/ghost/serialization/parser/bytes/GhostSwar.jvm.kt
kind: expect/actual
---

# GhostSwar.jvm

The JVM implementation of `ghostReadLong8`, which turns eight bytes of a `ByteArray` into a `Long` with a single memory load.

## What it is about

The common declaration in [[GhostSwar]] needs a fast way to read eight bytes at once. On the JVM the best tool is a `VarHandle` that views a `byte[]` as a `long[]`. This file creates that view once and exposes it through the `actual` function used by all the byte-channel SWAR code.

## How it works

`MethodHandles.byteArrayViewVarHandle` produces a handle that reads a `Long` out of a byte array at an arbitrary byte index. Ghost asks for the platform's native byte order, so the JIT can compile the access to one load instruction on x86 or ARM. The plain `get` access mode permits unaligned reads, which is what makes an arbitrary `index` safe and cheap; no alignment padding or copying is needed.

Because the result is in native order, a value read here has an unspecified byte layout. That is fine for Ghost: it only compares such values with other values from the same function, or with symmetric constants, so the layout never leaks. The handle lives in a private top-level `val`, so it is created once when the class loads and then treated as a constant by the JIT.

## The code

The handle:

```kotlin
private val LONG_VIEW = MethodHandles.byteArrayViewVarHandle(
    LongArray::class.java,
    ByteOrder.nativeOrder(),
)
```

And the `actual`, which is one cast around the handle's `get`:

```kotlin
internal actual fun ghostReadLong8(
    data: ByteArray,
    index: Int
): Long = LONG_VIEW.get(data, index) as Long
```

The caller is responsible for bounds: the VarHandle throws if fewer than eight bytes remain, so the hot paths check `index + 8 <= size` first.

## Related

- [[GhostSwar]]: the `expect` declaration and the shared scalar fallback.
- [[GhostSwar.android]]: the Android sibling, which cannot use a VarHandle because of its minimum API level.
- [[GhostSwar.nonJvm]]: the sibling for Kotlin/Native and Wasm.
- [[GhostJsonFlatReaderSubsystem]]: the field selector that calls `ghostReadLong8` on every key.
