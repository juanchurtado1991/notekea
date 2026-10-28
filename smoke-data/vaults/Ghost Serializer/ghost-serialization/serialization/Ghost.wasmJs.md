---
tags: [ghost-serializer, serialization, platform, wasm]
source: ghost-serialization/src/wasmJsMain/kotlin/com/ghost/serialization/Ghost.wasmJs.kt
kind: expect/actual
---

# Ghost.wasmJs

The Kotlin/Wasm (browser) actuals for the thread-safety helpers that `Ghost` declares as `expect`.

## What it is about

Ghost keeps several shared caches and registries, and it needs two platform primitives to guard them: a concurrent map and a synchronized block. This file supplies the Wasm versions of both. It is the smallest of the `Ghost.*` platform files because the browser runtime has nothing to protect against.

## How it works

Kotlin/Wasm in the browser runs on a single thread, so no real locking is needed. The actual for `createAtomicMap` simply returns a plain `mutableMapOf()`, and `runSynchronized` just calls the block. Both are cheaper than the thread-safe variants used on the JVM (`ConcurrentHashMap`, `synchronized`) and on Apple targets (an `objc_sync` based map).

The trade-off is that the code is only correct as long as Wasm stays single-threaded. If Kotlin/Wasm ever gets shared-memory threads, this file would have to change. The comment at the top of the source records that assumption.

## The code

```kotlin
actual fun <K, V> createAtomicMap(): MutableMap<K, V> = mutableMapOf()

actual fun <T> runSynchronized(
    lock: Any,
    block: () -> T
): T = block()
```

- `createAtomicMap` backs `Ghost`'s serializer, type and name caches. Here "atomic" costs nothing.
- `runSynchronized` ignores `lock` completely and runs `block` inline. `Ghost.addRegistry`, `Ghost.getSerializer` and `Ghost.prewarm` all call it, so they stay free of overhead on Wasm.

## Related

- [[Ghost]]: declares the `expect` functions this file implements and uses them for its caches
- [[Ghost.apple]]: Apple actual of the same two functions, with a real lock
- [[Ghost.jvmAndroid]]: JVM/Android actual, using `ConcurrentHashMap` and `synchronized`
- [[Ghost.nonJvm]]: sibling non-JVM file that implements the pooled reader and writer helpers for native and Wasm
