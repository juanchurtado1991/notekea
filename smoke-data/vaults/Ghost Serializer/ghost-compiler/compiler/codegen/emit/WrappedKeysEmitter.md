---
tags: [ghost-serializer, compiler, codegen, wrapped-keys]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/emit/WrappedKeysEmitter.kt
kind: object
---

# WrappedKeysEmitter

Code-generation helpers for `@GhostWrappedKeys`: reading several scattered top-level JSON keys into one nested object.

## What it is about

A wrapped-keys property gathers a set of source keys from the JSON object (for example three keys at the top level) and decodes them as the fields of a single Kotlin value. `WrappedKeysEmitter` produces the pieces for the *read* side: the constants that describe the keys, the capture variable that collects them during the parse loop, and the post-loop code that rebuilds a JSON document from them and decodes it. It is used by [[StandardEmitter]] (the fragmented emitter rejects wrapped keys).

## How it works

1. `addWrappedKeyConstants` declares two private constants per property: an array of byte-array literals for the source keys (each as `"key":` with the name escaped by [[JsonStringEscaping]]) and an `IntArray` of the indexes of keys flagged "omit if absent".
2. `emitCaptureVariables` declares a `GhostWrappedKeysCapture` sized for the number of keys.
3. During the parse loop, `wrappedKeyDispatch` maps each source key to its property and key index, and `emitWrappedKeyCapture` emits `reader.captureWrappedKey(index, ...)` so the value is saved rather than decoded.
4. After the loop, `emitPostLoopDeserialization` materializes the captured pieces into a JSON object (using the key literals, the omit-if-empty flag and the omit-absent indexes). If the result is not null it creates a nested reader on it and calls the property type's serializer; with resilient decoding it wraps that call. A nullable property gets `null` in an else branch.

## The code

Building the literals constant:

```kotlin
val quotedKey = C.STR_JSON_KEY_QUOTE + JsonStringEscaping.escape(text = key) + C.STR_JSON_KEY_COLON_SUFFIX
initializer.add(C.TEMPLATE_WRAPPED_KEY_LITERAL_BYTE, quotedKey)
```

The dispatch map links a wire key to the owning property:

```kotlin
keys.forEachIndexed { index, key ->
    map[key] = prop to index
}
```

The nested reader's type depends on the channel: for the string reader the emitter uses the string-reader template, otherwise the flat-reader template. This is the only place where the post-loop code is channel-aware.

## Related

- [[StandardEmitter]]: calls these helpers while building the parse loop and return.
- [[FragmentedEmitter]]: refuses classes with wrapped keys.
- [[JsonStringEscaping]]: escapes the key literals.
- [[GhostWrappedKeys]]: the annotation this supports.
- [[GhostWrappedKeysCapture]]: the runtime class that collects the captured values.
- [[WrappedKeysModel]]: the analysed form of the annotation.
- [[GhostPropertyModel]]: carries the `wrappedKeys` data.
