---
tags: [ghost-serializer, serialization, proto, error-handling, wkt]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoWktStringParsing.kt
kind: functions
---

# ProtoWktStringParsing

A tiny inline helper that turns a codec's `IllegalArgumentException` into the reader's own error with a JSON path.

## What it is about

The Timestamp and Duration codecs are pure string functions and signal bad input by throwing `IllegalArgumentException`. A user decoding JSON, however, expects the channel's normal decode exception with the location of the problem. `parseWktString` bridges the two so the codecs stay independent of any reader.

## How it works

The function takes the text, a `throwError` callback that returns `Nothing`, and a `parse` lambda. It runs `parse(text)` inside a try block; on `IllegalArgumentException` it calls `throwError` with the exception message (or an empty string). Since the callback is typically `reader.throwError(...)`, the error surfaces as the exception type of whichever channel is decoding, with its path information.

Because the function is `inline`, both lambdas are inlined at the call site, so no closure objects are allocated for each decoded timestamp.

Keeping this in one place means every string-based well-known type can reuse the same error path. A new type such as a hypothetical date wrapper would only need a codec that throws `IllegalArgumentException` on bad input, plus one call to `parseWktString` in its serializer. The helper deliberately catches only `IllegalArgumentException`; any other exception type is a bug in the codec and propagates unchanged rather than being reported as bad user input.

## The code

```kotlin
internal inline fun <T> parseWktString(
    text: String,
    throwError: (String) -> Nothing,
    parse: (String) -> T
): T = try {
    parse(text)
} catch (error: IllegalArgumentException) {
    throwError(error.message.orEmpty())
```

## Related

- [[ProtoTimestampSerializer]]: wraps `parseTimestamp` with this helper
- [[ProtoDurationSerializer]]: wraps `parseDuration` with this helper
- [[ProtoTimestampCodec]]: the codec whose exceptions are translated
- [[ProtoDurationCodec]]: the codec whose exceptions are translated
- [[GhostJsonException]]: the kind of error that finally reaches the caller
