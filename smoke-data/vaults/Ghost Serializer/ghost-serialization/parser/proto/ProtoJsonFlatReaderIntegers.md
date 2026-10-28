---
tags: [ghost-serializer, serialization, parser, proto, numbers]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/proto/ProtoJsonFlatReaderIntegers.kt
kind: functions
---

# ProtoJsonFlatReaderIntegers

Reads proto3 JSON `int32`, `int64` and `uint64` values, accepting quoted numbers and zero-fraction decimals.

## What it is about

Proto3 JSON is lenient about integers: a value may be quoted (always so for 64-bit types) and `1.0` is accepted as `1`, while `1.5` is an error. The standard integer readers reject a decimal point, so this file wraps them. It gives [[GhostProtoJsonFlatReader]] the functions `nextProtoInt32`, `nextProtoInt64` and `readProtoUInt64`.

## How it works

All three go through one private inline function, `readProtoIntegral`, which takes two lambdas: how to parse an integral value and how to narrow an exact double back to the target type. The steps:

1. Peek the token to see whether the value is quoted.
2. Call `hasFractionDotAhead`, a look-ahead loop over the bytes that stops at a quote, comma, closing bracket, brace or whitespace, and reports whether a `.` appeared. It allocates nothing.
3. If quoted, switch `coerceStringsToNumbers` on temporarily, restoring the old value in a `finally`.
4. With a dot, parse as a double and convert back; the conversion must be exact (`value.toInt().toDouble() == value`) or it throws `ERR_PROTO_FRACTIONAL_INT`. Without a dot, the exact integer parser runs, so digits, exponents and overflow keep their normal rules and errors.

The unsigned narrowing also requires `value >= 0.0`, since converting a negative double to `ULong` would otherwise give a misleading result.

## The code

```kotlin
internal fun GhostProtoJsonFlatReader.nextProtoInt32(): Int = readProtoIntegral(
    parseIntegral = { nextIntExtension() },
    narrowExactDouble = { value -> value.toInt().takeIf { it.toDouble() == value } }
)
```

The core:

```kotlin
val isQuoted = peekNextToken() == TOK.QUOTE_INT
val hasDot = hasFractionDotAhead(isQuoted = isQuoted)

val previousCoercion = coerceStringsToNumbers
if (isQuoted) {
    coerceStringsToNumbers = true
}
try {
    if (hasDot) {
        return narrowExactDouble(nextDoubleExtension()) ?: throwError(PC.ERR_PROTO_FRACTIONAL_INT)
    }
    return parseIntegral()
} finally {
    coerceStringsToNumbers = previousCoercion
}
```

## Related

- [[GhostProtoJsonFlatReader]]: overrides `nextInt`, `nextLong` and `nextProtoUInt64` with these.
- [[GhostJsonFlatReaderNumbers]]: provides `nextIntExtension`, `nextLongExtension`, `nextULongExtension` and `nextDoubleExtension`.
- [[ProtoJsonFlatReaderNumbers]]: builds `uint32` on `nextProtoInt64`.
- [[ProtoInt64ValueSerializer]]: a wrapper serializer using 64-bit proto values.
- [[GhostProtoConstants]]: the `PC` error constants.
