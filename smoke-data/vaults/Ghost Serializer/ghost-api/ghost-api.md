---
tags: [ghost-serializer, api, index]
---

# ghost-api

The annotations and small public types a user puts on their DTOs: what to serialize, how to name fields, how strict to be.

Part of [[Ghost Serializer]]. Its notes, by package:


## annotations

- [[GhostCoerce]]: A runtime annotation that asks Ghost to accept loosely typed JSON, such as the string `"42"` where an integer is expected.
- [[GhostDecoder]]: A property annotation that hands the deserialization of one field to a function you write yourself.
- [[GhostEncoder]]: A property annotation that sends the serialization of one field to a function you provide.
- [[GhostEnvelopeFallback]]: Marks the one payload property of an envelope that receives data whose discriminator is not recognised.
- [[GhostEnvelopePayload]]: Ties one discriminator value of an envelope to one nullable payload property.
- [[GhostFallback]]: A marker annotation for the sealed subclass that catches every unknown discriminator value.
- [[GhostFlatten]]: A property annotation that reads a deeply nested JSON value straight into a field, skipping wrapper classes.
- [[GhostIgnore]]: A property annotation that removes a field from the wire in both directions.
- [[GhostJsonEnvelope]]: A class annotation that declares a data class as a JSON envelope whose payload type is named by a field next to it, not inside it.
- [[GhostName]]: A tiny property annotation that renames the key a property uses on the wire.
- [[GhostProtoSerialization]]: The class annotation that makes Ghost generate serializers following the proto3 JSON mapping.
- [[GhostResilient]]: A marker that makes a property, or every property of a class, survive bad data instead of failing the whole parse.
- [[GhostSerialization]]: The main entry-point annotation: put it on a class and the Ghost KSP plugin generates a reflection-free serializer for it.
- [[GhostSignature]]: Marks a property as a key whose presence identifies a subclass in inferred polymorphism.
- [[GhostStrict]]: A runtime annotation that makes JSON parsing strict for one endpoint, parameter or controller.
- [[GhostWrap]]: A property annotation that writes a flat property into a nested JSON object path.
- [[GhostWrappedKeys]]: A property annotation that gathers several sibling JSON keys into one Kotlin property.
- [[GhostYamlSerialization]]: An opt-in marker that adds a YAML serializer to a model that already has a JSON or proto one.

## serialization

- [[InternalGhostApi]]: An opt-in marker annotation that flags declarations as internal to Ghost and not meant for application code.

## types

- [[RawJson]]: A value class-like holder for a piece of JSON kept exactly as the bytes that arrived on the wire.
- [[RawJsonKind]]: An enum that names the seven kinds of JSON value a `RawJson` payload can hold.
- [[RawJsonValueScanner]]: An internal object that classifies a `RawJson` value and converts it to scalars straight from its bytes.
