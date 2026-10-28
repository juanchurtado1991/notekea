---
tags: [ghost-serializer, compiler, codegen, serialize]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/emit/StandardSerializeEmitter.kt
kind: class
---

# StandardSerializeEmitter

The serialize emitter for ordinary classes: it writes the whole object body inline in one function.

## What it is about

Most DTOs have a modest number of properties, so the generated `serialize` function can hold every write statement directly. `StandardSerializeEmitter` is that simple shell on top of [[BaseSerializeEmitter]]. For classes above the size limit, [[SerializeCodeEmitter]] uses [[FragmentedSerializeEmitter]] instead. The class comment gives "typically fewer than 40 properties", which matches the limit constant in [[GhostEmitterConstants]].

## How it works

`emit` produces the object in four steps:

1. Write `beginObject`.
2. If a sealed discriminator value is given and no property already uses the discriminator key, write the key and the subclass name first.
3. For each property, in the order already sorted by [[SerializeCodeEmitter]], call `emitPathTransition` (which closes or opens nested objects for flattened paths) and then `emitProperty`.
4. Close any objects still open and write `endObject`.

The `currentPath` list is local to the call, so there is no state between serializer functions.

## The code

The whole method is short:

```kotlin
val currentPath = mutableListOf<String>()
properties.forEach { prop ->
    emitPathTransition(code = code, currentPath = currentPath, prop = prop)
    emitProperty(code = code, prop = prop)
}
emitClosePath(code = code, currentPath = currentPath)
code.addStatement(C.STR_WRITER_END_OBJ)
```

The discriminator check is what lets a sealed subclass carry its type tag without a duplicate when the class has its own property of that name:

```kotlin
val hasDiscriminatorProperty = properties.any { it.jsonName == sealedDiscriminatorKey }
if (discriminator != null && !hasDiscriminatorProperty) {
    code.addStatement(C.STR_WRITER_NAME_TYPE_VAL, sealedDiscriminatorKey, discriminator)
}
```

`SerializeCodeEmitter` also uses this class for value classes, calling `emitValueOrNull` on it to write the single wrapped property.

## Related

- [[BaseSerializeEmitter]]: the superclass with all property-level logic.
- [[SerializeCodeEmitter]]: creates it and chooses between this and the fragmented form.
- [[FragmentedSerializeEmitter]]: the large-class alternative.
- [[GhostPropertyModel]]: the properties it writes.
- [[GhostEmitterConstants]]: the statement templates and size limit.
