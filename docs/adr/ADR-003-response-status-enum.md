# ADR-003: ResponseStatus come enum invece di eccezioni per comportamenti attesi

**Data:** 2026-05-06
**Stato:** Accettata

## Contesto

Quando il chatbot rifiuta una domanda off-topic o non trova documenti rilevanti,
come comunica questo stato al chiamante? Due approcci:
1. Lanciare un'eccezione checked/unchecked
2. Includere uno stato semantico nel valore di ritorno

## Decisione

Usare `ResponseStatus` enum nel `ChatResponse`. Le eccezioni sono riservate
esclusivamente ai fallimenti infrastrutturali (LLM non raggiungibile, vector store down).

## Motivazioni

- Un chatbot che rifiuta una domanda off-topic **non è in errore** — è il comportamento corretto
- Le eccezioni per flow control rendono il codice del chiamante più complesso (try-catch nel path normale)
- `ResponseStatus` permette al chiamante di distinguere semanticamente i casi con un semplice `switch`
- Allineato con le best practice REST: HTTP 200 con stato semantico vs HTTP 4xx/5xx

## Conseguenze

- Il codice del chiamante è più pulito: `if (response.isSuccessful()) { ... }`
- Le eccezioni rimangono segnali di problemi reali, non di comportamenti attesi
- L'app consumatrice può loggare/monitorare ogni `ResponseStatus` indipendentemente
