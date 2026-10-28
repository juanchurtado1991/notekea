---
tags: [ghost-serializer, serialization, parser, field-select, string-channel]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/strings/GhostJsonStringReaderSubsystem.kt
kind: functions
---

# GhostJsonStringReaderSubsystem

The structural half of the `String` reader: objects, arrays, separators, null, booleans, collections and field-name selection.

## What it is about

[[GhostJsonStringReader]] keeps its own state small and moves operations into extension functions. This file holds the ones that deal with JSON structure: `beginObject`, `endObject`, `beginArray`, `endArray`, `hasNext`, `nextKey`, `consumeKeySeparator`, `consumeArraySeparator`, `nextBoolean`, `nextString`, `nextChar`, `consumeNull`, the `...OrNull` helpers, `skipValue`, `readList`, `readSet`, `readMap` and the field selectors.

## How it works

Most functions are thin wrappers over shared "Core" helpers from the common JSON package, which take lambdas for reading and mutating the reader's state. Only the representation differs between channels, so the comma masks, depth limits and trailing-comma rules behave identically everywhere.

The selector `internalSelect` has the same three tiers as the byte version, adapted to characters. First, the predicted key: it checks a closing quote after the candidate, then for names of up to eight chars it packs four chars at a time with `packChars4`, masks with `ghostCharSwarLengthMasks`, and compares with `predictedCharWord0` and `predictedCharWord1` from the options. Longer names use a four-way unrolled loop. Second, a perfect hash: `stringDispatch` is indexed by `((hash * multiplier + length) shr shift)`, and `verifyKeyMatch` confirms. Third, if the dispatch table is not exact, it scans all candidates before treating the key as unknown.

`readList`, `readSet` and `readMap` are `inline`, enforce `maxCollectionSize`, and mirror the byte reader's versions.

## The code

The packed prediction check:

```kotlin
val word0 = packChars4(chars = chars, index = start)
if (candidateLength <= SCN.LONG_CHARS) {
    (word0 and ghostCharSwarLengthMasks[candidateLength]) == options.predictedCharWord0[predicted]
} else {
    word0 == options.predictedCharWord0[predicted] &&
            (packChars4(chars = chars, index = start + SCN.LONG_CHARS) and
                    ghostCharSwarLengthMasks[candidateLength - SCN.LONG_CHARS]) ==
            options.predictedCharWord1[predicted]
}
```

The shared helper in `skipValue` and a boolean-coercion matcher read characters through `chars[it].code`.

## Related

- [[GhostJsonStringReader]]: the reader extended.
- [[GhostCharSwar]]: the packing and masks.
- [[JsonReaderOptions]]: carries `rawChars`, `stringDispatch` and predicted words.
- [[GhostJsonKeySelectHelpers]]: shared no-match and escaped-key logic.
- [[GhostJsonStructureHelpers]]: shared begin/end/hasNext helpers.
- [[GhostJsonCommaValidationHelpers]]: comma rules.
- [[GhostJsonFlatReaderSubsystem]]: the byte-reader equivalent.
