---
tags: [ghost-serialization, serialization, wasm, heuristics]
source: ghost-serialization/src/wasmJsMain/kotlin/com/ghost/serialization/parser/common/JsEngineDetect.wasmJs.kt
kind: functions
---

# JsEngineDetect.wasmJs

A one-function file that guesses, from the browser's user agent, whether the page runs on JavaScriptCore (Safari and iOS) so Ghost can choose a faster way to encode to a string.

## What it is about

On Kotlin/Wasm Ghost has two ways to implement `encodeToString`: a char-based writer followed by `CharArray.concatToString`, or the UTF-8 flat writer followed by a text decoder. Their speed depends on the engine. This file provides the probe that [[GhostHeuristics.wasmJs]] calls to pick one. It is part of the Wasm encode-path selection referred to as item #16 in the source comment.

## How it works

`ghostJsEnginePrefersUtf8EncodeToString()` is implemented directly in JavaScript with `js(code = ...)`, as an immediately invoked function:

1. If `navigator` does not exist (a non-browser host), return `true`.
2. Read `navigator.userAgent`.
3. If it contains `iPhone`, `iPad` or `iPod`, return `true`: every iOS browser, whatever its branding, runs JavaScriptCore.
4. Otherwise test for desktop Safari: the UA contains `Safari` but none of the tokens for Chrome, Chromium, CriOS, Edge variants, Firefox variants or Opera.

Returning `true` means "prefer the UTF-8 route", because JavaScriptCore has a severe slowdown on `CharArray.concatToString`, while V8 (Chrome and Edge) prefers the char writer. The check is string sniffing, so it is a heuristic; an unusual user agent could be misclassified, with only a performance cost.

## The code

```kotlin
@OptIn(ExperimentalWasmJsInterop::class)
internal fun ghostJsEnginePrefersUtf8EncodeToString(): Boolean = js(
    code = """
    (function () {
        if (typeof navigator === 'undefined') return true;
        var ua = navigator.userAgent || '';
        // Every iOS browser runs JavaScriptCore, whatever its branding says.
        if (/iPhone|iPad|iPod/.test(ua)) return true;
        // …
        return /Safari/.test(ua) && !isChromiumFirefoxOrOpera;
    })()
    """
)
```

The function is `internal`, so only code inside the serialization module (in practice the Wasm heuristics object) can call it, and it is evaluated once.

## Related

- [[GhostHeuristics.wasmJs]]: the only caller; caches the result as `encodeToStringViaUtf8Bytes`.
- [[GhostHeuristics]]: the `expect object` whose KDoc documents the flag and the JavaScriptCore slowdown.
- [[GhostJsonStringWriter]]: the char-based writer used when this returns `false`.
- [[FlatByteArrayWriter]]: the UTF-8 flat writer used when it returns `true`.
