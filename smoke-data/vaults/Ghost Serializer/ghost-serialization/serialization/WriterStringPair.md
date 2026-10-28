---
tags: [ghost-serializer, serialization, writer, pooling, strings]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/WriterStringPair.kt
kind: class
---

# WriterStringPair

A small holder that pairs a `FlatCharArrayWriter` with the `GhostJsonStringWriter` that writes into it.

## What it is about

The string encode path needs both a growable char buffer and a JSON writer over it. Grouping them in one object lets each thread keep a single pair and reuse it for every encode. It is the character twin of `WriterSinkPair` on the byte side.

## How it works

Both fields are created once: `charWriter` is a `FlatCharArrayWriter`, and `writer` is a `GhostJsonStringWriter` built on it. The platform actuals store one pair per thread (`ThreadLocal` on the JVM, `@ThreadLocal` on Native), reset it before every use, and read the result from `charWriter.array` and `charWriter.size`. The class is `@PublishedApi internal` because inline functions that run in user code reach it.

There is a small difference in how the reset works: callers reset both the writer (to clear its depth and comma state) and the char buffer (to clear its size and trim an oversized array) before every encode, which the `acquireStringWriterPair` functions do.

## The code

```kotlin
@PublishedApi
internal class WriterStringPair {
    val charWriter = FlatCharArrayWriter()
    val writer = GhostJsonStringWriter(buffer = charWriter)
}
```

## Related

- [[FlatCharArrayWriter]]: the buffer the pair owns
- [[GhostJsonStringWriter]]: the writer the pair owns
- [[WriterSinkPair]]: byte-channel counterpart
- [[Ghost.jvmAndroid]]: keeps one pair per thread with `ThreadLocal`
- [[Ghost.nonJvm]]: keeps one pair per thread on Native
