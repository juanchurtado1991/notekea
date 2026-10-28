---
tags: [ghost-serializer, serialization, writer, strings, platform]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/writer/strings/CharArrayCopy.kt
kind: expect/actual
---

# CharArrayCopy

The `expect` declaration of `copyRangeToCharArray`, plus the manual loop used on platforms without a native bulk copy.

## What it is about

The char writer often copies part of a `String` into its `CharArray`. On the JVM and Android there is a bulk copy that allocates nothing. On other targets a simple loop does the same job. This file declares the function and holds the shared loop.

## How it works

`String.copyRangeToCharArray(dest, destOffset, startIndex, endIndex)` copies the characters in `[startIndex, endIndex)` starting at `destOffset`. The JVM actual calls `String.toCharArray` with a destination. The Native and Wasm actuals call `copyRangeToCharArrayByLoop`, an `inline` function with a `while` loop (inline, so each actual contains its own copy of the loop, as it did before it was shared).

## The code

```kotlin
@Suppress("NOTHING_TO_INLINE")
internal inline fun String.copyRangeToCharArrayByLoop(
    dest: CharArray,
    destOffset: Int,
    startIndex: Int,
    endIndex: Int
) {
    var index = startIndex
    var destIndex = destOffset
    while (index < endIndex) {
        dest[destIndex++] = this[index++]
    }
}
```

## Related

- [[CharArrayCopy.jvmAndroid]]: JVM and Android actual using `toCharArray`
- [[CharArrayCopy.nonJvm]]: Native and Wasm actual using the loop
- [[FlatCharArrayWriter]]: the main user of the copy
