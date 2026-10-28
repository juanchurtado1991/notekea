---
tags: [ghost-serializer, serialization, proto, constants]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/GhostProtoConstants.kt
kind: object
---

# GhostProtoConstants

A single object holding every literal the proto3 JSON support needs: type names, error messages, calendar arithmetic constants and the base64 tables.

## What it is about

Ghost avoids magic numbers and repeated strings, so the proto module collects them in `GhostProtoConstants`. It is imported under the alias `PC` by the well-known-type serializers, codecs and the proto reader. Nothing in it has behaviour, except two lookup tables built once at class initialisation.

## How it works

The constants are grouped by purpose. There are the quoted `NaN`/`Infinity`/`-Infinity` literals and byte codes used to recognise them; the offsets of each field inside an RFC 3339 timestamp (`YYYY-MM-DDTHH:MM:SS`); buffer sizes for formatting timestamps, durations and longs; the constants of Howard Hinnant's days-from-civil algorithm; and the fully qualified names of the sixteen well-known types, plus the `@type` and `value` keys of `Any`.

The last section is base64, proto3's only mapping for `bytes`. `BASE64_ALPHABET_BYTES` is the encode table, and `BASE64_LUT` is a 256-entry decode table initialised to an "invalid" marker and filled for letters, digits, `+` and `/`. It also accepts the URL-safe `-` and `_` and maps `=` to a padding code, so the decoder is lenient on input while the encoder stays canonical.

## The code

```kotlin
    const val WKT_ANY_TYPE = "google.protobuf.Any"
    const val WKT_STRUCT_TYPE = "google.protobuf.Struct"
    const val WKT_VALUE_TYPE = "google.protobuf.Value"
    const val WKT_EMPTY_TYPE = "google.protobuf.Empty"
```

Error messages are shared so the same text appears whichever reader reports it:

```kotlin
    const val ERR_DURATION_SIGN = "Coherence error: seconds and nanos signs must match"
    const val ERR_DURATION_SUFFIX = "Missing 's' suffix in Duration"
    const val ERR_DURATION_FORMAT = "Malformed Duration: expected [-]<seconds>[.<up to 9 fraction digits>]s"
    const val ERR_DURATION_RANGE = "Duration out of range: seconds must be within ±315576000000"
```

The decode table is the one non-trivial initialiser:

```kotlin
    val BASE64_LUT = IntArray(size = 256) { B64_INVALID_CODE }.apply {
        for (i in 0..25) {
            this['A'.code + i] = i
            this['a'.code + i] = 26 + i
        }
        for (i in 0..9) {
            this['0'.code + i] = 52 + i
        }
        this['+'.code] = 62
        this['/'.code] = 63
        this['-'.code] = 62 // URL-safe
        this['_'.code] = 63 // URL-safe
        this['='.code] = B64_PADDING_CODE
    }
```

## Related

- [[GhostProtoFloatingValues]]: uses the `NaN`/`Infinity` literals
- [[ProtoTimestampCodec]]: uses the timestamp offsets and Hinnant constants
- [[ProtoDurationCodec]]: uses the duration limits and error messages
- [[ProtoAnySerializer]]: uses the `@type` and `value` key names
- [[GhostBase64]]: uses the base64 tables for bytes values
- [[ProtoJsonFlatReaderBase64]]: decodes proto bytes with `BASE64_LUT`
- [[GhostJsonTokens]]: the general token constants that these ones complement
