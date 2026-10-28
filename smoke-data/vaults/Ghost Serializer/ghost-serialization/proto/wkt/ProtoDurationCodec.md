---
tags: [ghost-serializer, serialization, proto, wkt, duration, parser]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoDurationCodec.kt
kind: functions
---

# ProtoDurationCodec

The internal functions that convert a `ProtoDuration` to and from text of the form `[-]<seconds>[.<fraction>]s`.

## What it is about

A proto3 JSON Duration is a string such as `"3s"`, `"1.5s"` or `"-0.000000001s"`. This file holds the parser and the formatter. [[ProtoDurationSerializer]] calls them, and [[ProtoDuration]] is the model.

## How it works

Formatting writes into a small byte array of `DUR_BUFFER_SIZE` bytes. It writes the seconds with `writeLongToBytes` from [[ProtoLongDigits]], then, if the nanos are not zero, a dot and a fraction produced by `writeNanosFraction` (shared with the timestamp codec, giving 3, 6 or 9 digits). A subtle case is a duration under one second: with `seconds == 0`, the number zero has no sign, so the code writes a leading hyphen itself when `nanos` is negative. Without it, `ProtoDuration(0, -1)` would print as `0.000000001s`.

Parsing first requires a trailing `s`, then an optional sign, then digits accumulated into a `Long`, checked after each digit against `DUR_MAX_SECONDS` (315,576,000,000), which is the range limit from the protobuf specification. A fraction, if present, may have at most nine digits and is scaled by `nanosScale`. The sign is applied to both components, so the result always satisfies the sign rule of the [[ProtoDuration]] constructor. Each failure throws `IllegalArgumentException` with a message from [[GhostProtoConstants]].

## The code

```kotlin
    if (seconds == 0L && duration.nanos < 0) {
        bytes[pos++] = TOK.CHAR_HYPHEN.code.toByte()
    }
```

The range check inside the digit loop:

```kotlin
    while (index < limitIndex && isAsciiDigit(char = durationString[index])) {
        seconds = seconds * WR.BASE_TEN + (durationString[index].code - TOK.ZERO_INT)
        if (seconds > PC.DUR_MAX_SECONDS) {
            throw IllegalArgumentException(PC.ERR_DURATION_RANGE)
        }
        index++
    }
```

Scaling a short fraction, so `.5` means 500,000,000 ns:

```kotlin
internal fun nanosScale(
    fractionDigits: Int
): Int {
    var multiplier = 1
    repeat(PC.NANOS_DIGITS - fractionDigits) { multiplier *= WR.BASE_TEN }
    return multiplier
```

## Related

- [[ProtoDuration]]: the model that is parsed into and formatted from
- [[ProtoDurationSerializer]]: the caller of `parseDuration` and `formatDuration`
- [[ProtoTimestampCodec]]: provides `writeNanosFraction` and `parseDecimalAt`
- [[ProtoLongDigits]]: provides `writeLongToBytes` for the seconds
- [[GhostProtoConstants]]: supplies limits, buffer size and messages
- [[GhostJsonTokens]]: supplies character constants
