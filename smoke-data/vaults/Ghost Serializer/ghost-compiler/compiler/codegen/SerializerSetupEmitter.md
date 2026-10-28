---
tags: [ghost-serializer, compiler, codegen, perfect-hash]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/SerializerSetupEmitter.kt
kind: class
---

# SerializerSetupEmitter

The emitter for everything a generated serializer declares before its read and write bodies: key lookup options, cached field headers, enum tables and the `warmUp` method.

## What it is about

Fast serializers precompute what they can. Instead of comparing field names at runtime, a generated object holds a perfect-hash `OPTIONS` table; instead of encoding `"name":` on every write it holds a ready `ByteString` (and a `String` for the text channel). `SerializerSetupEmitter` produces those members. [[GhostCodeGenerator]] calls its methods when the class shape needs them.

## How it works

- **Headers.** `addCachedHeaderProperties` takes all JSON names of the class, lets [[HeaderConstantNames]] allocate unique suffixes, escapes each name with [[JsonStringEscaping]], and declares a private `H_<SUFFIX>` `ByteString` built from the header text. With a text channel it also declares an `HS_<SUFFIX>` string.
- **Options.** `addPerfectHashOptions` chooses the key names: for an inferred sealed class, the union of the subclasses' property names; otherwise the top-level names from [[DispatchNamesResolver]]. [[PerfectHashFinder]] finds shift, multiplier and table size, and the options initializer is built with [[GeneratedCallFormat]]. `addEnumOptions` does the same for enum values.
- **Warm-up.** `buildWarmUpMethod` generates an override that deserializes a tiny, valid-looking document inside `try/catch`, once per reader channel, so JIT and class loading cost is paid before the first real request. Failures are swallowed on purpose.

## The code

The warm-up document is synthesized from the required properties only:

```kotlin
if (!prop.isNullable && !prop.hasDefaultValue) {
    val key = AC.STR_DOUBLE_QUOTE + JsonStringEscaping.escape(text = prop.jsonName) + AC.STR_DOUBLE_QUOTE
    val value = when {
        prop.type.isPrimitiveInt() || prop.type.isPrimitiveLong() ||
                prop.type.isPrimitiveByte() || prop.type.isPrimitiveShort() -> AC.STR_ZERO
        prop.type.isPrimitiveBoolean() -> CC.STR_FALSE
        prop.type.isString() -> CG.STR_EMPTY_STRING
        // …
        else -> CC.STR_NULL
    }
```

Floating-point types get `0.0`, chars a JSON null-char string, lists and sets `[]`, maps and nested Ghost types `{}`. Sealed, enum and value classes just use `{}`. Because each branch only needs to produce something parsable, `warmUp` can ignore any resulting exception.

The private `buildReaderOptionsInitializer` simply forwards the hash configuration, the serializer's `textChannel` flag and the names to `GeneratedCallFormat.jsonReaderOptionsOf`.

## Related

- [[GhostCodeGenerator]]: decides when each method is called.
- [[HeaderConstantNames]]: allocates the constant suffixes.
- [[JsonStringEscaping]]: escapes names in the headers and the warm-up JSON.
- [[GeneratedCallFormat]]: formats the options initializer.
- [[PerfectHashFinder]]: computes the hash parameters.
- [[PerfectHashConfig]]: the result type of that computation.
- [[DispatchNamesResolver]]: supplies the top-level key names.
- [[GhostSerializerContext]]: the class facts this emitter reads.
