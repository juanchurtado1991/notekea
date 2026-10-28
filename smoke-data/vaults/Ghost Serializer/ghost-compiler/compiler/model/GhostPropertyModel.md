---
tags: [ghost-serializer, compiler, model, analysis]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/model/GhostPropertyModel.kt
kind: class
---

# GhostPropertyModel

The compiler's description of one serializable property: everything the analyzer learned about it, in a form the emitters can use without touching KSP again.

## What it is about

The Ghost compiler works in two phases. [[GhostAnalyzer]] reads Kotlin declarations through KSP and the emitters write code. This data class is the contract between the two. Each property of a `@GhostSerialization` class becomes one `GhostPropertyModel`, so emitters ask questions such as "is it a list of Ghost objects?" or "does it have a default?" and never inspect annotations themselves. It is the most widely used type of the compiler and sits in the model layer.

## How it works

It is an `internal data class` with about 35 fields, most with defaults, so each analysis path sets only what applies. The fields fall into groups:

- **Identity and type**: `kotlinName`, `jsonName` (from `@GhostName` when present), the KSP `type`, a KotlinPoet `typeName` and `isNullable`.
- **Shape**: `isGhost`, `isList`, `isSet`, `isMap`, `isEnum`, `isPrimitiveArray`, `isValueClass`, `isSealedClass`, with inner element data such as `listInnerType`, `mapValueType` and `valueClassProperty`.
- **Defaults**: `hasDefaultValue`, `defaultExpression` (a whitelisted source string, or null when the generator must fall back to `.copy()`) and `isDefaultDependencySource`.
- **Annotation effects**: `isResilient`, `isContextual`, `customDecoder`, `customEncoder`, `flattenPath`, `wrapPath`, `wrappedKeys`, `isInferredSignature` and `inferredSubclasses`.
- **Mode**: `isProto` switches `Long` handling to quoted decimal strings, as the proto3 JSON mapping requires.

Related data that belongs together is grouped into its own types (for example [[WrappedKeysModel]] and [[CustomCoderModel]]) so a property has all or none of it.

## The code

Part of the declaration:

```kotlin
internal data class GhostPropertyModel(
    val kotlinName: String,
    val jsonName: String,
    val type: KSType,
    val typeName: TypeName,
    val isNullable: Boolean,
    val isGhost: Boolean,
    val isList: Boolean,
    // …
    val wrappedKeys: WrappedKeysModel? = null,
    val isInferredSignature: Boolean = false,
    val inferredSubclasses: List<InferredSubclassModel> = emptyList(),
    val isProto: Boolean = false,
    val isDefaultDependencySource: Boolean = false
)
```

`defaultExpression` is the field to treat carefully. It holds source text copied from the constructor default, and only an allowed set of forms is accepted (see [[DefaultExpressionExtractor]]); a null value tells the emitter it cannot reproduce the default itself.

## Related

- [[GhostAnalyzer]]: builds these models from KSP declarations.
- [[WrappedKeysModel]]: the wrapped-keys data carried by `wrappedKeys`.
- [[InferredSubclassModel]]: the subclass data carried by `inferredSubclasses`.
- [[CustomCoderModel]]: the type of `customDecoder` and `customEncoder`.
- [[GhostSerializerContext]]: gathers the models of a class for the emitters.
- [[GhostYamlEligibility]]: reads many fields to decide on YAML.
- [[DeserializeCodeEmitter]]: one of many emitters that consume it.
