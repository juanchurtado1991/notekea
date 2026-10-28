---
tags: [ghost-serializer, serialization, writer, strings, platform, native, wasm]
source: ghost-serialization/src/nonJvmMain/kotlin/com/ghost/serialization/writer/strings/CharArrayCopy.nonJvm.kt
kind: expect/actual
---

# CharArrayCopy.nonJvm

The Native and Wasm actual of `copyRangeToCharArray`, which delegates to a shared loop.

## What it is about

These targets have no allocation-free bulk copy from a `String` into a `CharArray`, so the function uses a plain loop. The loop itself lives in common code as an inline helper.

## How it works

The actual calls `copyRangeToCharArrayByLoop` with the same arguments. Because that helper is `inline`, the loop is compiled into this function, which keeps the behaviour of a per-platform copy while sharing the source. No temporary arrays are allocated.

The file sits in the shared non-JVM source set, so one definition serves both Native and Wasm. The matching JVM and Android file avoids the loop altogether.

## The code

```kotlin
internal actual fun String.copyRangeToCharArray(
    dest: CharArray,
    destOffset: Int,
    startIndex: Int,
    endIndex: Int
) = copyRangeToCharArrayByLoop(
    dest = dest,
    destOffset = destOffset,
    startIndex = startIndex,
    endIndex = endIndex
)
```

## Related

- [[CharArrayCopy]]: declares the `expect` function and the loop
- [[CharArrayCopy.jvmAndroid]]: JVM and Android actual with a bulk copy
- [[FlatCharArrayWriter]]: uses it for string writes
