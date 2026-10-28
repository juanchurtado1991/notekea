---
tags: [ghost-serializer, compiler, model, codegen]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/model/GhostSerializerContext.kt
kind: class
---

# GhostSerializerContext

An immutable bundle of every fact the code generator needs about one class, computed once.

## What it is about

Generating a serializer requires many small answers: Is the class sealed, a value class, an enum or an object? Is it proto? What is the discriminator key? What are its JSON names? Which reader and writer classes should the generated code mention? Rather than recompute these in each emitter, `GhostSerializerContext` computes them all in one place from the KSP declaration and the [[GhostPropertyModel]] list, and passes the finished object around. It sits between analysis and emission.

## How it works

The constructor is private; the only way to get an instance is `GhostSerializerContext.from(...)`. It takes the properties, the class declaration, the text-channel flag, the optional [[GhostEnvelopeModel]] and the YAML flag, and derives:

- **Shape flags**: `isSealed`, `isValue`, `isEnum`, `isObject`, `isResilient`, `isProto`, `isInferred`, plus the sealed subclasses.
- **Names**: the package, the original class name, and `baseClassName`, nested names joined with underscores, from which `serializerName` is `baseClassName + "Serializer"`. `finalTypeName` is the `name` set in the annotation, or the simple class name.
- **Sealed hierarchy data**: for a subclass of a sealed class (`resolveParentSealedClass` checks the parent declaration and super types) it computes the `discriminator` value, and the `sealedDiscriminatorKey` read from the parent (default `"type"`). `isInferred` is read from the sealed parent.
- **Paths**: `fullPaths` has one path per property, which is the flatten path, the wrap path plus the property name, or just the name.
- **Runtime classes**: KotlinPoet `ClassName`s for the abstract serializer, JSON writers, the three readers and the YAML counterparts, built from the package constants.

Three methods answer common questions: `getAllJsonNames` (in an inferred sealed class, names of all subclasses' properties), `needsCachedByteStringHeaders` and `needsObjectParsingImports`, which help the generator skip unneeded declarations.

## The code

The helper that gathers wire names, sorted so the output is stable:

```kotlin
internal fun collectJsonNames(
    properties: List<GhostPropertyModel>
): List<String> {
    val names = mutableSetOf<String>()
    properties.forEach { prop ->
        val wrappedKeys = prop.wrappedKeys
        if (wrappedKeys != null) {
            names.addAll(wrappedKeys.sourceKeys)
        } else if (prop.flattenPath != null) {
            names.addAll(prop.flattenPath)
        // …
```

and the derived serializer name:

```kotlin
val serializerName: String = baseClassName + CC.STR_SERIALIZER_SUFFIX
```

## Related

- [[GhostCodeGenerator]]: creates the context and drives generation with it.
- [[GhostPropertyModel]]: the property list it holds.
- [[GhostEnvelopeModel]]: kept as `envelopeModel`.
- [[SerializerSetupEmitter]]: reads names and flags to emit the setup.
- [[SerializerImportResolver]]: decides imports from the same flags.
- [[GhostCommonConstants]]: package names for the runtime classes.
- [[AbstractGhostSerializer]]: the base class every generated serializer extends.
