---
tags: [ghost-serializer, compiler, codegen, serialize]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/emit/FragmentedSerializeEmitter.kt
kind: class
---

# FragmentedSerializeEmitter

The serialize emitter for large classes: it spreads the property writes over several private "chunk" functions.

## What it is about

The JVM limits a method to 64 KB of bytecode, and a data class with hundreds of properties would produce a `serialize` function that exceeds it. `FragmentedSerializeEmitter` avoids that by writing the properties in groups. [[SerializeCodeEmitter]] selects it when a class has more properties than the standard limit of 40.

## How it works

`emit` writes `beginObject`, then the optional sealed discriminator (as in [[StandardSerializeEmitter]]), and splits the properties with `chunked(DEFAULT_CHUNK_SIZE)`, which equals the standard limit. For each chunk it generates a private function named from a template with the chunk prefix, the index and the writer class name (so the byte writer and the string writer get distinct functions), and adds a call to it in the main body. After all chunks it closes open paths and writes `endObject`.

The tricky part is flattened or wrapped properties: a nested object may open in one chunk and close in a later one. Chunks always run in order, so a single `currentPath` list is shared at generation time and passed to every chunk. Each chunk begins by calling `emitPathTransition` for each property, which emits the `endObject` or `name + beginObject` statements needed, and the final close is emitted in the main function.

## The code

The loop that creates and calls the chunks:

```kotlin
chunks.forEachIndexed { index, chunkProps ->
    val chunkFunName = C.TEMPLATE_CHUNK_FUN_NAME
        .format(C.STR_SERIALIZE_CHUNK_PREFIX, index, writerSuffix)

    emitChunkFunction(index, chunkProps, typeSpecBuilder, writerSuffix, currentPath)
    code.addStatement(C.TEMPLATE_CHUNK_CALL_WRITER, chunkFunName)
}
```

Each chunk function takes the writer and the value as parameters:

```kotlin
.addModifiers(KModifier.PRIVATE)
.addParameter(C.STR_PARAM_WRITER, writerClass)
.addParameter(C.STR_PARAM_VALUE, originalClassName)
```

The function is added straight to the serializer `TypeSpec`, which is why `emit` receives the builder, unlike the standard emitter.

## Related

- [[BaseSerializeEmitter]]: the superclass supplying `emitProperty` and path handling.
- [[SerializeCodeEmitter]]: picks this class for classes over the size limit.
- [[StandardSerializeEmitter]]: the inline version for smaller classes.
- [[FragmentedEmitter]]: the deserialize-side counterpart for large classes.
- [[GhostEmitterConstants]]: the chunk size and function-name templates.
