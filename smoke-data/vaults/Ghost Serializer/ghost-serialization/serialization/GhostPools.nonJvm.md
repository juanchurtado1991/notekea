---
tags: [ghost-serializer, serialization, platform, native, wasm, pooling]
source: ghost-serialization/src/nonJvmMain/kotlin/com/ghost/serialization/GhostPools.nonJvm.kt
kind: expect/actual
---

# GhostPools.nonJvm

The Native and Wasm actual of `getLocalPool`: one `GhostPool` per thread on Native and a single global on Wasm.

## What it is about

`GhostPools` needs the pool of the current thread. These targets do it with a top-level variable instead of a `ThreadLocal` object.

## How it works

A private variable `poolInstance` holds a `GhostPool`, annotated with `@ThreadLocal`. On Kotlin/Native each thread sees its own copy; on Kotlin/Wasm in the browser the annotation has no effect and the variable is a plain single-threaded global, as the comment in the source says. `getLocalPool()` just returns it.

Pools are not shared between threads on Native either, so the same lock-free borrow and return pattern as on the JVM is safe. On Wasm there is only one thread, so one global instance is enough.

## The code

```kotlin
@ThreadLocal
private var poolInstance = GhostPool()

internal actual fun getLocalPool(): GhostPool = poolInstance
```

## Related

- [[GhostPools]]: declares `GhostPool` and `getLocalPool`
- [[GhostPools.jvm]]: JVM actual with a `ThreadLocal`
- [[GhostPools.android]]: Android actual with a main-thread shortcut
- [[Ghost.nonJvm]]: sibling file with the other non-JVM actuals
