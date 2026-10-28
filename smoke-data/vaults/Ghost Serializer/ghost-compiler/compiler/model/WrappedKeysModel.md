---
tags: [ghost-serializer, compiler, model, wrapped-keys]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/model/WrappedKeysModel.kt
kind: class
---

# WrappedKeysModel

A small record with everything `@GhostWrappedKeys` adds to one property.

## What it is about

`@GhostWrappedKeys` lets several top-level JSON keys be collapsed into a single Kotlin property of a nested type. The analyzer computes four pieces of information for it, and this class bundles them. It sits in the model layer between [[WrappedKeysAnalyzer]], which produces it, and the emitters that read or write wrapped properties.

## How it works

The data are kept together on purpose, as the KDoc explains: a property either has all four or none, and emitters that deal with wrapped keys depend on this type and not on the whole [[GhostPropertyModel]]. In [[GhostPropertyModel]] the field is `wrappedKeys: WrappedKeysModel?`, so null means "not a wrapped-keys property".

The data say:

- which wire keys are swallowed by the property (`sourceKeys`),
- whether an empty or all-null capture should make the property null (`omitIfEmpty`),
- which keys force a null result when absent or JSON null (`omitIfAbsent`),
- and where each key lives inside the wrapped type (`unwrapFields`), which the writer needs to split the object back into top-level keys.

No logic lives here, only data.

## The code

```kotlin
internal data class WrappedKeysModel(
    val sourceKeys: List<String>,
    val omitIfEmpty: Boolean,
    val omitIfAbsent: List<String>,
    val unwrapFields: List<WrappedUnwrapFieldModel>,
)
```

`sourceKeys` also takes part in dispatch: [[DispatchNamesResolver]] uses them instead of the property's own JSON name when it builds the list of top-level names that the perfect hash must separate. Each entry of `unwrapFields` is a [[WrappedUnwrapFieldModel]], with the JSON name, the Kotlin path into the wrapped value, nullability and type.

## Related

- [[GhostPropertyModel]]: holds this as `wrappedKeys`.
- [[WrappedKeysAnalyzer]]: creates it and validates the annotation arguments.
- [[WrappedUnwrapFieldModel]]: the element type of `unwrapFields`.
- [[WrappedKeysEmitter]]: emits the capture and materialize code from it.
- [[DispatchNamesResolver]]: reads `sourceKeys` for top-level names.
