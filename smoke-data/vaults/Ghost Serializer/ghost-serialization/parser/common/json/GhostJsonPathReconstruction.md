---
tags: [ghost-serialization, serialization, parser, errors, path]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/json/GhostJsonPathReconstruction.kt
kind: object
---

# GhostJsonPathReconstruction

Rebuilds the JSONPath of an error by re-scanning the part of the document before it, so the happy path never pays for path tracking.

## What it is about

Tracking `$.a.b[2]` on every key and element would slow parsing. The in-memory readers (flat bytes and string) keep the whole document addressable, so when an error occurs they can scan the prefix once and derive the path. This object does that scan. The streaming reader releases consumed segments and so keeps eager tracking instead. It also offers `unescapeKey`, reused by other code.

## How it works

`reconstruct(tracker, start, end, limit, getByte, decodeRange)` is `inline`. It resets the tracker and walks `[start, end)` with a purely structural state machine (no schema), holding a stack of states (`STATE_ROOT`, object start, colon, value, after value, after comma, array start and so on) in an `IntArray` that starts at 256 entries and doubles if needed because a caller may raise `maxDepth`.

- On `{` or `[`, it pushes a frame on the tracker and the state stack.
- On `"`, if the state accepts a key, it finds the closing quote, decodes and unescapes the key and pushes a key frame; if it is a value, it completes a scalar.
- On a comma or colon it advances the state.
- On `}` or `]` it pops and calls `finishObjectValue` or `finishArrayValue`.
- Anything else is scanned as a scalar up to a delimiter.

A string or scalar still open at `end`, or ending exactly there, means the error happened while reading it, so its owning key stays on the path. Illegal sequences stop the scan.

`unescapeKey(raw)` resolves backslash escapes (simple letters and `\uXXXX`) with a `StringBuilder`; text without a backslash is returned untouched. It also has small helpers `findClosingQuote` and `findScalarEnd`.

## The code

```kotlin
@PublishedApi
internal fun unescapeKey(
    raw: String
): String {
    if (raw.indexOf(BACKSLASH_CHAR) < 0) return raw
    val builder = StringBuilder(raw.length)
    // …
    return builder.toString()
}
```

## Related

- [[GhostJsonPathTracker]]: the stack this scan fills.
- [[GhostJsonFlatReader]]: calls it to build the path of a byte-array parse error.
- [[GhostJsonStringReader]]: calls it for the string channel.
- [[GhostDiscriminatorPeeker]]: reuses `unescapeKey` for escaped keys and values.
- [[GhostJsonTokens]]: supplies the token codes.
- [[GhostJsonException]]: the exception that eventually shows the path.
