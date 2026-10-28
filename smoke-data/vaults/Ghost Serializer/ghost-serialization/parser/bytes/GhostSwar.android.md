---
tags: [ghost-serializer, serialization, parser, swar, android]
source: ghost-serialization/src/androidMain/kotlin/com/ghost/serialization/parser/bytes/GhostSwar.android.kt
kind: expect/actual
---

# GhostSwar.android

The Android implementation of `ghostReadLong8`, which assembles the eight-byte word by hand.

## What it is about

[[GhostSwar]] declares `ghostReadLong8` as an `expect` function. The JVM actual uses a `VarHandle`, but this class exists for Android, where that is not an option. The file is only a few lines: it forwards to the shared scalar routine.

## How it works

Android's minimum SDK in this project is 21, while `VarHandle` views over `byte[]` arrived in API 26. Ghost therefore cannot rely on them and reads each of the eight bytes, masks it to an unsigned value, shifts it into place and ORs the results together. The comment in the source also notes why the byte order does not matter: the function only feeds symmetric comparisons, so any consistent layout gives the same answers.

The routine it forwards to, `ghostReadLong8Scalar`, is declared `inline` in the common file. That choice means the Android actual compiles to the same bytecode as a hand-written loop in this file, so sharing the code between Android, Native and Wasm costs no extra call on the hot path.

## The code

The whole file is one declaration:

```kotlin
internal actual fun ghostReadLong8(
    data: ByteArray,
    index: Int
): Long = ghostReadLong8Scalar(
    data = data,
    index = index
)
```

Nothing else happens here. The bounds contract is the same as on every platform: the caller guarantees that `index + 8` does not pass the end of the array.

## Related

- [[GhostSwar]]: holds the `expect` function and `ghostReadLong8Scalar`.
- [[GhostSwar.jvm]]: the JVM actual that uses a `VarHandle` instead.
- [[GhostSwar.nonJvm]]: the Native and Wasm actual, identical in behavior to this one.
- [[GhostJsonFlatReader]]: uses the function in `skipWhitespace` to swallow runs of spaces.
