---
tags: [ghost-serializer, compiler, model, wrapped-keys]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/model/WrappedUnwrapFieldModel.kt
kind: class
---

# WrappedUnwrapFieldModel

Describes one wire field that is written out when a `@GhostWrappedKeys` property is unwrapped during serialization.

## What it is about

Reading wrapped keys collects several top-level JSON keys into one nested object. Writing has to go the other way: for a wrapped property, the serializer emits each key again at the top level with the matching value taken from inside the wrapped type. This record is one such key-to-value mapping, and the analyzer builds one per key. It is part of [[WrappedKeysModel]].

## How it works

The emitter walks the list of these records. For each one it writes the JSON name and then reads the value by following `kotlinPath`, a list of property names from the wrapped object to the value, and writes it according to its `type`. A nullable field is written only when it is not null.

The only subtle field is `sealedSubclassName`. In a proto3 `oneof`, the wrapped type is a sealed class and each wire key belongs to one subclass (the KDoc example is `Text.text` and `Code.code`). When that field is set, the generated writer wraps the key in an `is` check for that subclass and reads the field through the smart-cast, so keys of other subclasses are simply not written. When it is null (the default), the field belongs directly to the wrapped type.

No behavior beyond holding data.

## The code

```kotlin
internal data class WrappedUnwrapFieldModel(
    val jsonName: String,
    val kotlinPath: List<String>,
    val isNullable: Boolean,
    val typeName: TypeName,
    val type: KSType,
    val sealedSubclassName: ClassName? = null,
)
```

It carries both a KotlinPoet `typeName` (to write code) and a KSP `type` (so the emitter can reason about whether the value is, for instance, a list or an enum).

## Related

- [[WrappedKeysModel]]: contains a list of these as `unwrapFields`.
- [[WrappedKeysAnalyzer]]: builds the records by walking the wrapped type.
- [[BaseSerializeEmitter]]: loops over them to write the keys back.
