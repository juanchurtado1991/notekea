---
tags: [ghost-serializer, ktor, index]
---

# ghost-ktor

The Ktor integration: content negotiation that sends and receives DTOs through Ghost.

Part of [[Ghost Serializer]]. Its notes, by package:


## ktor

- [[GhostClientExtensions]]: Extension functions on Ktor's `HttpResponse` that decode a body with Ghost directly, without ContentNegotiation.
- [[GhostContentConverter]]: The Ktor `ContentConverter` that lets Ktor's ContentNegotiation read and write JSON through Ghost.
- [[GhostKtorBuffers]]: An internal helper that reads a Ktor body into a pooled byte buffer so converters can parse it without extra allocations.
- [[GhostKtorErrorMessages]]: An internal object holding the text of the errors raised by Ghost's Ktor bypass functions.
- [[GhostKtorExtensions]]: The three extension functions that register Ghost's converters in Ktor's ContentNegotiation.
- [[GhostKtorMediaTypes]]: An internal object with the content-type constants used for YAML negotiation in Ghost's Ktor module.
- [[GhostKtorSerializers]]: Internal helper functions shared by Ghost's three Ktor converters: serializer lookup and response encoding.
- [[GhostProtoContentConverter]]: The Ktor content converter for the proto3 JSON mapping, used with `@GhostProtoSerialization` models.
- [[GhostServerExtensions]]: Extension functions on Ktor's `ApplicationCall` that respond with Ghost-serialized bodies, bypassing ContentNegotiation.
- [[GhostYamlContentConverter]]: The Ktor content converter for YAML bodies, backed by Ghost's YAML serializers.
