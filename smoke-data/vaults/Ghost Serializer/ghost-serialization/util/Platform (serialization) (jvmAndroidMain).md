---
tags: [ghost-serializer, serialization, platform, jvm, android]
source: ghost-serialization/src/jvmAndroidMain/kotlin/com/ghost/serialization/util/Platform.kt
kind: expect/actual
---

# Platform (serialization) (jvmAndroidMain)

The JVM and Android actual of the `isJvm` flag, which is `true`.

## What it is about

Some shared code needs to know whether it is running on the JVM, for example to choose between behaviors that exist only there. `isJvm` is a constant declared with `expect` in common code, and each platform group says which value it has.

## How it works

There is no logic. The JVM and Android source set sets the value to `true` at class load time. The non-JVM source set (Native and Wasm) sets it to `false`.

Since the value is a top-level `val` with an initializer, it is initialized once with the generated class. Callers in common code can branch on it, and the other group of targets (Native and Wasm) answer `false` through their own file.

## The code

```kotlin
actual val isJvm: Boolean = true
```

## Related

- [[Platform (serialization) (commonMain)]]: declares the `expect` property
- [[Platform (serialization) (nonJvmMain)]]: non-JVM actual with the value `false`
