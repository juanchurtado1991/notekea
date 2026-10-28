---
tags: [ghost-serializer, serialization, platform, apple, concurrency]
source: ghost-serialization/src/appleMain/kotlin/com/ghost/serialization/Ghost.apple.kt
kind: expect/actual
---

# Ghost.apple

The Apple (iOS, watchOS and similar) actuals for `createAtomicMap` and `runSynchronized`.

## What it is about

`Ghost` guards its caches and registry sets with these two primitives. On Apple Kotlin/Native targets, objects can be shared between threads, so real synchronization is required. This file implements it with the Objective-C runtime's lock API.

## How it works

`runSynchronized` calls `objc_sync_enter(lock)`, runs the block, and always calls `objc_sync_exit(lock)` in a `finally`. `createAtomicMap` returns an `AppleConcurrentMap`, which uses the same primitive internally. Compare the JVM version (`ConcurrentHashMap`, `synchronized`) and Wasm (nothing at all).

`objc_sync_enter` is re-entrant on the same thread, so a function that holds the lock can call another synchronized function with the same lock without deadlocking. The lock object is whatever the caller passes (`Ghost` passes a private `Any`).

## The code

```kotlin
actual fun <K, V> createAtomicMap(): MutableMap<K, V> = AppleConcurrentMap()

actual fun <T> runSynchronized(
    lock: Any,
    block: () -> T
): T = try {
    objc_sync_enter(lock)
    block()
} finally {
    objc_sync_exit(lock)
}
```

Note that `objc_sync_enter` is inside the `try`, so `objc_sync_exit` runs even if entering fails.

## Related

- [[Ghost]]: declares the `expect` functions and uses them
- [[AppleConcurrentMap]]: the map returned by `createAtomicMap`
- [[Ghost.wasmJs]]: Wasm actual with no locking
- [[Ghost.jvmAndroid]]: JVM and Android actual using `synchronized`
- [[Ghost.nonJvm]]: non-JVM file shared with Apple for pooled readers and writers
