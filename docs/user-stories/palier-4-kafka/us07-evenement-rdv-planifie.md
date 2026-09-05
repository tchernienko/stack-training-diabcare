# US7 — Un événement `RdvPlanifie` est publié quand un RDV est créé

**Package** : `rdv/domain` (l'événement lui-même), `rdv/infrastructure/secondary` (le producteur)

**Payload de l'événement** (à adapter, mais a minima) :

| Champ | Type |
|---|---|
| eventId | UUID |
| occurredAt | Instant |
| rdvId | String |
| patientId | String |
| dateHeure | LocalDateTime |

- Topic suggéré : `diabcare.rdv.v1` (versionnez le nom de topic dès le départ — c'est une habitude à
  prendre, pas un besoin réel à cette échelle).
- Publication déclenchée juste après la sauvegarde réussie du RDV, dans le use case applicatif
  (`rdv/application`), via un port que vous définissez et implémentez avec `KafkaTemplate` côté
  `infrastructure/secondary`.

**Critère d'acceptation** : après un `POST /rdv` réussi, un message apparaît sur le topic
(vérifiable avec `kafka-console-consumer` dans le conteneur, ou un test avec `spring-kafka-test`
déjà en dépendance de test).

**Concepts et notions concernées**
- Event-driven / DDD : notion d'événement de domaine (Domain Event).
- Kafka : topic, producteur, `KafkaTemplate`, sérialisation (JSON), clé/valeur de message, convention de nommage/versionnage de topic (`.v1`).
- Architecture hexagonale : le producteur Kafka est un adapter **secondary**, piloté par le use case applicatif (et non l'inverse).
