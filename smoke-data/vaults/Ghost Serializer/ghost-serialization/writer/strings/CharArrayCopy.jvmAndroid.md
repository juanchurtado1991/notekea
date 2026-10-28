---
tags: [ghost-serializer, serialization, platform, jvm, android, writer]
source: ghost-serialization/src/jvmAndroidMain/kotlin/com/ghost/serialization/writer/strings/CharArrayCopy.jvmAndroid.kt
kind: expect/actual
---

# CharArrayCopy.jvmAndroid

The JVM and Android actual of `copyRangeToCharArray`, which copies part of a `String` into a `CharArray`.

## What it is about

The string JSON writer puts text into a `FlatCharArrayWriter` buffer. Copying characters with a loop is slower than a bulk copy, so on the JVM and Android this actual uses the platform's native copy.

## How it works

It calls `String.toCharArray(destination, destinationOffset, startIndex, endIndex)`, which copies directly into the existing array (a single `System.arraycopy` underneath) and allocates nothing. Native and Wasm cannot do this, so they use a manual loop.

The function is an extension on `String`, so a call site reads as `text.copyRangeToCharArray(...)`. It adds no checks of its own beyond what `toCharArray` does for its index arguments.

## The code

```kotlin
internal actual fun String.copyRangeToCharArray(
    dest: CharArray,
    destOffset: Int,
    startIndex: Int,
    endIndex: Int
) {
    toCharArray(
        destination = dest,
        destinationOffset = destOffset,
        startIndex = startIndex,
        endIndex = endIndex
    )
}
```

## Related

- [[CharArrayCopy]]: declares the `expect` function and the shared loop
- [[CharArrayCopy.nonJvm]]: Native and Wasm actual that uses the loop
- [[FlatCharArrayWriter]]: the writer that uses this copy for strings
