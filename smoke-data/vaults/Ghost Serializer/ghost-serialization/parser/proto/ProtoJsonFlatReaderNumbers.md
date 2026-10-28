---
tags: [ghost-serializer, serialization, parser, proto, numbers]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/proto/ProtoJsonFlatReaderNumbers.kt
kind: functions
---

# ProtoJsonFlatReaderNumbers

Proto3-aware readers for floating-point values, enums and `uint32` on [[GhostProtoJsonFlatReader]].

## What it is about

Proto3 JSON writes the special floating-point values as quoted strings (`"NaN"`, `"Infinity"`, `"-Infinity"`), lets enums appear as a name or a number, and has an unsigned 32-bit type that no signed Kotlin type holds exactly. This file adds those rules on top of the standard number readers.

## How it works

`nextProtoDouble` and `nextProtoFloat` both call the private inline `readProtoFloatingValue`, which receives the three special values and the plain parser as parameters. It peeks the token. If it is a quote, it compares the bytes after it directly: `NaN` (3 bytes plus closing quote), then `Infinity`, then `-Infinity`, checking the closing quote at the right offset. On a match it moves the position past the literal and returns the constant. If none match, a quoted number such as `"1.5"` is parsed with `withStringCoercion`, which turns `coerceStringsToNumbers` on and restores it afterward in a `finally`. An unquoted value goes straight to the normal parser. No `String` is created for the check.

`readProtoEnum` peeks: a quote means `selectString` against the options and an "unknown enum" error if nothing matches; otherwise the value is read as an integer. `readProtoUInt32` reads a 64-bit value with `nextProtoInt64` and checks it falls between 0 and `PROTO_UINT32_MAX`, returning a `Long`.

## The code

```kotlin
internal fun GhostProtoJsonFlatReader.nextProtoDouble(): Double = readProtoFloatingValue(
    nan = Double.NaN,
    positiveInfinity = Double.POSITIVE_INFINITY,
    negativeInfinity = Double.NEGATIVE_INFINITY,
    parse = { nextDoubleExtension() }
)
```

```kotlin
internal fun GhostProtoJsonFlatReader.readProtoUInt32(): Long {
    val value = nextProtoInt64()
    if (value < 0L || value > PC.PROTO_UINT32_MAX) {
        throwError(PC.ERR_PROTO_UINT32_OVERFLOW)
    }
    return value
}
```

## Related

- [[GhostProtoJsonFlatReader]]: the class whose overrides call these.
- [[ProtoJsonFlatReaderIntegers]]: `nextProtoInt64`, used by `readProtoUInt32`.
- [[GhostJsonFlatReaderNumbers]]: the standard `nextDoubleExtension` and `nextFloatExtension`.
- [[ProtoUInt32ValueSerializer]]: a wrapper serializer for unsigned 32-bit values.
- [[GhostProtoConstants]]: the `PC` constants.
