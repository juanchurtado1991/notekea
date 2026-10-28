---
tags: [ghost-serializer, serialization, platform, apple, concurrency]
source: ghost-serialization/src/appleMain/kotlin/com/ghost/serialization/AppleConcurrentMap.kt
kind: class
---

# AppleConcurrentMap

A thread-safe `MutableMap` for Apple Kotlin/Native, guarded by Objective-C object locks.

## What it is about

The JVM has `ConcurrentHashMap`, but Kotlin/Native does not offer a common concurrent map. Ghost's serializer caches need one, so on Apple targets this class wraps an ordinary map and locks every operation with `objc_sync_enter` and `objc_sync_exit`, the same primitive `runSynchronized` uses.

## How it works

All operations go through a private `inline fun withLock`, which locks on a private `Any`, runs the operation on the delegate map, and unlocks in a `finally`. Reads such as `get` and `containsKey` also take the lock, which suits Native's shared-object memory model. The `entries`, `keys` and `values` views are returned as snapshot copies (`toMutableSet`, `toMutableList`), so callers can iterate without holding the lock and without seeing concurrent changes. As a consequence these views are not live: modifying them does not change the map.

## The code

```kotlin
internal class AppleConcurrentMap<K, V> : MutableMap<K, V> {
    private val delegate = mutableMapOf<K, V>()
    private val lock = Any()
    private inline fun <T> withLock(
        block: () -> T
    ): T {
        objc_sync_enter(lock)
        return try {
            block()
        } finally {
            objc_sync_exit(lock)
        }
    }
    // …
}
```

## Related

- [[Ghost.apple]]: creates it in `createAtomicMap`
- [[Ghost]]: uses it for its serializer caches on Apple platforms
- [[Ghost.wasmJs]]: Wasm actual that uses a plain map instead
