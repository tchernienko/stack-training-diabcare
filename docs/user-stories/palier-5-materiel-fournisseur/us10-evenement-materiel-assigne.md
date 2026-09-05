# US10 — Un événement `MaterielAssigne` déclenche une notification différente

Même patron que US7/US8 : événement publié par `materiel/infrastructure/secondary` sur un topic
dédié (ex. `diabcare.materiel.v1`), consommé par `notification/infrastructure/primary` avec un
`@KafkaListener` distinct de celui de US8, loguant un message différent (ex. "Matériel {type}
assigné au patient {patientId}, fourni par {fournisseurId}").

**Critère d'acceptation** : les deux flux (RDV → notification, Matériel → notification) coexistent
sans interférence, chacun sur son topic, chacun avec son propre listener.

**Concepts et notions concernées**
- Réutilisation des concepts Kafka de US7/US8 : isolation de plusieurs couples topic/consumer-group coexistant, un `@KafkaListener` dédié par flux métier.
