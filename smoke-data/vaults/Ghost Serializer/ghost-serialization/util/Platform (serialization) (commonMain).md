---
tags: [ghost-serializer, serialization, platform]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/util/Platform.kt
kind: expect/actual
---

# Platform (serialization) (commonMain)

A four-line file that declares the `isJvm` property as an `expect` value.

## What it is about

It lets code in common source sets ask "am I on the JVM?" without a platform check of its own. The answer comes from a tiny actual in each platform group.

## How it works

There is nothing to run here: the file contains only the `expect val isJvm: Boolean` declaration and its KDoc. The JVM and Android group provides `true`, and the non-JVM group (Native and Wasm) provides `false`, both as plain constants set once.

Expect declarations of properties follow the same rule as functions: every target must provide exactly one actual, otherwise the build fails. That is why the JVM and Android group and the non-JVM group each carry a small file with the same package and property name.

## The code

```kotlin
/** True when running on the JVM. */
expect val isJvm: Boolean
```

## Related

- [[Platform (serialization) (jvmAndroidMain)]]: actual returning `true`
- [[Platform (serialization) (nonJvmMain)]]: actual returning `false`
