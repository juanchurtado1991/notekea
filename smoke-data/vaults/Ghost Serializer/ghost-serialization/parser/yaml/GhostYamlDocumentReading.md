---
tags: [ghost-serialization, serialization, yaml, parser]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/yaml/GhostYamlDocumentReading.kt
kind: functions
---

# GhostYamlDocumentReading

The entry points for parsing a whole YAML document, or a stream of documents separated by `---`, plus the reader reset used by the pool.

## What it is about

A YAML payload may hold one document or many. This file contains the top-level functions that start parsing: `reset`, `readDocument`, and two overloads of `readAllDocuments`. Together they handle directives, document markers and the clean-up of per-document state such as anchors and tag handles. The value itself is parsed by [[GhostYamlValueReading]].

## How it works

- **`reset(newData)`** points a reader at a new byte array and sets every field back to its initial value: position, depth, indentation, anchor and tag tables, traversal state, path tracker and the option flags (strict, coercions, `maxDepth`, `maxCollectionSize`). The thread-local pools in [[GhostYamlPools]] call it to reuse a reader.
- **`readDocument()`** clears anchors and tag directives, skips directives and `---`, skips blanks, returns an empty map if there is nothing left, and otherwise reads one value from the root.
- **`readAllDocuments()`** returns a list of raw values. The overload that takes a lambda deserializes each document into `T` directly: it prepares the cursor for the document, calls the lambda, then clears the traversal state.

Both overloads share `readAllDocumentsImpl`, an `inline` loop. For each document it clears anchors (they do not cross documents), skips directives and markers, treats a lone `...` as the end of a document that never started, reads the document, then consumes `...`. Without an explicit end, only EOF, `---` or `%` may follow; anything else raises "Unexpected content after document value". Directives are accepted only at the start or after an explicit `...`. A loop iteration that consumes no input throws "Parser made no progress".

## The code

The reset function ends by restoring the option defaults:

```kotlin
strictMode = false
coerceStringsToNumbers = false
coerceBooleans = false
maxDepth = SC.MAX_DEPTH
maxCollectionSize = GhostHeuristics.maxCollectionSize
```

The typed overload shows how the two phases connect:

```kotlin
fun <T> GhostYamlFlatReader.readAllDocuments(deserializeDocument: (GhostYamlFlatReader) -> T): List<T> =
    readAllDocumentsImpl {
        prepareRootForCurrentDocument()
        val result = deserializeDocument(this)
        clearAfterDocument()
        result
    }
```

## Related

- [[GhostYamlFlatReader]]: the receiver; `reset` is called by the pools.
- [[GhostYamlValueReading]]: `readValue` parses each document's content.
- [[GhostYamlWhitespace]]: skips directives, markers and blanks.
- [[GhostYamlCursorTraversal]]: `prepareRootForCurrentDocument` and `clearAfterDocument` live there.
- [[GhostYamlPools]]: lends readers that are reset with `reset`.
- [[GhostYamlErrorReporting]]: `yamlError` for the errors above.
- [[GhostHeuristics]]: supplies the default `maxCollectionSize`.
