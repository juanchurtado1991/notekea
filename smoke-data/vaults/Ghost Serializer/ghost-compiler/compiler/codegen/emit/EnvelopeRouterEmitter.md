---
tags: [ghost-serializer, compiler, codegen, envelope]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/emit/EnvelopeRouterEmitter.kt
kind: class
---

# EnvelopeRouterEmitter

Emits the helper functions that route an envelope's payload to the right field or type based on a discriminator.

## What it is about

An envelope is a message with a type tag and a payload, for instance `{"type": "user", "data": {...}}`, where the payload's shape depends on the tag. `EnvelopeRouterEmitter` adds functions to the generated serializer so a caller can get the payload without decoding all alternatives. Per the class comment, the routing is zero-copy: it hands back the raw JSON slice or the already decoded field.

## How it works

`emit` adds, in order: lazily resolved serializers for mapped target types, `routePayload(envelope)`, `parsePayload(bytes)` and, when some mapping has a target type, `routeTyped(envelope)` and `parseTyped(bytes)`.

`routePayload` returns a nullable raw JSON value. In generic mode with no mappings it just returns the data field; otherwise it builds a `return when (envelope.<discriminator>) { ... }` with one branch per mapping (`"value" -> envelope.field`) and an `else` that returns the fallback mapping's field or generic data field or null. `routeTyped` has the same shape but returns `Any?`; for a raw-JSON mapping with a target type, the branch decodes the payload with the cached serializer. `parsePayload` and `parseTyped` take bytes, decode the envelope with the reader class and then call the corresponding route function.

The target serializers are `lazy`, exactly like the contextual ones in [[ContextualSerializerRegistry]]: the object may be initialized before the registry that provides them exists.

## The code

The dispatch block:

```kotlin
envelope.payloadMappings.forEach { mapping ->
    builder.add(
        C.STR_ENVELOPE_BRANCH,
        mapping.discriminatorValue,
        payloadExpression(mapping = mapping, fieldName = mapping.kotlinName, typed = typed)
    )
}
```

The typed decode expression for a raw JSON field is `envelope.%L?.let { %T.decode(it, %L) }`, using the `RawJsonDecode` helper and the cached serializer property named after the mapping. The cached property:

```kotlin
.addModifiers(KModifier.PRIVATE)
.delegate(C.TEMPLATE_LAZY_RESOLVE_SERIALIZER, ghostClass, targetType)
```

## Related

- [[GhostCodeGenerator]]: calls `emit` when the class has an envelope model.
- [[GhostEnvelopeModel]]: the analysed envelope the emitter reads.
- [[EnvelopePayloadMapping]]: one discriminator-to-field mapping.
- [[ContextualSerializerRegistry]]: uses the same lazy-resolution technique.
- [[RawJsonDecode]]: the helper used to decode a raw payload into the target type.
- [[GhostJsonEnvelope]]: the annotation that declares an envelope.
