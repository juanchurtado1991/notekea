---
tags: [ghost-serializer, compiler, analysis, hash]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/analysis/DispatchNamesResolver.kt
kind: object
---

# DispatchNamesResolver

A tiny helper that lists, in a stable order, the top-level JSON field names a generated deserializer has to recognize.

## What it is about

When a generated deserializer reads an object it matches each key it meets to a field using a perfect hash table and a `when (index)` block. Both must be built from the same list in the same order, otherwise the indexes would not agree. This object is the single place where that list is computed, so the table of [[SerializerSetupEmitter]] and the branches of [[StandardEmitter]] and [[FragmentedEmitter]] cannot drift apart.

## How it works

`topLevelNames` takes the property models and, for each, takes the name by which it appears at the top level of the JSON object:

1. A `@GhostWrappedKeys` property contributes all its source keys, because several wire keys map to the single property.
2. A flattened property (`@GhostFlatten`) contributes the first segment of its path, since the first key is the one at the top level; the rest is read by a sub-dispatch.
3. A wrapped property (`@GhostWrap`) likewise contributes the first segment of its path.
4. Anything else contributes its JSON name.

The result is deduplicated with `distinct()` but otherwise keeps declaration order. Deduplication matters because several flattened properties can share the same first segment, and the hash table needs each name once.

## The code

```kotlin
fun topLevelNames(
    properties: List<GhostPropertyModel>
): List<String> = properties.flatMap { prop ->
    prop.wrappedKeys?.sourceKeys ?: listOf(
        prop.flattenPath?.firstOrNull()
            ?: prop.wrapPath?.firstOrNull()
            ?: prop.jsonName,
    )
}.distinct()
```

The index of a name in this list is the index that the generated `selectNameAndConsume` returns, so the order is part of the contract.

## Related

- [[GhostPropertyModel]]: the input; provides the path and name fields.
- [[WrappedKeysModel]]: its `sourceKeys` are read here.
- [[SerializerSetupEmitter]]: builds the options and hash table from the names.
- [[StandardEmitter]]: uses the names for the standard parse loop.
- [[FragmentedEmitter]]: uses them for classes split into chunks.
- [[PerfectHashFinder]]: finds the hash for exactly this list.
