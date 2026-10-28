---
tags: [ghost-serialization, serialization, base64, codec]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/GhostBase64.kt
kind: functions
---

# GhostBase64

Base64 encoding and decoding for `ByteArray` values, written without platform APIs so it works on every Kotlin target.

## What it is about

Binary data in JSON and in the Protobuf JSON mapping travels as a Base64 string. Ghost needs this on JVM, Android, Native and Wasm alike, so the codec lives in common code. The decoder accepts the standard and URL-safe alphabets, with or without `=` padding, and ignores whitespace. The encoder produces the standard alphabet with padding.

## How it works

Decoding is built in layers. `decodeBase64StringOrNull(value)` is `@PublishedApi internal` and does the work: it allocates an output array of the input length, walks the characters skipping any code at or below the space code, and gathers four significant characters at a time into a small `IntArray` chunk. Each is looked up through `lookupBase64Code`, which bounds-checks against the lookup table so a non-Latin-1 character reports "invalid" instead of throwing an index error. Padding codes drop the trailing bytes. A final partial group of two or three characters is padded virtually with `=`; a lone leftover character is invalid. The result is trimmed with `copyOf`. `null` means invalid input.

On top of that, `decodeBase64String(value)` throws `IllegalArgumentException`, and an `@InternalGhostApi inline` overload takes a `throwError` lambda so a reader can report its own error type.

`encodeBase64String(source)` computes the output length, processes three input bytes into four output bytes with shifts and masks, handles the one-byte and two-byte tails with `=` padding, and decodes the byte output to a string.

## The code

```kotlin
@InternalGhostApi
inline fun decodeBase64String(
    value: String,
    throwError: (String) -> Nothing
): ByteArray = decodeBase64StringOrNull(value = value) ?: throwError(PC.ERR_INVALID_BASE64)
```

The helpers `combineFirstByte`, `combineSecondByte` and `combineThirdByte` are inline functions that merge two six-bit values into a byte.

## Related

- [[GhostProtoConstants]]: holds the lookup table, alphabet bytes and shift constants (imported as `PC`).
- [[ProtoBytesValueSerializer]]: serializes `BytesValue` using this codec.
- [[ProtoJsonFlatReaderBase64]]: the Protobuf JSON reader code that decodes Base64 fields.
- [[GhostJsonReader]]: a reader flavour this decoder is designed to work with through `nextString()`.
- [[GhostJsonStringReader]]: the other reader flavour; the decoder works on a plain `String`.
- [[GhostJsonTokens]]: provides the space and `=` constants.
