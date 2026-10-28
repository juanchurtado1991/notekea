---
tags: [ghost-serializer, api, types, rawjson]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/types/RawJsonKind.kt
kind: enum
---

# RawJsonKind

An enum that names the seven kinds of JSON value a `RawJson` payload can hold.

## What it is about

When a model keeps part of a document as `RawJson`, the code that receives it often needs to know whether it is an object, a list, a number and so on before deciding how to read it. `RawJsonKind` is that answer, returned by `RawJson.kind()`. It sits in the `types` package of `ghost-api`, next to the class it describes.

## How it works

The classification is based only on the first token of the payload, which is why it is cheap and never builds a parse tree. The scanner looks at the first byte: `{` is an object, `[` an array, a quote a string, a digit or minus sign a number (confirmed by a full number check), and the letters `t`, `f` and `n` are checked against the exact literals `true`, `false` and `null`.

The last constant, `INVALID`, is the safe bucket for empty or whitespace-only payloads and for anything that fails at the first token. Callers can therefore branch on a closed set of cases with a `when` expression and no exceptions.

## The code

```kotlin
enum class RawJsonKind {
    /** `{` … `}` */
    OBJECT,
    /** `"` … `"` */
    STRING,
    /** JSON number (integer, fraction, or exponent form). */
    NUMBER,
    /** `true` or `false`. */
    BOOLEAN,
    /** `null`. */
    NULL,
    // …
}
```

The full list is `OBJECT`, `ARRAY`, `STRING`, `NUMBER`, `BOOLEAN`, `NULL` and `INVALID`. Note that `true` and `false` share one constant, `BOOLEAN`.

## Related

- [[RawJson]]: exposes `kind()` that returns this enum.
- [[RawJsonValueScanner]]: the object that computes the classification.
- [[RawJsonSerializer]]: the serializer that reads and writes `RawJson` fields.
- [[GhostJsonValueCaptureScan]]: the capture logic that produces `RawJson` slices in the first place.
