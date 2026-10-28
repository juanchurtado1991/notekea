---
tags: [ghost-serializer, compiler, model, custom-coder]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/model/CustomCoderModel.kt
kind: class
---

# CustomCoderModel

Holds what the compiler knows about a user-supplied encoder or decoder for one property.

## What it is about

A property can be marked `@GhostDecoder(provider, functionName)` or `@GhostEncoder(...)` to bypass the generated read or write code for it with a function the developer wrote. [[CustomCoderAnalyzer]] resolves such an annotation into this record, and the emitters use it to write the call into the serializer. A property stores two of them: `customDecoder` and `customEncoder`.

## How it works

The record has three fields: the `provider` (a KotlinPoet `TypeName` for the class or object that owns the function), the `functionName`, and `readerKinds`, a set of [[CustomCoderReaderKind]] values describing which reader parameter types the function accepts. Ghost has three reader implementations (bytes/streaming, flat buffer, native string), and a custom function may support only some. The set defaults to just `BYTES`.

`supports(kind)` answers whether the function can be called directly with the reader of the current channel. If so, the generated code simply calls `Provider.fn(reader)`. If not, the emitter builds a bridge: it creates a temporary `GhostJsonReader` over the same data and position, calls the function with it, then moves the real reader's position forward by what the temporary reader consumed. That is slower but keeps one custom function working on every channel.

## The code

```kotlin
internal data class CustomCoderModel(
    val provider: TypeName,
    val functionName: String,
    val readerKinds: Set<CustomCoderReaderKind> = setOf(CustomCoderReaderKind.BYTES),
) {
    fun supports(kind: CustomCoderReaderKind): Boolean = kind in readerKinds
}
```

The decoder emitter picks the channel kind from the reader class it is generating for, and chooses direct call or bridge with `coder.supports(channelKind)`.

## Related

- [[CustomCoderAnalyzer]]: creates it from the annotation and the overloads it finds.
- [[CustomCoderReaderKind]]: the element type of `readerKinds`.
- [[GhostPropertyModel]]: holds the decoder and the encoder.
- [[BaseDeserializeEmitter]]: uses `supports` to choose a direct call or a bridge.
- [[GhostDecoder]]: the annotation that declares a custom decoder.
- [[GhostEncoder]]: the annotation that declares a custom encoder.
