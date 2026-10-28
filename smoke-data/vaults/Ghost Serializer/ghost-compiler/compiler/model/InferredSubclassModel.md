---
tags: [ghost-serializer, compiler, model, polymorphism]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/model/InferredSubclassModel.kt
kind: class
---

# InferredSubclassModel

Pairs a subclass of a sealed class with its analyzed properties, so inferred polymorphism can choose between subclasses by looking at the keys.

## What it is about

A sealed hierarchy is usually decoded with a type discriminator field (`"type": "dog"`). Ghost can also work without one: with `inferred = true` it looks at which fields appear in the JSON and picks the subclass they fit. For that the compiler needs, for each subclass, its declaration and the property models of its fields. This two-field record holds them. It is a model-layer type and appears in a property as `inferredSubclasses`.

## How it works

At analysis time, the analyzer fills a list of these for the property that carries the inferred subclasses. At code generation, the deserializer emitter gathers the distinct JSON names of all subclasses and builds a `seenMask` and an `eligibilityMask`. For every name it reads, the generated loop sets that name's bit in `seenMask` and clears from `eligibilityMask` the bits of the subclasses that do not own that field. After the loop, a `when` has one branch per subclass, combining the two masks with the subclass's required-field mask. If no branch fits, the code throws an error that prints both masks, which helps debugging.

[[TextChannelPlanner]] also reads these records: a subclass and the properties of its fields count as dependencies when deciding which classes need the string reader.

## The code

```kotlin
internal data class InferredSubclassModel(
    val declaration: KSClassDeclaration,
    val properties: List<GhostPropertyModel>
)
```

The declaration is the KSP class (so code can reference it and read annotations), and `properties` is the list of the same [[GhostPropertyModel]] type used everywhere else, which lets emitters treat a subclass's fields like any class's fields.

## Related

- [[GhostPropertyModel]]: holds a list of these as `inferredSubclasses` and is the type of `properties` here.
- [[GhostAnalyzer]]: builds the records.
- [[DeserializeCodeEmitter]]: emits the mask-based subclass decision from them.
- [[TextChannelPlanner]]: follows them when it propagates the text channel.
