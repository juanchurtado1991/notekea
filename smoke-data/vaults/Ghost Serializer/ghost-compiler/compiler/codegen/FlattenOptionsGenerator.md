---
tags: [ghost-serializer, compiler, codegen, flatten, perfect-hash]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/FlattenOptionsGenerator.kt
kind: object
---

# FlattenOptionsGenerator

An object that emits one perfect-hash lookup table per nested object level for properties annotated with `@GhostFlatten` or wrapped paths.

## What it is about

When a class flattens a nested JSON structure (for example a field at `a.b.c` read into a top-level property), the reader has to open each intermediate object and look up the keys that appear at that level. Each level needs its own key table, just as the top-level object has one. `FlattenOptionsGenerator` creates those tables as private `OPTIONS_*` properties of the generated serializer. [[GhostCodeGenerator]] calls it once while building the object.

## How it works

1. It builds a tree of `FlattenNode`s from each property's `flattenPath` (or `wrapPath`). Each segment of the path is a node, and the property is attached to the node of its last segment.
2. For every root node it walks the tree recursively. Nodes with neither children nor properties are skipped.
3. For each node it computes the option name from the path so far, joined by `_` and upper-cased, with the options prefix in front.
4. It collects the distinct key names that occur one level below the node, taken from the precomputed `fullPaths` of all properties whose path starts with the node's path.
5. It asks [[PerfectHashFinder]] for a hash configuration (shift, multiplier, table size) for those names and emits the property through [[GeneratedCallFormat]].

The result is that deserialization can pick a key at any nesting level with a hash and a table lookup, with no string comparison chain.

## The code

The tree is built by walking each path and creating missing nodes:

```kotlin
path.forEachIndexed { index, segment ->
    val isLast = index == path.size - 1
    val node = currentMap.getOrPut(segment) { FlattenNode(segment = segment) }
    if (isLast) {
        node.properties.add(prop)
    } else {
        currentMap = node.children
    }
}
```

The names for a level are found by matching path prefixes:

```kotlin
val names = properties.mapIndexedNotNull { index, _ ->
    val path = fullPaths[index]
    if (path.size > depth && path.subList(0, depth) == currentPath) path[depth] else null
}.distinct()
```

The emitted property is `private`, typed as the common `JsonReaderOptions` class, and its initializer is built with `jsonReaderOptionsOf`, receiving shift, multiplier, table size, the text-channel flag, the extended-key-hash flag and the names. The recursion then repeats the process for each child with an extended prefix and path.

## Related

- [[GhostCodeGenerator]]: calls `generateNestedOptions` while assembling the serializer.
- [[PerfectHashFinder]]: finds the hash parameters for each level.
- [[GeneratedCallFormat]]: formats the generated options call.
- [[GhostPropertyModel]]: provides `flattenPath` and `wrapPath`.
- [[JsonReaderOptions]]: the runtime class the generated initializer constructs.
- [[GhostFlatten]]: the annotation that causes these tables to exist.
