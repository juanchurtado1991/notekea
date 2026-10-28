---
tags: [ghost-serializer, api, annotations, flatten]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/annotations/GhostFlatten.kt
kind: annotation
---

# GhostFlatten

A property annotation that reads a deeply nested JSON value straight into a field, skipping wrapper classes.

## What it is about

APIs often bury a value like `{"user": {"address": {"city": "Lima"}}}`. Modelling every level means a class per level and an allocation per object. With `@GhostFlatten("user.address.city")` on a `city` property, the class stays flat. It is one of the shape-transforming annotations of `ghost-api`.

## How it works

The `path` argument is a dot-separated list of keys. During analysis the compiler groups flattened properties by path and generates a reader that descends through those nested objects itself, filling the fields as it goes and skipping everything else. No intermediate objects are created, which is the allocation saving the annotation advertises. The inverse operation, rebuilding the nesting while writing, is done by [[GhostWrap]].

Because the generated reading logic depends on JSON nesting, the annotation is incompatible with proto3 mappings ([[GhostProtoSerialization]]) and with YAML ([[GhostYamlSerialization]]).

## The code

```kotlin
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
annotation class GhostFlatten(val path: String)
```

The single `path` parameter is the dotted route. Targets are `PROPERTY` and `FIELD`, so it works on constructor properties and on backing fields. Note that the path separator is a dot, so a key that itself contains a dot would need a different approach.

## Related

- [[GhostWrap]]: the inverse, nesting a property on serialization.
- [[GhostWrappedKeys]]: a related shape annotation that groups sibling keys.
- [[FlattenOptionsGenerator]]: compiler piece that builds the flattening code.
- [[BaseDeserializeEmitter]]: emitter that produces the descent through nested objects.
- [[GhostPropertyModel]]: stores the path for each property.
- [[GhostProtoSerialization]]: cannot be combined with it.
