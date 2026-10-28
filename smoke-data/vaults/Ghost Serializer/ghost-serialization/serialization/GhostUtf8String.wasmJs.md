---
tags: [ghost-serializer, serialization, platform, wasm, utf8]
source: ghost-serialization/src/wasmJsMain/kotlin/com/ghost/serialization/GhostUtf8String.wasmJs.kt
kind: expect/actual
---

# GhostUtf8String.wasmJs

The Kotlin/Wasm implementation of `ghostUtf8BytesToString`, which decodes UTF-8 with the browser's `TextDecoder`.

## What it is about

Turning bytes into a `String` is slow on Kotlin/Wasm when it goes through `decodeToString`, especially for payloads of tens of kilobytes (the source links the issue as #16, the "encode cliff"). Ghost bypasses that by handing the bytes to the JavaScript engine's native UTF-8 decoder. It is used when `Ghost.encodeToString` is told to encode through bytes on JavaScriptCore.

## How it works

The function copies the requested range byte by byte into a reusable JavaScript `Uint8Array`, then calls `new TextDecoder('utf-8').decode(...)` on it. The copy loop exists because Wasm memory and JS typed arrays are separate; the typed array is typed as `JsAny` so the module does not need the browser typed-array dependency.

To avoid allocating a typed array per call, the view is cached in a top-level variable. `acquireUtf8View` reuses it when it is big enough, and otherwise doubles the size (never below 4096 bytes). If the cached view is longer than the data, `decode` is called on a `subarray(0, length)`. An empty range returns `""` immediately.

## The code

```kotlin
internal actual fun ghostUtf8BytesToString(
    bytes: ByteArray,
    offset: Int,
    length: Int
): String {
    if (length <= 0) return ""
    val view = acquireUtf8View(length = length)
    var index = 0
    while (index < length) {
        u8Set(arr = view, index = index, value = bytes[offset + index].toInt() and 0xff)
        index++
    }
    return textDecodeUtf8(bytes = view, length = length)
}
```

The three private helpers (`newUint8Array`, `textDecodeUtf8`, `u8Set`) are one-line `js(code = ...)` bridges. The cache variables are not thread-safe, which is fine in a single-threaded browser.

## Related

- [[GhostUtf8String]]: declares the `expect` function
- [[GhostUtf8String.jvmAndroid]]: JVM/Android actual using `decodeToString`
- [[GhostUtf8String.native]]: native actual using `decodeToString`
- [[Ghost]]: calls it from `encodeToString` when `GhostHeuristics.encodeToStringViaUtf8Bytes` is set
- [[GhostHeuristics.wasmJs]]: the Wasm heuristics that decide when this path is used
