---
tags: [ghost-serialization, serialization, yaml, parser]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/yaml/GhostYamlFlatReader.kt
kind: class
---

# GhostYamlFlatReader

The YAML reader: the class that holds the parser state and exposes the same cursor-style API that generated serializers use for JSON.

## What it is about

Generated `deserialize` code calls methods such as `beginObject`, `selectNameAndConsume`, `nextInt` and `endObject`. For YAML, the object receiving those calls is a `GhostYamlFlatReader` created over a `ByteArray`. The class is mostly a state container and a facade: the real work is spread over extension functions in sibling files (whitespace, scalars, block and flow style, anchors, tags and document reading), and over the cursor implementation in [[GhostYamlCursorTraversal]].

## How it works

Reading happens in two phases. First `readDocument` (see [[GhostYamlDocumentReading]]) scans the bytes once and builds an in-memory tree of `Map`, `List` and scalar values. Then the cursor methods walk that tree with iterators. That is why the class holds both byte-level fields (`position`, `limit`, `currentIndent`, `indentHasTab`, `depth`, `anchorTable`, `tagDirectives`) and tree-walking fields (`traversalStack`, `currentMap`, `mapIterator`, `currentList`, `nextValue`, ...).

The public cursor members are real members, each delegating to an `xxxImpl` extension function. The reason, noted in the source, is that code generated in other Gradle modules calls them without imports, and class members resolve through the receiver type. Options such as `strictMode`, `coerceStringsToNumbers`, `coerceBooleans`, `maxDepth` and `maxCollectionSize` are plain properties. A `GhostJsonPathTracker` records the path for error messages.

## The code

Delegating members are one-liners:

```kotlin
fun beginObject() = beginObjectImpl()
fun endObject() = endObjectImpl()
fun nextInt(): Int = nextIntImpl()
fun nextString(): String = nextStringImpl()
```

`nextByte` and `nextShort` reuse `nextInt` and narrow the result with a range check. The collection helpers are `inline` so the lambda costs nothing:

```kotlin
inline fun <T> readList(crossinline itemParser: () -> T): List<T> {
    beginArray()
    if (isNextCloseArray()) {
        endArray()
        return emptyList()
    }
    val list = ArrayList<T>()
    while (hasNextArrayElement()) {
        list.add(itemParser())
    }
    endArray()
    return list
}
```

`readSet` builds a `LinkedHashSet`, and `readMap` throws if a key parser returns null. `throwMissingRequiredField` pushes the field name on the path before throwing, so the exception points to the right place. The nested `StateFrame` class stores one level of traversal state, and `ChompStyle` (`STRIP`, `CLIP`, `KEEP`) is used by block scalars.

## Related

- [[GhostYamlCursorTraversal]]: implements the cursor members.
- [[GhostYamlDocumentReading]]: parses the bytes into the tree.
- [[GhostYamlSerializer]]: the contract that receives this reader.
- [[GhostYamlPools]]: lends a pooled reader.
- [[GhostYamlScanConstants]]: limits such as `MAX_DEPTH`.
- [[GhostJsonPathTracker]]: provides the path for errors.
- [[JsonReaderOptions]]: the type of field-name options passed to `selectNameAndConsume`.
- [[GhostYamlErrorReporting]]: `yamlError` for parse-phase failures.
