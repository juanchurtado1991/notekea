---
tags: [ghost-serializer, spring-boot-starter, index]
---

# ghost-spring-boot-starter

The Spring Boot starter: an HTTP message converter and auto-configuration that put Ghost in front of Jackson for annotated types.

Part of [[Ghost Serializer]]. Its notes, by package:


## spring

- [[GhostAutoConfiguration]]: A marker auto-configuration class that announces Ghost's presence to Spring Boot.
- [[GhostHttpMessageConverter]]: The Spring MVC `HttpMessageConverter` that reads and writes JSON request and response bodies with Ghost.
- [[GhostReactiveDecoder]]: The WebFlux `Decoder` that reads JSON and NDJSON request bodies into Ghost models.
- [[GhostReactiveEncoder]]: The WebFlux `Encoder` that writes Ghost models as JSON or NDJSON response bodies.
- [[GhostRequestBodyAdvice]]: A Spring `@ControllerAdvice` that turns the `@GhostStrict` and `@GhostCoerce` annotations into per-request reader settings.
- [[GhostSpringConfig]]: An internal object holding two thread-local flags that carry reader options from the request advice to the converters.
- [[GhostSpringDataBuffers]]: Two small internal extension functions for turning Spring buffers into the UTF-8 byte arrays that Ghost readers expect.
- [[GhostSpringMediaTypes]]: An internal object with the media types and MIME types used by the Spring MVC converters and WebFlux codecs.
- [[GhostSpringTypeSerializers]]: An internal class that resolves Ghost serializers from Java and Spring types for the Spring converters.
- [[GhostWebFluxAutoConfiguration]]: The auto-configuration that registers Ghost's reactive encoders and decoders in a Spring WebFlux application.
- [[GhostWebMvcAutoConfiguration]]: The auto-configuration that plugs Ghost's converters into a Spring MVC (servlet) application.
- [[GhostYamlHttpMessageConverter]]: The Spring MVC converter for YAML bodies, for models that have a `GhostYamlSerializer`.
- [[GhostYamlReactiveDecoder]]: The WebFlux `Decoder` that reads YAML request bodies into Ghost models.
- [[GhostYamlReactiveEncoder]]: The WebFlux `Encoder` that writes Ghost models as YAML response bodies.
