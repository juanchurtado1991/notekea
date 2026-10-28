---
tags: [ghost-serializer, compiler, codegen, serialize]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/emit/SerializeCodeEmitter.kt
kind: class
---

# SerializeCodeEmitter

The coordinator that builds the generated `serialize(writer, value)` function: it looks at the shape of the class and delegates to the right specialised emitter.

## What it is about

Serializing a sealed hierarchy, an enum, a value class, a small DTO and a huge DTO all need different bodies. `SerializeCodeEmitter` is the front door for the writing side, mirror of [[DeserializeCodeEmitter]] for reading. [[GhostCodeGenerator]] creates one and calls `build` once per writer class (byte writer, string writer, YAML writer).

## How it works

At construction it sorts the properties so that flattened and wrapped properties with the same root end up next to each other. Each property is keyed by its first path segment (flatten path, wrap path, or its own JSON name); roots are ordered by first appearance, and within one root by the joined path. This lets the emitters open a nested JSON object once and close it once, avoiding duplicate brackets.

`build` picks a strategy with a `when`:

1. sealed: a `when (value)` that calls the serializer object of each subclass;
2. value class: unwrap and write the single inner property;
3. enum: a `when` that writes each member's serial name (or the plain `name` when there are no custom names);
4. more properties than the size limit: [[FragmentedSerializeEmitter]];
5. otherwise: [[StandardSerializeEmitter]].

It remembers the emitter it used as `activeEmitter`, because the contextual serializers discovered while writing must later be declared in the object: `injectContextualSerializers` forwards to that emitter.

## The code

The dispatch of the large/small case:

```kotlin
properties.size > C.PROPERTY_MAX_SIZE -> {
    val fragmented = FragmentedSerializeEmitter(
        properties = sortedProperties,
        originalClassName = originalClassName,
        writerClass = writerClass
    )
    activeEmitter = fragmented
    fragmented.emit(code = code, typeSpecBuilder = typeSpecBuilder,
        discriminator = discriminator, sealedDiscriminatorKey = sealedDiscriminatorKey)
}
```

The result is always an `override fun serialize` with a `writer` and a `value` parameter, whose body is the `CodeBlock` built by the chosen branch. The limit is 40 properties (`PROPERTY_MAX_SIZE`). Sealed dispatch asks `serializerClassName()` for the generated serializer name of each subclass, so every subclass is written by its own serializer, including its discriminator.

## Related

- [[GhostCodeGenerator]]: creates it and calls `build` for each writer.
- [[BaseSerializeEmitter]]: the base class of the two emitters it chooses between.
- [[StandardSerializeEmitter]]: the emitter for normal classes and value classes.
- [[FragmentedSerializeEmitter]]: the emitter for classes above the limit.
- [[DeserializeCodeEmitter]]: the reading-side counterpart.
- [[GhostPropertyModel]]: the properties it sorts and writes.
- [[GhostEmitterConstants]]: the size limit and templates.
