---
tags: [ghost-serialization, serialization, parser, encoding]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/json/JsonEncodingKind.kt
kind: enum
---

# JsonEncodingKind

An internal enum naming the five text encodings Ghost recognizes at the start of a JSON document.

## What it is about

JSON text may legally arrive as UTF-8, UTF-16 or UTF-32, in either byte order. Ghost's parsers only understand UTF-8, so the entry points have to know which of the five cases they are looking at. This enum is the vocabulary for that decision: it is what encoding detection returns and what the transcoder switches on.

## How it works

The five entries are `UTF8`, `UTF16_LE`, `UTF16_BE`, `UTF32_LE` and `UTF32_BE`. Detection (`detectJsonByteEncoding`) looks at a byte-order mark first, then at the pattern of NUL bytes for BOM-less input, which works because ASCII structural characters in UTF-16 or UTF-32 are interleaved with zero bytes. If nothing matches it answers `UTF8`.

After detection, the code takes one of two routes: `UTF8` means the bytes are used as they are (after skipping any BOM), while any other value is passed to `decodeNonUtf8ToUtf8`, which routes it to `utf16ToUtf8` or `utf32ToUtf8` with a `littleEndian` flag derived from the entry. In that `when`, the `UTF8` case is marked unreachable since callers handle it first.

## The code

```kotlin
internal enum class JsonEncodingKind {
    UTF8,
    UTF16_LE,
    UTF16_BE,
    UTF32_LE,
    UTF32_BE
}
```

The enum is `internal`, so it is not part of Ghost's public API. Applications never choose an encoding; the choice is made from the data.

## Related

- [[DetectedJsonEncoding]]: pairs a kind with the BOM size.
- [[GhostJsonUtf8Input]]: detects the kind and transcodes accordingly.
- [[GhostJsonTokens]]: defines the BOM bytes and unit sizes the detection compares against.
