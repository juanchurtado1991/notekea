---
tags: [ghost-serialization, serialization, parser, polymorphism]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/GhostDiscriminatorPeeker.kt
kind: object
---

# GhostDiscriminatorPeeker

Looks ahead in a JSON object to read the value of a type-discriminator key (such as `"type"`) without consuming or fully parsing anything.

## What it is about

Polymorphic deserialization needs to know which subclass to build before it can read the object's fields, but the discriminator may appear anywhere among the keys. Generated deserializers for sealed hierarchies therefore call `GhostDiscriminatorPeeker` to find it. It works on all three reader channels: `peek` for byte-backed readers (flat and streaming) and `peekChars` for the string reader. The object is `@InternalGhostApi`.

## How it works

Both entry points funnel into one private inline function, `peekInternal`, parameterized by lambdas for reading a unit, matching the key and decoding a range. This keeps a single scan algorithm for bytes and chars.

The scan walks object members one by one:

1. Skip whitespace; accept either `{` or a `"` (the string reader may already be inside the object).
2. Limit the scan to `maxDiscriminatorPeekDistance` units from [[GhostHeuristics]], so a missing key cannot cause a scan of a huge document.
3. For each member, read the key, check it against the wanted key (escaped keys are unescaped before comparing), skip `:`, then either return the string value if the key matched (a non-string value gives `null`) or skip the value whole.
4. Values are skipped by kind: strings with escape awareness, objects and arrays by depth counting (`skipBalanced`), scalars up to a delimiter.

Skipping values as a whole means a string value equal to the key name is never mistaken for the key, and stopping at the end of the object means a missing key never reads beyond a stream. Streaming reads go through `byteOrEof`; when not streaming, `peek` reads the raw array directly for speed. Escaped content is decoded through `GhostJsonPathReconstruction.unescapeKey`.

## The code

```kotlin
val getByte = if (isStreaming) {
    { pos: Int -> source.byteOrEof(index = pos) }
} else {
    { pos: Int -> rawData[pos].toInt() and BYTE_MASK }
}
```

If the key is not found within the distance, the object closes first, or the value is not a string, the result is `null`, and the caller reports a missing discriminator.

## Related

- [[GhostHeuristics]]: supplies `maxDiscriminatorPeekDistance`.
- [[GhostSource]]: used for key comparison and decoding on byte channels.
- [[GhostJsonPathReconstruction]]: provides `unescapeKey` for escaped keys and values.
- [[GhostJsonFlatReader]]: calls `peek` for polymorphic reads.
- [[GhostJsonReader]]: the streaming reader that calls `peek`.
- [[GhostJsonStringReaderSubsystem]]: the string reader side that calls `peekChars`.
- [[GhostJsonTokens]]: defines the token codes.
