---
tags: [ghost-serialization, serialization, parser, errors, path]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/json/GhostJsonPathTracker.kt
kind: class
---

# GhostJsonPathTracker

A compact stack that remembers where the parser is in the document, so a parse error can say `$.items[3].name` instead of only a byte position.

## What it is about

Error messages are far more useful with a JSONPath. Tracking the path must cost almost nothing on the happy path, so this class pushes and pops integers and string references only, and builds text with a `StringBuilder` only when an exception is actually created. It is used by the readers that track eagerly (the streaming reader, which discards consumed bytes) and as the output holder when the in-memory readers reconstruct a path after an error.

## How it works

Each stack frame is one of three kinds: a key (object field name), an array (element index), or an anonymous object frame (so a nested object does not pop its parent's key). The kind and array index share one `Int` in `slots`: the low two bits are the kind tag, and the upper 30 bits hold the index biased by +1, so the "before first element" index of -1 is stored as 0 with no sign bit. Names live in a parallel `keys` array. Stale data left in a slot is never read because `formatPath` consults only the field relevant to each kind.

Operations: `pushKey`, `pushObject`, `pushArray`, `enterArrayElement` (increments the index, clamping at the maximum instead of wrapping), and three finish calls: `finishScalarValue` pops the owning key; `finishObjectValue` pops the object frame and then the key; `finishArrayValue` pops the array frame and then the key. `mark`, `reset` and `resetTo` support rollback. Arrays double in size when full.

`formatPath` starts at `$`, appends `.name` for simple names (letters, digits, `_`, `$`) or `['odd name']` otherwise, and `[n]` for array elements.

## The code

```kotlin
@PublishedApi
internal fun enterArrayElement() {
    if (size == 0) return
    val i = size - 1
    val slot = slots[i]
    if (slot and KIND_MASK != KIND_ARRAY) return
    val biased = slot ushr INDEX_SHIFT
    if (biased < MAX_BIASED_INDEX) {
        slots[i] = slot + INDEX_UNIT
    }
}
```

## Related

- [[GhostJsonPathReconstruction]]: fills a tracker by rescanning the input after an error.
- [[GhostJsonException]]: calls `formatPath` when it is constructed.
- [[GhostJsonFlatReader]]: keeps a tracker for its error path.
- [[GhostJsonReader]]: the streaming reader that tracks eagerly.
- [[GhostJsonStringReader]]: the string reader that uses a tracker too.
- [[GhostYamlFlatReader]]: also references this tracker.
