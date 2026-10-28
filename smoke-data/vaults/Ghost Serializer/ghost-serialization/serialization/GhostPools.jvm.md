---
tags: [ghost-serializer, serialization, platform, jvm, pooling]
source: ghost-serialization/src/jvmMain/kotlin/com/ghost/serialization/GhostPools.jvm.kt
kind: expect/actual
---

# GhostPools.jvm

The JVM actual of `getLocalPool`: one `GhostPool` per thread, created lazily.

## What it is about

`GhostPools` hands out reusable byte buffers from a per-thread pool. This file tells the JVM how to find the pool for the current thread. Because each thread owns its pool, buffers can be taken and returned without any locking.

## How it works

It is a single `ThreadLocal` created with `ThreadLocal.withInitial { GhostPool() }`, so the pool is built the first time a thread asks for it. Unlike the Android version, there is no special main-thread handling.

Because every thread gets its own `GhostPool`, a buffer taken from the pool can be used without locks, and a buffer released on one thread lands in that thread's pool. The cost is memory: each thread that encodes or decodes may keep up to one buffer per size tier alive.

## The code

```kotlin
private val pool = ThreadLocal.withInitial { GhostPool() }

internal actual fun getLocalPool(): GhostPool = pool.get()
```

## Related

- [[GhostPools]]: declares `GhostPool` and the buffer API that calls `getLocalPool`
- [[GhostPools.android]]: Android actual with a main-thread shortcut
- [[GhostPools.nonJvm]]: single-instance or `@ThreadLocal` actual for Native and Wasm
