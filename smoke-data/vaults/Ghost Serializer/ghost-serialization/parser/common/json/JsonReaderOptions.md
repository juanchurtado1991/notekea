---
tags: [ghost-serialization, serialization, parser, dispatch]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/json/JsonReaderOptions.kt
kind: class
---

# JsonReaderOptions

The per-class field-name table that a generated deserializer hands to a reader so it can map JSON keys to field indexes very quickly.

## What it is about

For each serializable class, the compiler knows the JSON names of its fields and searches for a perfect-hash `shift`, `multiplier` and `tableSize`. At runtime the generated code builds a `JsonReaderOptions` from those numbers and the names. The readers then call `select`-style functions that hash an incoming key and read one slot of a table to get the field index, instead of comparing against every name.

## How it works

The constructor takes the names as `rawBytes` (UTF-8 `ByteArray`s) and `rawStrings`, the dispatch parameters, and flags `enableStringDispatch` and `extendedKeyHash`. In `init` it:

1. Detects collisions among the 4-byte-prefix hashes of the names, using a `HashSet<Long>` of packed (hash, length) pairs to avoid allocating pairs. `hasCollisions` is set if a collision was seen or `extendedKeyHash` was requested.
2. Fills `dispatch`, an `IntArray(tableSize)` initialized to -1, at `((key * multiplier + length) shr shift) and mask`.
3. Optionally builds the string-channel table, `stringDispatch`, which can also be built lazily on first access. The compiler's hash was found over UTF-8 bytes while this table hashes UTF-16 chars, so if two keys land in one slot `stringDispatchIsExact` becomes false and the string channel scans the names after a miss.
4. Precomputes helpers for the predicted-field fast path: `predictedKeyPadded` (names of up to 8 bytes, zero-padded to 8 so a single masked 8-byte read compares them), and `predictedCharWord0` and `predictedCharWord1` (names of up to 8 chars packed into two `Long`s).

`findOptionIndex(name)` serves YAML enum-style lookups with the same hash. A companion `of(...)` family of overloads fills in defaults (shift 0, multiplier 31, table size 1024) and funnels into `buildOptions`.

## The code

```kotlin
val perfectHashKey = ((key * multiplier + bytes.size) shr shift) and tableMask
if (dispatch[perfectHashKey] == -1) {
    dispatch[perfectHashKey] = index
}
```

## Related

- [[GhostJsonKeySelectHelpers]]: provides `computeKeyHashCore`, used to hash every name here.
- [[GhostJsonScanConstants]]: supplies the dispatch defaults and word sizes.
- [[PerfectHashFinder]]: the compiler-side search that picks `shift` and `multiplier`.
- [[GhostJsonFlatReader]]: uses the options in its select path.
- [[GhostJsonReaderSubsystem]]: streaming reader code that uses them.
- [[GhostJsonStringReaderSubsystem]]: string reader code that uses `rawChars` and the string table.
- [[GhostYamlCursorTraversal]]: YAML traversal code that also works with these options.
