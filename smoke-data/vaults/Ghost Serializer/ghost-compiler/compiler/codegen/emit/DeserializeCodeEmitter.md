---
tags: [ghost-serializer, compiler, codegen, deserialize]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/emit/DeserializeCodeEmitter.kt
kind: class
---

# DeserializeCodeEmitter

The coordinator that builds the generated `deserialize(reader)` function: it chooses a strategy from the class shape and also contains the sealed, enum, object and inferred-polymorphism generators.

## What it is about

This is the reading-side counterpart of [[SerializeCodeEmitter]]. [[GhostCodeGenerator]] builds one instance per reader class (streaming, flat, string and YAML flat), and each call to `build` adds a `deserialize` override for that reader to the serializer object. It extends [[BaseDeserializeEmitter]] for the shared reader-call logic.

## How it works

`build` selects a body with a `when`: a Kotlin `object` (read and skip everything, return the instance), inferred sealed (no discriminator), discriminated sealed, value class, enum, a class above the property limit ([[FragmentedEmitter]]) or an ordinary class ([[StandardEmitter]]). It then adds the function and, for the non-flat path when resilience is supported, an `isResilient` override.

- **Sealed with discriminator:** peeks the discriminator key and generates a `when` over its value. A subclass marked with the fallback annotation is used for unknown values; otherwise an unknown type throws. The value of a subclass comes from its `@GhostSerialization(name = ...)` argument or, if absent, its simple name.
- **Enum:** selects the string against the enum options table (see [[SerializerSetupEmitter]]) and maps the index to a member. Unknown values use a member called `unknown` or, when a fallback is requested, the last member; with no fallback they throw.
- **Inferred sealed:** with no discriminator, the subclass is identified by which fields appear. Each field has a bitmask of the subclasses that own it; as fields are seen, an *eligibility mask* is narrowed with AND, and a *seen mask* is built with OR. At the end each subclass is tested against its required-fields mask, and the first eligible one that has all of its required fields is instantiated. If none match it throws a `GhostJsonException`.

## The code

The mask construction in `createInferredSealedContext`:

```kotlin
val propertyToClassMask = names.associateWith { name ->
    var mask = C.VAL_ZERO_L
    inferredInfo.forEachIndexed { index, subclass ->
        if (subclass.properties.any { it.jsonName == name }) {
            mask = mask or (C.VAL_ONE_L shl index)
        }
    }
    mask
}
```

And the strategy choice:

```kotlin
properties.size > C.PROPERTY_MAX_SIZE -> {
    emitFragmented(body = body, typeSpecBuilder = typeSpecBuilder, isFlatPath = isFlatPath)
}
else -> {
    emitStandard(body = body, typeSpecBuilder = typeSpecBuilder, isFlatPath = isFlatPath)
}
```

`emitFragmented` and `emitStandard` also call `injectContextualSerializers` unless the path is flat, so the contextual fields are declared once per object and not per reader. Because eligibility uses a `Long`, inferred polymorphism is limited by the 64 bits of the mask (the code shifts by subclass index).

## Related

- [[GhostCodeGenerator]]: creates one emitter per reader class.
- [[BaseDeserializeEmitter]]: the superclass with the shared reader-call builders.
- [[StandardEmitter]]: the body generator for ordinary classes.
- [[FragmentedEmitter]]: the body generator for large classes.
- [[SerializeCodeEmitter]]: the writing-side counterpart.
- [[InferredSubclassModel]]: describes the subclasses in inferred polymorphism.
- [[GhostJsonException]]: thrown by generated code when no subclass matches.
- [[GhostFallback]]: the annotation that marks a fallback subclass or enum value.
