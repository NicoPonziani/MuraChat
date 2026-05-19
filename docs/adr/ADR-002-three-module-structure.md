# ADR-002: Struttura a tre moduli con core Java-puro

**Data:** 2026-05-06
**Stato:** Accettata

## Contesto

Una libreria Spring Boot tipica ha due moduli: `autoconfigure` e `starter`.
Aggiungere un terzo modulo `core` ha un costo (build più complessa) ma porta
vantaggi architetturali significativi.

## Decisione

Il progetto è strutturato in tre moduli:
- `mura-chat-core`: interfacce e model objects — zero dipendenze Spring o Spring AI
- `mura-chat-spring-boot-autoconfigure`: implementazioni con Spring AI
- `mura-chat-spring-boot-starter`: aggregatore pom.xml

## Conseguenze

- Il contratto pubblico (`ChatbotService`, `ChatRequest`, `ChatResponse`) è testabile
  senza Spring context — test istantanei, zero boilerplate
- Il web starter futuro (`mura-chat-web-spring-boot-starter`) dipende da `core`,
  non da `autoconfigure` — nessuna dipendenza circolare
- L'app consumatrice può implementare le porte secondarie (`QueryClassifier`, ecc.)
  senza portarsi dietro Spring AI come dipendenza transitiva

## Grafo dipendenze

```
starter → autoconfigure → core
```

Unidirezionale e aciclico.
