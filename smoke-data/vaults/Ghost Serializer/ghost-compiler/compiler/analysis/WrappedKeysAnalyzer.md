---
tags: [ghost-serializer, compiler, analysis, wrapped-keys]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/analysis/WrappedKeysAnalyzer.kt
kind: class
---

# WrappedKeysAnalyzer

Reads and validates `@GhostWrappedKeys`, and works out where each collapsed wire key lives inside the wrapped type.

## What it is about

`@GhostWrappedKeys` lets several top-level JSON keys be gathered into one Kotlin property whose type is a nested class. That has a wire-format logic of its own, so the analyzer was split out of [[GhostAnalyzer]] to keep class validation separate. This class does three things: finds the annotation, maps each key to a field path (needed to write the keys back), and reports invalid combinations.

## How it works

**Reading the annotation.** `resolveWrappedKeysAnnotation` looks on the property first and then on the matching primary-constructor parameter, since an annotation placed on a `val` parameter can end up on either. It uses KSP's typed `getAnnotationsByType` and, if that throws (the code notes that common-metadata KSP can fail on array arguments), falls back to reading raw KSP annotation arguments. Empty `keys` is an error. The result is a small internal `WrappedKeysConfig` that [[GhostAnalyzer]] turns into a [[WrappedKeysModel]].

**Mapping keys to fields.** `resolveWrappedUnwrapFields` takes the wrapped type and returns one [[WrappedUnwrapFieldModel]] per key. For a key it looks for a property of the wrapped type with that JSON name; otherwise it searches nested properties that are themselves wrapped-keys properties for that key, recursing and extending the Kotlin path; otherwise, for a sealed wrapped type, it looks through the subclasses (the proto3 `oneof` case) and tags the result with the subclass. If nothing matches it logs a warning only, since an unmapped key is tolerated.

**Validation.** `validateWrappedKeys` reports: the same wire key claimed by two properties, `omitIfEmpty` on a non-null property, a property that combines wrapped keys with `@GhostFlatten` or `@GhostWrap`, and a wrapped key that is also the name of a regular property.

## The code

Duplicate wire keys are detected with a map from key to owning property:

```kotlin
prop.wrappedKeys?.sourceKeys?.forEach { key ->
    val owner = wireKeys.put(key, prop.kotlinName)
    if (owner != null && owner != prop.kotlinName) {
        logger.error(
            AC.STR_ERR_WRAPPED_DUP_KEY_1 + key + AC.STR_ERR_WRAPPED_DUP_KEY_2 +
                    clazz.simpleName.asString() + AC.STR_ERR_WRAPPED_DUP_KEY_3 + owner +
                    AC.STR_ERR_WRAPPED_DUP_KEY_4 + prop.kotlinName + AC.STR_ERR_WRAPPED_DUP_KEY_5,
            clazz,
        )
    }
}
```

The sealed branch of key resolution, which tags the subclass:

```kotlin
return WrappedUnwrapFieldModel(
    jsonName = wireKey,
    kotlinPath = wrapperPath + direct.simpleName.asString(),
    isNullable = directType.isMarkedNullable,
    typeName = directType.toTypeName(),
    type = directType,
    sealedSubclassName = subclass.toClassName(),
)
```

## Related

- [[GhostAnalyzer]]: owns an instance and calls the three methods.
- [[WrappedKeysModel]]: built from the config that this class returns.
- [[WrappedUnwrapFieldModel]]: produced by `resolveWrappedUnwrapFields`.
- [[GhostWrappedKeys]]: the annotation being read.
- [[GhostAnalyzerConstants]]: argument names and error messages.
- [[GhostAnalyzerTypeHelpers]]: `getJsonName`, `isGhostType` and `hasAnnotation`.
- [[WrappedKeysEmitter]]: the emitter that uses the results.
