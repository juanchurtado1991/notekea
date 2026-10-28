---
tags: [ghost-serializer, compiler, model, custom-coder]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/model/CustomCoderReaderKind.kt
kind: enum
---

# CustomCoderReaderKind

An enum naming the three reader types a custom coder function can take as its first parameter.

## What it is about

Ghost decodes JSON through three readers, each tuned for a different input: a streaming reader over bytes, a flat reader over one byte buffer, and a string reader for the native text channel. A developer's `@GhostDecoder` function takes one of them. The compiler needs to know which, so the generated serializer can call it directly where the types match. This enum is the label for each case, and [[CustomCoderModel]] keeps a set of them.

## How it works

[[CustomCoderAnalyzer]] looks at every overload of the function whose name was given, reads the type of the first parameter and maps it to a kind by qualified name. If the type matches none of the three, or no overload is found, the set falls back to `BYTES`.

Later, [[BaseDeserializeEmitter]] checks the reader class it is emitting for. If the function supports this kind, it is called directly. Otherwise a temporary bytes reader is created for the call (the bridge), which costs an extra object and a position copy.

So the choice here is a trade-off. A developer who writes overloads for all three readers gets the fastest path on every channel, and one who writes only the bytes version still gets correct behavior.

## The code

```kotlin
internal enum class CustomCoderReaderKind {
    /** `fun(GhostJsonReader): T` — bytes / streaming channel. */
    BYTES,

    /** `fun(GhostJsonFlatReader): T` — flat byte buffer channel. */
    FLAT,

    /** `fun(GhostJsonStringReader): T` — native string channel (textChannel). */
    STRING,
}
```

The mapping from reader class to kind in the emitter is a `when` on the simple class name: the string reader gives `STRING`, the flat reader gives `FLAT`, and anything else gives `BYTES`.

## Related

- [[CustomCoderModel]]: holds a set of these kinds.
- [[CustomCoderAnalyzer]]: detects the kinds from function signatures.
- [[BaseDeserializeEmitter]]: picks direct call or bridge by kind.
- [[GhostJsonReader]]: the reader type behind `BYTES`.
- [[GhostJsonFlatReader]]: the reader type behind `FLAT`.
- [[GhostJsonStringReader]]: the reader type behind `STRING`.
