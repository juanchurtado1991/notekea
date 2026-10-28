---
tags: [ghost-serializer, compiler, codegen, deserialize]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/emit/BaseDeserializeEmitter.kt
kind: class
---

# BaseDeserializeEmitter

The abstract base of the deserialize emitters: it holds the bitmask bookkeeping and builds the reader call for every kind of property and type.

## What it is about

Whatever the shape of the class, reading a property comes down to the same questions: which reader call, how to treat null, how to remember that a field was seen, and how to descend into flattened objects. `BaseDeserializeEmitter` answers them once, and [[StandardEmitter]] and [[FragmentedEmitter]] reuse the answers. It is the reading twin of [[BaseSerializeEmitter]].

## How it works

**Masks.** Presence of fields is tracked in `Long` bitmasks (64 properties per mask), so `maskCount` grows with the number of properties. `requiredMasks` marks fields that must appear on the wire; `defaultMasks` marks fields with a default value, which lets the generated code omit the constructor argument when the field was not present. Generated validation is then a single `mask & required == required` test. `emitPropertyMaskConstants` and `emitDefaultMaskConstant` declare these as private `const val`s, using a special string for `Long.MIN_VALUE` that cannot be written as a negative literal.

**Reader calls.** `buildCall` handles a property: custom decoder first, then nullable, then value class (recursing into the inner property), sealed, primitive array, proto3 coercions (`Long`, `ULong`, `ByteArray` as Base64), contextual and finally `buildTypeReaderCall`. That function recurses by type: raw JSON, `ByteArray`, value class (wrap the inner call in the constructor), Ghost or enum (call the serializer), scalars from [[ScalarReaderCalls]], then set, list and map via the read-collection templates. Nullable types are wrapped by `nullGuarded`; scalars with a fused `orNull` call skip the extra branch. A collection with no resolvable type argument degrades to reading a string.

**Custom decoders** are called directly when the decoder supports the reader's channel; otherwise a bridge creates a temporary reader of the supported kind, runs the decoder, and copies the position back.

**Flattened groups.** `emitFlattenedGroup` generates the nested `while/when` loop for each level of a flatten path, using the `OPTIONS_*` table from [[FlattenOptionsGenerator]]. A null object on the path counts as absent. If two leaf properties end at the same path it throws at compile time to avoid an infinite loop.

## The code

```kotlin
protected val maskCount = (properties.size + C.MASK_SIZE_BITS_MINUS_ONE) /
        C.MASK_SIZE_BITS.toInt()
```

```kotlin
scalar != null -> readScalar(scalar = scalar, type = type, isProto = isProto)

type.isSet() -> {
    val inner = type.arguments.firstOrNull()?.type?.resolve()
        ?: return stringReaderFallback(type = type)
    // …
}
```

Contextual types again use [[ContextualSerializerRegistry]] and are declared via `injectContextualSerializers`.

## Related

- [[StandardEmitter]]: subclass for ordinary classes.
- [[FragmentedEmitter]]: subclass for large classes.
- [[DeserializeCodeEmitter]]: also a subclass, adds the sealed, enum, object and inferred strategies.
- [[ScalarReaderCalls]]: the scalar call table.
- [[ContextualSerializerRegistry]]: names the contextual serializer fields.
- [[PrimitiveArraySerializers]]: picks the primitive-array serializer class.
- [[FlattenOptionsGenerator]]: generates the options for flattened levels.
- [[BaseSerializeEmitter]]: the writing-side twin.
