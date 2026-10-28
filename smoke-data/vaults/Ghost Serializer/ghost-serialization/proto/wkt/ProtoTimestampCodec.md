---
tags: [ghost-serializer, serialization, proto, wkt, timestamp, calendar]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoTimestampCodec.kt
kind: functions
---

# ProtoTimestampCodec

The internal functions that convert a `ProtoTimestamp` to and from its RFC 3339 text, including the calendar arithmetic.

## What it is about

Proto3 JSON writes a Timestamp as a string like `2024-02-29T23:59:59.123Z`. This file implements both directions without any date library, so it works on every Kotlin Multiplatform target. [[ProtoTimestampSerializer]] calls `parseTimestamp` and `formatTimestamp`; the model is [[ProtoTimestamp]].

## How it works

The calendar maths is Howard Hinnant's `days_from_civil` and `civil_from_days`, which converts between a year-month-day date and a day count using 400-year eras. It is correct over the whole proleptic Gregorian range (years 0001 to 9999) and avoids the epoch-only shortcuts of libc. All the magic numbers are named `HINNANT_*` constants in [[GhostProtoConstants]].

Parsing is positional. The first 19 characters must have the shape `YYYY-MM-DDTHH:MM:SS`; each field is parsed by `parseDecimalAt`, which rejects non-digits, and each separator is checked and reported with its own message. A fraction of up to nine digits is scaled to nanoseconds with `nanosScale`. The zone is `Z` or `z`, or a `+HH:MM` / `-HH:MM` offset that is subtracted from the epoch seconds; anything else is rejected.

Formatting writes into a fixed `ByteArray` (`TS_BUFFER_SIZE`) through `writePaddedInt` instead of building strings. Fractions follow proto3's rule: exactly 0, 3, 6 or 9 digits, chosen by `writeNanosFraction` from whether the nanos are divisible by a million or a thousand, so 450,000,000 ns prints as `.450` and never `.45`. Negative seconds are floored to the previous day so pre-1970 dates format correctly.

## The code

```kotlin
internal fun dateToEpochSeconds(
    year: Int,
    month: Int,
    day: Int,
    // …
    val dayOfYear = (PC.HINNANT_MONTH_COEFF * (monthAdjustment - 3) + 2) / 5 + day - 1
    val dayOfEra = yearOfEra * PC.DAYS_PER_YEAR + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    val days = era * PC.HINNANT_DAYS_PER_ERA + dayOfEra - PC.HINNANT_EPOCH_OFFSET
    return days * PC.SECONDS_PER_DAY + hour * PC.SECONDS_PER_HOUR + minute * PC.SECONDS_PER_MINUTE + second
```

Positional parsing with a dedicated error per separator:

```kotlin
    val year = timestampString.parseDecimalAt(start = PC.TS_YEAR_START, end = PC.TS_YEAR_END)
    if (timestampString[PC.TS_YEAR_END] != TOK.CHAR_HYPHEN) {
        throw IllegalArgumentException(PC.ERR_TIMESTAMP_YEAR_HYPHEN)
    }
    val month = timestampString.parseDecimalAt(start = PC.TS_MONTH_START, end = PC.TS_MONTH_END)
```

The 0/3/6/9-digit rule:

```kotlin
    if (nanos % PC.NANOS_PER_MILLI == 0) {
        width = 3
        scale = PC.NANOS_PER_MILLI
    } else if (nanos % PC.NANOS_PER_MICRO == 0) {
        width = 6
        scale = PC.NANOS_PER_MICRO
    } else {
        width = 9
        scale = 1
    }
```

## Related

- [[ProtoTimestamp]]: the model that is parsed into and formatted from
- [[ProtoTimestampSerializer]]: the only caller of `parseTimestamp` and `formatTimestamp`
- [[ProtoDurationCodec]]: reuses `writeNanosFraction`, `parseDecimalAt` and `nanosScale` from this file
- [[GhostProtoConstants]]: supplies the offsets, buffer sizes, Hinnant constants and messages
- [[GhostJsonTokens]]: supplies character constants for separators and digits
- [[GhostJsonWriterConstants]]: supplies the base-ten constant
