---
tags: [ghost-serializer, compiler, codegen, deserialize]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/emit/FragmentedEmitter.kt
kind: class
---

# FragmentedEmitter

The deserialize body generator for large classes: it moves state into a `DecodingContext` class and splits the key dispatch into chunk functions.

## What it is about

A JVM method cannot exceed 64 KB of bytecode, and a single function with hundreds of locals and branches would. `FragmentedEmitter` is used by [[DeserializeCodeEmitter]] when a class has more than 40 properties. It does the same work as [[StandardEmitter]], laid out so that no single method is too big.

## How it works

1. `buildDecodingContext` creates a private class `DecodingContext` with a mutable field per property (with its initial value) and a `Long` mask field per mask index. It is added to the object only for the non-flat path; the flat and other readers reuse it.
2. The top-level JSON key names from [[DispatchNamesResolver]] are chunked by 40. Chunks follow the OPTIONS indexes, so a flattened or wrapped group with one root key is decoded by one branch.
3. Each chunk becomes a private `decodeChunkN(reader, ctx, index)` function with a `when(index)`, assigning into the context and OR-ing mask bits.
4. The main loop selects the key index and routes ranges (`start..end`) to the chunk functions; `-1` ends the loop and `-2` skips an unknown key.
5. After the loop it validates, closes the object and builds the result.

The return logic is the same idea as in the standard emitter but reads from the context: if every default has a known expression, a single constructor call with each default argument conditional on the mask bit; otherwise a required-args constructor (with one branch per combination of dependency-source defaults) followed by `.copy(...)` for present defaults. Validation loops over the masks and names the first missing required field.

Wrapped keys are not supported here: `emit` starts with a `check` that fails the compilation for such classes.

## The code

```kotlin
check(properties.none { it.wrappedKeys != null }) {
    C.STR_ERR_FRAGMENTED_WRAPPED_KEYS_1 + originalClassName + C.STR_ERR_FRAGMENTED_WRAPPED_KEYS_2
}
```

```kotlin
body.addStatement(
    C.TEMPLATE_CHUNK_CALL,
    start,
    end,
    chunkFunName
)
```

`emitContextAssignment` stores the decoded value in the context field (inside `decodeResilient` for resilient properties) and sets the mask bit.

## Related

- [[BaseDeserializeEmitter]]: the superclass.
- [[DeserializeCodeEmitter]]: chooses it when the class is large.
- [[StandardEmitter]]: the single-method alternative.
- [[FragmentedSerializeEmitter]]: the writing-side counterpart.
- [[DispatchNamesResolver]]: provides the top-level names that are chunked.
- [[GhostPropertyModel]]: the property data.
