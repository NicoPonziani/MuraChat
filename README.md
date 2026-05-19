# MuraChat

> **A Spring Boot Starter for domain-bound AI chatbots via RAG.**  
> MuraChat provides the engine. Your app provides the knowledge base.

---

## What is MuraChat?

MuraChat is a reusable Spring Boot library that gives any Java application a fully configured
AI chatbot that answers *exclusively* from the application's own documentation — never
hallucinating, never drifting off-topic.

The name comes from the Italian *mura* (city walls): the chatbot lives inside your application's
walls and never leaves them.

---

## Quick Start

Add the starter to your `pom.xml`:

```xml
<dependency>
    <groupId>com.murachat</groupId>
    <artifactId>mura-chat-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

Add minimal configuration:

```yaml
murachat:
  llm:
    api-key: ${OPENAI_API_KEY}
  ingestion:
    path: classpath:docs/
```

Place your documentation files under `src/main/resources/docs/`.

Inject the service in your controller:

```java
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatbotService chatbotService;

    public ChatController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatbotService.chat(request));
    }
}
```

---

## Architecture

MuraChat follows **Hexagonal Architecture**. The `core` module contains pure Java interfaces
and model objects with zero Spring dependencies. All Spring AI implementations live in
`autoconfigure`, cleanly separated from the public contract.

```
Your App (Controller) → ChatbotService (core) ← ChatbotServiceImpl (autoconfigure)
```

All beans are `@ConditionalOnMissingBean` — override any component by registering your own `@Bean`.

---

## Modules

| Module | Purpose |
|---|---|
| `mura-chat-core` | Public API: interfaces, records, enums. Zero Spring deps. |
| `mura-chat-spring-boot-autoconfigure` | Spring AI implementations and auto-configuration |
| `mura-chat-spring-boot-starter` | Single dependency aggregator for consuming apps |

---

## Architecture Decision Records

| ADR | Decision |
|---|---|
| [ADR-001](docs/adr/ADR-001-http-exposure.md) | HTTP exposure is the consuming app's responsibility |
| [ADR-002](docs/adr/ADR-002-three-module-structure.md) | Three-module structure with pure-Java core |
| [ADR-003](docs/adr/ADR-003-response-status-enum.md) | ResponseStatus enum instead of exceptions for expected outcomes |

---

## Project Status

| Sprint | Focus | Status |
|---|---|---|
| Sprint 1 | Multi-module scaffold, core model, port interfaces | ✅ Done |
| Sprint 2 | ChatClient auto-configuration, provider switching | 🔜 Next |
| Sprint 3 | RAG pipeline, vector store integration | ⏳ Planned |
| Sprint 4 | Domain filter, multi-turn memory | ⏳ Planned |
| Sprint 5 | Tool calling | ⏳ Planned |
| Sprint 6 | End-to-end integration, robustness | ⏳ Planned |
| Sprint 7 | Release 1.0.0 | ⏳ Planned |
