---
tags: [ghost-serializer, api, annotations, custom-coder]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/annotations/GhostDecoder.kt
kind: annotation
---

# GhostDecoder

A property annotation that hands the deserialization of one field to a function you write yourself.

## What it is about

Sometimes a field has a wire format that no built-in serializer understands, for example a date packed into a custom string. Instead of writing a whole serializer for the owning class, you annotate just that property with `@GhostDecoder` and point it at a static function. It belongs to the annotations package of `ghost-api` and is read by the KSP compiler, not at runtime.

## How it works

At compile time the compiler's custom coder analysis reads the two arguments, `provider` (a class or object) and `functionName`, and emits a direct call to that function in the generated deserializer. The first parameter of the function must be one of Ghost's JSON readers: `GhostJsonReader`, `GhostJsonFlatReader` or `GhostJsonStringReader`. If you provide an overload for each reader you use, the generated code calls it directly; if you do not, the compiler bridges through a temporary reader, which costs an extra allocation.

The feature is JSON only. Combining it with [[GhostYamlSerialization]] on the same class is rejected, because YAML has no matching reader contract.

## The code

```kotlin
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class GhostDecoder(
    val provider: KClass<*>,
    val functionName: String
)
```

- `provider` is a `KClass<*>`, so it can name an `object` or a class with a static-like function.
- `functionName` is a plain string resolved by the analyzer, which is why a typo is a compile-time diagnostic rather than a runtime failure.
- The target is `PROPERTY` only: the decoder is per field, never per class.

The write-side counterpart is [[GhostEncoder]], which uses the same shape.

## Related

- [[GhostEncoder]]: the serialization twin of this annotation.
- [[CustomCoderAnalyzer]]: the compiler piece that reads the annotation and validates the function.
- [[GhostYamlSerialization]]: incompatible with custom coders.
- [[GhostJsonReader]]: one of the reader types the target function can accept.
- [[GhostJsonFlatReader]]: the flat reader variant the function may take.
- [[GhostSerialization]]: the class-level annotation whose generated deserializer calls the custom function.
