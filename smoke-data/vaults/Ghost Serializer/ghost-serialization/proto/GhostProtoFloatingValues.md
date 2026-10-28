---
tags: [ghost-serializer, serialization, proto, floating-point]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/GhostProtoFloatingValues.kt
kind: functions
---

# GhostProtoFloatingValues

Extension functions that write `double` and `float` values to a Ghost writer using proto3's string form for NaN and infinity.

## What it is about

Plain JSON cannot express NaN or infinity, so proto3 JSON writes them as the strings `"NaN"`, `"Infinity"` and `"-Infinity"`. These `protoValue` functions apply that rule on the writer side. They are called by the KSP-generated `@GhostProtoSerialization` serializers and by the `DoubleValue` and `FloatValue` well-known types.

## How it works

Each overload computes the non-finite name through one private helper, `protoNonFiniteName`, which returns the string for NaN or either infinity, or `null` for a finite number. If it returns null, the function falls back to the writer's normal `value(number = ...)`, so finite numbers keep the shortest round-trip formatting. A `Float` stays a `Float` there (it is only widened to `Double` to test for non-finite values), so it is not printed with spurious digits.

There are overloads for [[GhostJsonWriter]] (bytes) and [[GhostJsonStringWriter]] (strings). For [[GhostYamlWriter]] the function just writes the number, because YAML already has `.nan` and `.inf`.

## The code

```kotlin
fun GhostJsonWriter.protoValue(
    number: Double
): GhostJsonWriter {
    val nonFiniteName = protoNonFiniteName(number = number) ?: return value(number = number)
    return value(text = nonFiniteName)
}
```

```kotlin
private fun protoNonFiniteName(
    number: Double
): String? = when {
    number.isNaN() -> PC.STR_NAN
    number == Double.POSITIVE_INFINITY -> PC.STR_INFINITY
    number == Double.NEGATIVE_INFINITY -> PC.STR_NEGATIVE_INFINITY
    else -> null
```

## Related

- [[GhostProtoConstants]]: supplies the `NaN` and `Infinity` literals
- [[GhostJsonWriter]]: the byte writer extended here
- [[GhostJsonStringWriter]]: the string writer extended here
- [[GhostYamlWriter]]: the YAML writer, where the extension just forwards the number
- [[ProtoDoubleValueSerializer]]: uses `protoValue` to write `DoubleValue`
- [[ProtoFloatValueSerializer]]: uses `protoValue` to write `FloatValue`
