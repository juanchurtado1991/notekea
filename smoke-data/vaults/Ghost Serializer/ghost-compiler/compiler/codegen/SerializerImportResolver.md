---
tags: [ghost-serializer, compiler, codegen, imports]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/SerializerImportResolver.kt
kind: class
---

# SerializerImportResolver

The class that works out which reader extension functions and helper types a generated serializer file must import, and adds exactly those.

## What it is about

Generated serializers call many top-level extension functions of the reader (`nextInt`, `readList`, `beginObject`, `nextStringOrNull` and so on). Kotlin needs an explicit import for each one, but importing all of them into every file would produce unused-import noise and slow compilation. `SerializerImportResolver` inspects the class and imports only what the emitted code will use. It is called once by [[GhostCodeGenerator]] through `applyTo`.

## How it works

`applyTo` first adds object-parsing imports when the class reads objects, then the null-handling helpers for flattened groups, then calls `addParserImports`. That method looks at all property types, including type arguments, value-class inner types and the properties of inferred sealed subclasses, and decides:

- list, set and map readers, plus `beginArray`/`endArray` and `nextKey`;
- nullable helpers: a nullable `String`, `Int`, `Long`, `ULong` or `Boolean` uses a fused `nextXOrNull`, while other nullable types use `consumeNull` and `isNextNullValue`;
- scalar readers (`nextInt`, `nextLong`, `nextString`, doubles, floats, chars, booleans, proto unsigned longs), only when a plain call will actually be emitted;
- Base64 helpers versus the raw-JSON capture for `ByteArray`, and raw JSON, resilient decoding and wrapped-key imports.

When the class has a text channel, a `mirrorStringChannelImports` helper repeats the imports from the string-reader package.

Every `needs...Import` predicate must agree with what the deserialize emitters produce. For example, `ByteArray` is "covered" (Base64) only for proto properties, and proto propagates through lists, sets and maps but not into inferred subclasses; the code comments say this must mirror the emitters exactly.

## The code

A nullable scalar is a good example of the "only what is used" rule:

```kotlin
when {
    type.isString() -> nullableImports.add(CG.STR_NEXT_STRING_OR_NULL_NAME)
    type.isPrimitiveInt() -> nullableImports.add(CG.STR_NEXT_INT_OR_NULL_NAME)
    type.isPrimitiveLong() -> nullableImports.add(CG.STR_NEXT_LONG_OR_NULL_NAME)
    // …
    else -> {
        nullableImports.add(CG.STR_CONSUME_NULL_NAME)
        nullableImports.add(CG.STR_IS_NEXT_NULL_VALUE_NAME)
    }
}
```

The small predicates show the decision rule: `emitsPlainNextInt` is true for a non-null `Int` and for `Byte` or `Short`, since those always emit `reader.nextInt().toX()`. A nullable scalar is fused, so it does not need the plain function. `resolveAllTypes` collects every reachable type once, setting a `hasNullable` flag on the way, so the later checks are cheap.

The private enum `ByteArrayCoverage { COVERED, UNCOVERED }` classifies each `ByteArray` occurrence, and `hasProtoFloatingUsage` triggers the proto value import for double and float properties without a custom encoder.

## Related

- [[GhostCodeGenerator]]: creates it and calls `applyTo` on the file builder.
- [[GhostSerializerContext]]: supplies the properties and flags it reads.
- [[GhostPropertyModel]]: the per-property data it inspects.
- [[DeserializeCodeEmitter]]: emits the calls that these imports must cover.
- [[BaseDeserializeEmitter]]: the shared deserialize logic whose choices it mirrors.
- [[GhostCodegenConstants]]: the names of imports and packages.
- [[TypeHelpers]]: the `isString`, `isList`, `isMap` style type predicates it uses.
