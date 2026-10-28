---
tags: [ghost-serializer, compiler, codegen, serialize]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/emit/BaseSerializeEmitter.kt
kind: class
---

# BaseSerializeEmitter

The abstract base of the serialize-side emitters: it knows how to write one property, one nested value, one collection and the nesting objects of flattened paths into generated Kotlin.

## What it is about

Every generated `serialize(writer, value)` body is a sequence of "write this field" statements. The size-dependent shells ([[StandardSerializeEmitter]] and [[FragmentedSerializeEmitter]]) decide how those statements are grouped, but the content of each statement is produced here. This is the largest piece of the writing side of the compiler and sits between the property model and KotlinPoet `CodeBlock`s.

## How it works

For each property `emitProperty` picks the key constant (`H_*` bytes for the byte writer, `HS_*` strings for the string writer, names allocated by [[HeaderConstantNames]]) and then branches:

- **Wrapped keys** properties write their unwrapped fields one by one, with an `is` check when a field belongs to one sealed subclass.
- **Proto3 omission:** a non-null scalar or collection that holds its zero value is skipped under a generated `if`.
- **Nullable:** either the field is omitted when null, or a `null` value is written.
- **Fused types** (`Int`, `Long`, `ULong`, `String`, `Boolean`, `Double`, `Float` without a custom encoder, and not proto-quoted) use a single fused writer call that writes the name and value together. Anything else writes the raw name, then goes through `emitValue`.

`emitTypeValue` resolves a type recursively: value classes unwrap to their inner type, raw JSON is written as a slice, Ghost and enum types call their serializer object, scalars use [[ScalarWriteTemplates]], and lists, sets and maps expand into loops. Anything else calls a contextual serializer from the [[ContextualSerializerRegistry]]. Loop variables use a counter (`size0`, `i0`, ...) so sibling lists at the same depth never collide. Sets iterate directly without copying to a list.

`emitPathTransition` and `emitClosePath` keep a mutable `currentPath`: they close the objects the next property does not share and open the new ones, so flattened and wrapped fields land in nested JSON objects.

## The code

The fused-type decision:

```kotlin
val isProtoQuotedType = prop.isProto &&
    (type == AC.K_LONG || type == AC.K_ULONG || type == AC.K_DOUBLE || type == AC.K_FLOAT)
if (isProtoQuotedType) {
    return false
}
```

The custom-encoder path is notable: when the target is the string writer, the encoder is written for the byte writer, so the emitted code creates a temporary flat buffer and writer, runs the encoder on it, then copies the bytes into the string channel through a bridge.

The list loop:

```kotlin
code.addStatement(C.STR_WRITER_BEGIN_ARR)
code.addStatement("val %L = %L.size", sizeVar, accessor)
code.beginControlFlow("for (%L in 0 until %L)", indexVar, sizeVar)
code.addStatement("val %L = %L[%L]", itemVar, accessor, indexVar)
```

An indexed loop avoids allocating an iterator for lists.

## Related

- [[StandardSerializeEmitter]]: subclass for normal-sized classes.
- [[FragmentedSerializeEmitter]]: subclass that splits large classes into chunk functions.
- [[SerializeCodeEmitter]]: chooses which subclass to use.
- [[ContextualSerializerRegistry]]: provides names for contextual serializers.
- [[ScalarWriteTemplates]]: the scalar writer statements.
- [[HeaderConstantNames]]: the header constant naming.
- [[PrimitiveArraySerializers]]: picks the primitive-array serializer class.
- [[GhostPropertyModel]]: the property data it consumes.
