# US8 — Le module Notification consomme l'événement et logue une notification

**Package** : `notification/application`, `notification/infrastructure/primary` (le `@KafkaListener`
est un adaptateur **primary** — c'est l'extérieur, Kafka, qui pilote l'appli ici, contrairement au
producteur RDV qui est **secondary**)

- Consommateur du topic `diabcare.rdv.v1`, groupe `diabcare-backend` (déjà configuré dans
  `application.yml`).
- À réception, loguez (niveau INFO) un message simulant l'envoi d'une notification : par exemple
  "Notification envoyée au patient {patientId} pour le RDV du {dateHeure}". Pas besoin de vraie
  intégration email/SMS pour ce projet.

**Critère d'acceptation** : créer un RDV via l'API déclenche, dans les logs du backend, la ligne de
notification correspondante, de façon asynchrone (le `POST /rdv` répond avant que la notification
ne soit forcément déjà loguée).

**Concepts et notions concernées**
- Kafka : `@KafkaListener`, groupe de consommateurs (consumer group), offset/commit, sémantique at-least-once (livraison possible en double — à connaître même si non traitée ici).
- Architecture hexagonale : un listener Kafka est un adapter **primary** (c'est l'extérieur — Kafka — qui pilote l'appli), à l'inverse du producteur de US7.
- Asynchrone : traitement découplé de la requête HTTP d'origine.
