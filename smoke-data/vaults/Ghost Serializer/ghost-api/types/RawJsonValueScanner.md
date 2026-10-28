---
tags: [ghost-serializer, api, types, rawjson, scanner]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/types/RawJsonValueScanner.kt
kind: object
---

# RawJsonValueScanner

An internal object that classifies a `RawJson` value and converts it to scalars straight from its bytes.

## What it is about

`RawJson` is deliberately a thin holder. All the logic behind `kind()`, `asIntOrNull()`, `asStringOrNull()` and friends lives here, so the public class stays small. The object is `internal` in the `types` package of `ghost-api`; applications never call it directly.

## How it works

Every function receives the `RawJson` and reads `storage`, `storageOffset` and `storageLength` directly, so it only allocates when a `String` result is needed. Classification looks at the first byte and verifies literals such as `true` and `null` byte by byte. Number checks (`isJsonNumberToken`) follow the JSON grammar: optional minus, either `0` or a non-zero digit run, optional fraction, optional exponent, and the token must consume the whole slice.

Integer conversion has two steps. `parseIntegerOrNull` accumulates the value negatively, which lets it represent `Long.MIN_VALUE`, and detects overflow without exceptions. If that fails because of a fraction or exponent, `exactIntegralOrNull` handles forms like `1e2` or `100e-2` by shifting decimal digits, and returns a value only if the result is exactly integral and fits in 19 digits. Strings take an ASCII fast path: if there is no backslash, the bytes are decoded in one call; otherwise `decodeJsonStringWithEscapes` handles `\n`, `\uXXXX` and surrogate pairs.

## The code

The classification function dispatches on the first byte:

```kotlin
fun kind(
    raw: RawJson
): RawJsonKind {
    if (raw.storageLength <= 0) return RawJsonKind.INVALID
    return when (raw.byteAt(relativeIndex = 0)) {
        OPEN_OBJ -> RawJsonKind.OBJECT
        OPEN_ARR -> RawJsonKind.ARRAY
        QUOTE -> RawJsonKind.STRING
        // …
        else -> RawJsonKind.INVALID
    }
}
```

`asIntOrNull` reuses the long path and rejects values outside the `Int` range. `asDoubleOrNull` takes the integer path first and falls back to decoding the text once for fractions and exponents. `asBooleanOrNull` compares exact literal lengths, so `"true "` is not a boolean. A subtle comment in the code explains why a positive token equal to `-Long.MIN_VALUE` is treated as out of range.

## Related

- [[RawJson]]: the class whose accessors delegate here.
- [[RawJsonKind]]: the enum returned by `kind`.
- [[GhostJsonNumericLimits]]: the related numeric limits used by the readers for ordinary parsing.
- [[GhostJsonEscapeHelpers]]: similar escape handling in the main readers.
