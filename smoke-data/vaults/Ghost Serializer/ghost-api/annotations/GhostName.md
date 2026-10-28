---
tags: [ghost-serializer, api, annotations, naming]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/annotations/GhostName.kt
kind: annotation
---

# GhostName

A tiny property annotation that renames the key a property uses on the wire.

## What it is about

By default Ghost uses the Kotlin property name as the JSON, proto3 JSON or YAML key. When the external format uses another spelling, such as `user_id` instead of `userId`, you add `@GhostName("user_id")` and the generated code uses that key in every path. It lives in the `annotations` package of `ghost-api`.

## How it works

The annotation exists only at compile time. Its retention is `SOURCE`, so nothing of it reaches the bytecode: the KSP processor reads it while analysing the class, stores the chosen name in the property model, and emits the key into the generated reader and writer. The same name is used for JSON, for proto3 JSON (classes annotated with [[GhostProtoSerialization]]) and for YAML (classes annotated with [[GhostYamlSerialization]]), so one annotation keeps all formats consistent.

Because the renaming is resolved at build time, there is no runtime cost: the key is simply a constant that the generated code compares or writes.

## The code

```kotlin
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class GhostName(val name: String)
```

The only parameter is `name`, the exact wire key. It is applied to properties, not classes; for renaming a generated serializer in the registry use the `name` parameter of [[GhostSerialization]] instead, which solves a different problem (registry collisions).

## Related

- [[GhostPropertyModel]]: the compiler model that records the custom wire name for each property.
- [[GhostAnalyzerTypeHelpers]]: analysis helpers that resolve property naming.
- [[GhostIgnore]]: the sibling annotation that removes a property from the wire instead of renaming it.
- [[GhostSerialization]]: the class annotation that triggers generation where the name is used.
- [[GhostYamlSerialization]]: YAML path that honours the same name.
