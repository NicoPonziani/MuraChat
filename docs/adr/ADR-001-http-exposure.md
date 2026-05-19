# ADR-001: Esposizione HTTP come responsabilità dell'app consumatrice

**Data:** 2026-05-06
**Stato:** Accettata

## Contesto

MuraChat può essere usata in applicazioni con strategie di sicurezza eterogenee:
JWT, sessioni, API key, nessuna auth (app interne). Qualsiasi scelta hardcoded
di sicurezza nella libreria sarebbe sbagliata per almeno una categoria di consumatori.

## Decisione

La libreria espone esclusivamente un `ChatbotService` come bean Spring.
Nessun controller REST, nessuna configurazione WebSocket, nessun filtro di sicurezza
sono inclusi nel modulo core (v1.0).

L'app consumatrice è responsabile di:
1. Creare il proprio adapter HTTP (controller REST, WebSocket, ecc.)
2. Proteggere l'endpoint con la propria `SecurityFilterChain`
3. Mappare il proprio modello di request/response verso `ChatRequest`/`ChatResponse`

## Conseguenze

- Integrazione in ~10 righe di controller da parte dell'app consumatrice
- Zero conflitti con la security esistente dell'app
- Il web layer opzionale è rinviato a v1.1 come `mura-chat-web-spring-boot-starter`

## Alternative Considerate

**Endpoint con `@ConditionalOnProperty`** — scartato perché inquina il modulo core
con dipendenze HTTP e crea ambiguità sulla security ownership.

**Web starter separato da subito** — rinviato a v1.1; il contratto del service layer
deve essere stabile e testato in produzione prima di aggiungere complessità.
