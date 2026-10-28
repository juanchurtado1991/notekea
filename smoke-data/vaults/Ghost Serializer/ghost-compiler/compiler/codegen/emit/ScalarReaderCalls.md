---
tags: [ghost-serializer, compiler, codegen, deserialize]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/emit/ScalarReaderCalls.kt
kind: object
---

# ScalarReaderCalls

A lookup table that maps each Kotlin scalar type to the reader call that decodes it.

## What it is about

The deserialize emitters need a statement such as `reader.nextInt()` for an `Int` property, `reader.nextStringOrNull()` for a nullable `String`, or a coerced variant for proto3. `ScalarReaderCalls` holds these decisions as data. As the comment says, supporting a new scalar means adding one entry instead of one more branch in the type dispatch of [[BaseDeserializeEmitter]].

## How it works

Each `Entry` has a `matches` predicate on a `KSType`, a mandatory `nonNullCall`, an optional `orNullCall` and an optional `protoCall`. `find(type)` returns the first entry that matches, or `null`, which sends the caller down the other cases (value classes, collections, Ghost types, contextual serializers).

The three call forms capture real decisions:

- **`orNullCall`** is a fused `nextXOrNull()` that reads a nullable value in one call. It exists for `Int`, `Boolean`, `Long`, `ULong` and `String`. Types without one (`Double`, `Float`, `Byte`, `Short`, `Char`) are wrapped by the emitter in a generic null guard instead.
- **`protoCall`** replaces the plain call under proto3, where 64-bit integers can arrive as quoted strings. Only `Long` and `ULong` have one (the "coerced" readers).
- **`nonNullCall`** is the plain call.

## The code

```kotlin
class Entry(
    val matches: (KSType) -> Boolean,
    val nonNullCall: String,
    val orNullCall: String? = null,
    val protoCall: String? = null,
)
```

A representative entry:

```kotlin
Entry(
    matches = KSType::isPrimitiveLong,
    nonNullCall = C.STR_NEXT_LONG,
    orNullCall = C.STR_NEXT_LONG_OR_NULL,
    protoCall = C.STR_NEXT_LONG_PROTO_COERCED
),
```

The list is checked in order with `firstOrNull`, so ordering only matters if two predicates could match the same type, which they do not here. [[BaseDeserializeEmitter]] uses the entry in `readScalar`: proto override first, then the fused call for nullable types, then the plain call under a null guard.

## Related

- [[BaseDeserializeEmitter]]: the only consumer, in `buildTypeReaderCall` and `readScalar`.
- [[ScalarWriteTemplates]]: the equivalent table for writing.
- [[SerializerImportResolver]]: decides the imports for the same calls; it must stay consistent with this table.
- [[TypeHelpers]]: provides the `isPrimitiveInt`, `isString` etc. predicates used as `matches`.
- [[GhostEmitterConstants]]: the call strings.
