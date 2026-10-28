---
tags: [ghost-serializer, api, annotations, custom-coder]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/annotations/GhostEncoder.kt
kind: annotation
---

# GhostEncoder

A property annotation that sends the serialization of one field to a function you provide.

## What it is about

It is the write-side twin of [[GhostDecoder]]. When a field must appear on the wire in a custom form, you point `@GhostEncoder` at a static function instead of building a complete serializer for the parent class. It belongs to the annotations package of `ghost-api` and is JSON only; it cannot be used on a class that also uses [[GhostYamlSerialization]].

## How it works

At compile time the compiler's custom coder analysis reads `provider` and `functionName` and generates a call to `fun(writer, value: T)` in the serializer. Ghost has two JSON writers, the flat byte writer and the general writer, and the documentation recommends providing an overload for each so both serialization paths call the function directly. The string channel is different: it bridges through a temporary flat writer, which means an extra object for that path.

This design keeps the common case fast, with direct calls and no reflection, while allowing any format the user function can write.

## The code

```kotlin
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class GhostEncoder(
    val provider: KClass<*>,
    val functionName: String
)
```

`provider` is the class or object that declares the function, and `functionName` is looked up by name when the compiler runs, so mistakes surface at build time.

## Related

- [[GhostDecoder]]: the deserialization twin.
- [[CustomCoderAnalyzer]]: validates and models the encoder function.
- [[GhostJsonWriter]]: one of the writer types the function may take.
- [[GhostJsonStringWriter]]: the string-channel writer path that uses a bridge.
- [[GhostYamlSerialization]]: incompatible with custom coders.
