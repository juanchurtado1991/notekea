---
tags: [ghost-serialization, serialization, yaml, parser]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/yaml/GhostYamlCursorTraversal.kt
kind: functions
---

# GhostYamlCursorTraversal

The second phase of YAML reading: the functions that walk an already parsed tree through the same cursor API Ghost uses for JSON.

## What it is about

After [[GhostYamlDocumentReading]] has turned the bytes into nested `Map`, `List` and scalar values, generated code still wants to call `beginObject`, `nextKey`, `nextInt` and so on. This file implements each of those calls as an `xxxImpl` extension on [[GhostYamlFlatReader]], which delegates to it. No byte scanning happens here.

## How it works

State lives in the reader: `nextValue` (the value the next scalar read will take), the current map or list with its iterator, and a stack of saved frames.

- `beginObjectImpl` makes sure the root is parsed, checks that `nextValue` is a `Map` (otherwise "Expected Map but found ..."), pushes a frame and starts iterating the entries; `beginArrayImpl` does the same for a `List`.
- `selectNameAndConsumeImpl` takes the next entry, sets `nextValue` to its value and asks `JsonReaderOptions` for the index of the key. Known names push the key on the path tracker; unknown ones return `tokenUnknownName`; the end returns `tokenEndObject`.
- Scalar reads (`nextIntImpl`, `nextLongImpl`, `nextDoubleImpl`, ...) take the pending value, convert it, update the path and throw with a typed message on a mismatch. `wholeNumberOrNull` accepts integers, numeric strings and floats with no fraction that fit in a `Long`, so `1e2` can be read as an `Int` but `1.5` is rejected instead of truncated.
- `nextBooleanImpl` accepts a `Boolean`, or a string when it parses as a boolean (more permissive when `coerceBooleans` is on).
- `throwErrorImpl` builds a [[GhostYamlException]] with the path from the tracker, which is how cursor-phase errors get paths like `$.user.age`.
- For documents in a stream, `prepareRootForCurrentDocument` and `clearAfterDocument` reset state between documents.

## The code

The null check is a one-liner, because null scalars are already `null` in the tree:

```kotlin
internal fun GhostYamlFlatReader.isNextNullValueImpl(): Boolean {
    ensureRootParsed()
    return nextValue == null
}
```

Frame handling is shared by objects and arrays:

```kotlin
private fun GhostYamlFlatReader.pushTraversalFrame() {
    traversalStack.add(
        GhostYamlFlatReader.StateFrame(
            currentMap,
            mapIterator,
            currentEntry,
            currentList,
            listIterator
        )
    )
}
```

`consumeKeySeparatorImpl` is intentionally empty: there is no separator in a tree. `nextProtoUInt64Impl` turns on `coerceStringsToNumbers` temporarily so it can read the decimal text of a 64-bit unsigned value.

## Related

- [[GhostYamlFlatReader]]: owns the state and delegates every public call here.
- [[GhostYamlDocumentReading]]: builds the tree that is walked.
- [[GhostYamlException]]: thrown with a path from here.
- [[GhostYamlErrorMessages]]: the "Expected ... but found" prefixes.
- [[GhostJsonPathTracker]]: records the path of the current value.
- [[JsonReaderOptions]]: matches field names to indexes.
- [[GhostYamlNumberDecoding]]: produces the numbers found in the tree.
