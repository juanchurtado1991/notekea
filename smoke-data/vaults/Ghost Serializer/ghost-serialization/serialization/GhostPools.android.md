---
tags: [ghost-serializer, serialization, platform, android, pooling]
source: ghost-serialization/src/androidMain/kotlin/com/ghost/serialization/GhostPools.android.kt
kind: expect/actual
---

# GhostPools.android

The Android actual of `getLocalPool`, which gives each thread its own buffer pool and the main thread a dedicated one.

## What it is about

Ghost reuses byte buffers through a tiered pool (`GhostPools`) so hot paths do not allocate. The pool must be per thread, because buffers are handed out without locking. On Android the main thread is by far the most common caller, so this actual gives it a fast path that avoids the `ThreadLocal` lookup.

## How it works

`getLocalPool` first compares the current thread with a cached reference to the main thread; if it matches, it returns a single shared `GhostPool` for the main thread. If the main thread has not been resolved yet, it asks `Looper.getMainLooper()` once (wrapped in a `try`, so a failure to get the looper just means no main-thread shortcut) and caches the thread. Every other thread gets a `GhostPool` stored in a `ThreadLocal`, created on first use.

The file uses `@file:JvmName("GhostPools_jvmKt")` so the generated class has the same name as the JVM one. The cached main thread reference is `@Volatile`; the `mainThreadResolved` flag is a plain variable.

## The code

```kotlin
private val pool = ThreadLocal<GhostPool>()
private val mainThreadInstance = GhostPool()

@Volatile
private var mainThreadRef: Thread? = null
private var mainThreadResolved = false

internal actual fun getLocalPool(): GhostPool {
    val currentThread = Thread.currentThread()
    val mainRef = mainThreadRef
    if (currentThread === mainRef) {
        return mainThreadInstance
    }
    // …
}
```

The last part of the function creates a `GhostPool` lazily when `pool.get()` returns `null` and stores it for the thread.

## Related

- [[GhostPools]]: declares `GhostPool`, the `expect` `getLocalPool` and the buffer acquire/release API
- [[GhostPools.jvm]]: plain-JVM actual that uses a `ThreadLocal.withInitial`
- [[GhostPools.nonJvm]]: Native and Wasm actual with a single instance
- [[Ghost.android]]: the Android file for registry discovery that shares this source set
