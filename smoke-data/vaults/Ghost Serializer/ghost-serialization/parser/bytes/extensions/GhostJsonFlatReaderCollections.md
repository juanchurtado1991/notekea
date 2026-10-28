---
tags: [ghost-serializer, serialization, parser, collections, resilient]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/bytes/extensions/GhostJsonFlatReaderCollections.kt
kind: functions
---

# GhostJsonFlatReaderCollections

Inline helpers that decode JSON arrays and objects into Kotlin collections, plus `decodeResilient`, which turns a bad value into `null`.

## What it is about

Generated serializers need to read `List<T>`, `Set<T>` and `Map<K, V>` fields. Rather than generate a loop for every type, they call these functions with a lambda for the element. Because they are `inline`, the lambda is expanded at the call site and there is no per-element allocation or virtual call. They are the byte-channel version of the same helpers on the other readers.

## How it works

`readList` begins an array, returns `emptyList()` for `[]`, and otherwise loops: parse an item, read the next token, stop at `]` and demand a comma otherwise. After each comma it checks the size against `maxCollectionSize`, which protects against heap exhaustion from a hostile document. `readSet` is identical but fills a `LinkedHashSet` directly, avoiding an intermediate list. `readMap` loops over key and value lambdas; a `null` key means the key parser met `}` straight after a comma, which is reported as a trailing comma. Since the comma was consumed with `nextNonWhitespace`, the loop clears that depth's bit in `needsCommaMask` so the next key does not demand a second comma.

`decodeResilient` is the error-tolerant wrapper. It saves position, cached token, depth and both comma masks, runs the block, and on a `GhostJsonException` restores everything, skips the bad value with `skipValue` and returns `null`. It is marked internal API and powers Ghost's lenient decoding of bad fields.

## The code

The list loop core:

```kotlin
while (true) {
    list.add(itemParser())
    val next = nextNonWhitespace()
    if (next == TOK.CLOSE_ARR_INT) {
        if (depth > 0) depth--
        break
    }
    if (next != TOK.COMMA_INT) {
        throwError("${EM.ERR_EXPECTED_COMMA_OR_CLOSE_ARR} but found $next")
    }
    if (list.size > maxSize) {
        throwError("${EM.ERR_MAX_COLLECTION_SIZE} ($maxSize)")
    }
}
```

Rollback in `decodeResilient`:

```kotlin
} catch (_: GhostJsonException) {
    position = savedPos
    nextTokenByte = savedToken
    depth = savedDepth
    needsCommaMask = savedNeedsCommaMask
    commaConsumedMask = savedCommaConsumedMask
    skipValue()
    return null
}
```

## Related

- [[GhostJsonFlatReader]]: the reader extended, with `beginArray`, `skipValue` and the masks.
- [[GhostJsonException]]: the exception that triggers resilient rollback.
- [[GhostHeuristics]]: defines `maxCollectionSize` and the initial capacity.
- [[ListSerializer]]: a serializer that builds on collection reading.
- [[SetSerializer]]: the set counterpart, also built on these helpers.
- [[GhostJsonReaderSubsystem]]: the streaming reader's version, which also restores the path tracker and pins the stream.
