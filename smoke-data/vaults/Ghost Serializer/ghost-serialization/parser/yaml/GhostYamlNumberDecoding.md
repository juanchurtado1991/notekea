---
tags: [ghost-serialization, serialization, yaml, parser, numbers]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/yaml/GhostYamlNumberDecoding.kt
kind: functions
---

# GhostYamlNumberDecoding

Scans and parses the numeric forms of YAML scalars: decimal integers and floats, hexadecimal, octal and binary integers, and `.inf` and `.nan`.

## What it is about

When a plain scalar looks like a number, the reader must decide between `Long` and `Double`, and must handle the other bases that YAML 1.2 allows. This file does it directly on bytes. `readNumber` finds the extent of a number token at the current position, and `tryParseNumber` converts a byte range, returning `null` when the text is not a valid number so the caller falls back to a string. [[GhostYamlScalarDecoding]] is its main caller.

## How it works

- **Token scan** (`readNumber`): after an optional `-`, a `0x`, `0o` or `0b` prefix makes the function scan only digits valid for that base (so `-0x10` is one token); otherwise it takes digits, signs, `.` and `e`/`E`.
- **Other bases** (`tryParseNumber`): digits are accumulated in a `Long` with shifts of 4, 3 and 1 bits, and the sign is applied at the end.
- **Special floats:** text that starts with `.` is matched, in lowercase, against `.inf`, `+.inf`, `-.inf` and `.nan`; anything else is not a number.
- **Decimal:** digits are accumulated as a `Long` with an overflow guard. If the value would overflow, or a `.`, `e` or `E` appears, the scan switches to floating point: it decodes the text and parses it. Plain decimal literals use Ghost's exact cross-platform parser (`parseDecimalLiteralToDouble`) rather than the platform's `toDouble`, so results match on every target.
- **Float exactness:** `losesFloatPrecisionViaDouble` handles a "double rounding" corner case. If the double lands exactly midway between two floats, rounding twice (text to double to float) may differ from parsing the text once to a float. In that case the tree keeps the original text instead of a `Double`, and `nextFloat` parses it later; `nextDouble` still gets the same value from it. Only midpoints are checked, so ordinary values pay no second parse.
- `exactFloatOrNull` gives the single-rounding float parse for a string, used by the cursor reader.

## The code

Decimal accumulation with an overflow check:

```kotlin
if (accumulatedLongValue > (Long.MAX_VALUE - digit) / SC.DECIMAL_RADIX) {
    isFloatingPoint = true
    break
}
accumulatedLongValue = accumulatedLongValue * SC.DECIMAL_RADIX + digit
```

The shape test that guards the exact parser:

```kotlin
/** True for `-?digits(.digits)?([eE][+-]?digits)?`, the only shape the exact literal parse accepts. */
private fun isPlainDecimalLiteral(
    text: String
): Boolean {
```

## Related

- [[GhostYamlScalarDecoding]]: calls `tryParseNumber` from `interpretScalar`.
- [[GhostYamlValueReading]]: calls `readNumber` when a value starts with `-` and a digit.
- [[GhostYamlCursorTraversal]]: `nextFloatImpl` uses `exactFloatOrNull`.
- [[GhostYamlScanConstants]]: radix and shift constants.
- [[GhostYamlTokens]]: byte constants for digits and base letters.
- [[GhostDecimalLiteralParsing]]: the exact decimal parser used for plain literals.
- [[GhostYamlFlatReader]]: the receiver and provider of `isDigit`.
