---
tags: [ghost-serializer, serialization, platform, native, wasm]
source: ghost-serialization/src/nonJvmMain/kotlin/com/ghost/serialization/util/Platform.kt
kind: expect/actual
---

# Platform (serialization) (nonJvmMain)

The Native and Wasm actual of the `isJvm` flag, which is `false`.

## What it is about

It answers the common `isJvm` question for every non-JVM target in one place, since the same value applies to Kotlin/Native and Kotlin/Wasm.

## How it works

The actual is a single constant assignment evaluated at startup. There are no branches and nothing to allocate.

The source set groups Kotlin/Native (including Apple targets) and Kotlin/Wasm, so a single file covers all of them. The JVM and Android group has the matching file that returns `true`.

## The code

```kotlin
actual val isJvm: Boolean = false
```

## Related

- [[Platform (serialization) (commonMain)]]: declares the `expect` property
- [[Platform (serialization) (jvmAndroidMain)]]: actual returning `true`
