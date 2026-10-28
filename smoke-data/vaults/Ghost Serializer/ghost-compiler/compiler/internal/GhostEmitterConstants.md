---
tags: [ghost-serializer, compiler, constants, codegen]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/internal/GhostEmitterConstants.kt
kind: object
---

# GhostEmitterConstants

The largest constants file of the compiler: code templates and snippets that the emitters put inside generated `serialize` and `deserialize` functions.

## What it is about

Emitters write Kotlin through KotlinPoet, mixing fixed text with placeholders: `%T` for a type, `%L` for a literal piece of code, `%S` for a string and `%N` for a name. The fixed text is not scattered through the emitter code but kept here. It is what a generated method body is made of, such as `reader.beginObject()`, `while (true)`, `val index = reader.selectNameAndConsume(OPTIONS)` or `writer.name(...).value(...)`. Reading this file is a quick way to see what generated code looks like.

## How it works

The constants group by topic:

- **Deserializing objects**: the begin/end object calls, the loop, `when (index)` with `-1 -> break` at the end of the object, and `skipValue`.
- **Reading values**: `reader.nextInt()` and so on for all scalar types, nullable variants, the null-check wrapper `if (reader.isNextNullValue()) {...} else {...}`, and the `readList`, `readSet` and `readMap` wrappers.
- **Proto3 JSON**: `int64` and `uint64` fields accept bare numbers or quoted strings, so the generated read temporarily turns on `coerceStringsToNumbers` and restores it afterwards (`STR_NEXT_LONG_PROTO_COERCED`). The write side uses quoted decimal strings and Base64 for bytes, and zero checks (`%L != 0`) to omit default values.
- **Bit masks**: templates such as `mask%L = mask%L or %L` and `(mask%s and %s) != 0L` track which fields were seen, to find missing required ones. `PROPERTY_MAX_SIZE = 40` is the number of fields beyond which a class is split into "fragmented" chunks (`decodeChunk`, `serializeChunk`) with a `DecodingContext`.
- **Special modes**: inferred sealed classes (`eligibilityMask`, `seenMask`), envelopes (`routePayload`, `parseTyped`), wrapped keys, custom-coder bridges and flatten.
- **Defaults**: `MAX_DEFAULT_BRANCH_COUNT = 4` allows up to 2^4 constructor branches without any `.copy()`.
- **Error messages**: two carry a comment saying they must stay in sync with `GhostJsonErrorMessages`, because runtime code matches them to give hints.

## The code

Reading a nullable value, and the proto-aware read of a 64-bit integer:

```kotlin
const val TEMPLATE_NULL_CHECK_L =
    "if (reader.isNextNullValue()) {\n  reader.consumeNull()\n  null\n} else {\n  %L\n}"

const val STR_NEXT_LONG_PROTO_COERCED =
    "run {\n" +
            "  val savedCoerce = reader.coerceStringsToNumbers\n" +
            "  reader.coerceStringsToNumbers = true\n" +
            "  val parsed = reader.nextLong()\n" +
            "  reader.coerceStringsToNumbers = savedCoerce\n" +
            "  parsed\n" +
            "}"
```

## Related

- [[BaseDeserializeEmitter]]: the main user of the reader snippets.
- [[BaseSerializeEmitter]]: the main user of the writer snippets.
- [[FragmentedEmitter]]: uses the chunk and `DecodingContext` templates.
- [[EnvelopeRouterEmitter]]: uses the envelope templates.
- [[WrappedKeysEmitter]]: uses the wrapped-keys templates.
- [[GhostCodegenConstants]]: its sibling for whole-file concerns.
- [[GhostJsonErrorMessages]]: the runtime messages that two of these must match.
