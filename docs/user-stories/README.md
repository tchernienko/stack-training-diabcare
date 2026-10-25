# User stories — Diabcare

Convention pour chaque US : package cible (domain/application/infrastructure primary|secondary),
champs à modéliser, endpoints REST, règles métier, critères d'acceptation.
Le "comment" (nom exact des classes) reste à vous — seul le "quoi" est spécifié.

Rappels d'architecture actés dans la conception (à respecter, pas à redécouvrir) :
- Monolithe modulaire, un seul module Maven, frontières par **package** (`domain` / `application` /
  `infrastructure/primary` / `infrastructure/secondary`), vérifiées par ArchUnit (US12).
- Un module ne dépend **jamais** du `domain` ni de l'`infrastructure` d'un autre module — uniquement
  de son `application` (interfaces exposées). Exemple : RDV consulte Patient via une interface de
  lecture exposée par `patient/application`, jamais via `Patient` (domain) ou `PatientJpaEntity`.
- Un port `secondary` propre à chaque module qui en a besoin (ex. RDV définit lui-même ce qu'il
  attend de Patient — pas Patient qui impose son API).
- Les IDs référencés par plusieurs modules (ex. l'ID d'un patient utilisé par RDV et Matériel) sont
  candidats à vivre dans un `shared/` — à vous de le constater en implémentant RDV, pas de le
  préconfigurer.
- Rôles Keycloak (realm `diabcare`, déjà créés dans `infra/keycloak/realm-export.json`) : `patient`,
  `soignant`, `coordinateur`, `admin`. `admin` reste un rôle **technique**, jamais utilisé pour les
  droits métier ci-dessous.

---

## Paliers

- [Palier 1 — Premier agrégat + API (Patient)](palier-1-patient/) : [US1](palier-1-patient/us01-creation-patient.md), [US2](palier-1-patient/us02-liste-patients.md)
- [Palier 2 — RDV](palier-2-rdv/) : [US3](palier-2-rdv/us03-planification-rdv.md)
- [Palier 3 — Sécurité Keycloak (mono-realm, 4 rôles)](palier-3-securite-keycloak/) : [US4](palier-3-securite-keycloak/us04-connexion-oidc.md), [US5](palier-3-securite-keycloak/us05-rdv-personnels-patient.md), [US6](palier-3-securite-keycloak/us06-soignant-refuse-creation-patient.md)
- [Palier 4 — Kafka](palier-4-kafka/) : [US7](palier-4-kafka/us07-evenement-rdv-planifie.md), [US8](palier-4-kafka/us08-consommateur-notification.md)
- [Palier 5 — Matériel + Fournisseur](palier-5-materiel-fournisseur/) : [US9](palier-5-materiel-fournisseur/us09-assignation-materiel.md), [US10](palier-5-materiel-fournisseur/us10-evenement-materiel-assigne.md)
  — + US de renforcement : [catalogue de références](palier-5-materiel-fournisseur/us13-catalogue-references.md)
- [Palier 6 — CI/CD](palier-6-ci-cd/) : [US11](palier-6-ci-cd/us11-pipeline-ci.md), [US12](palier-6-ci-cd/us12-archunit-frontieres.md)

## Frontend Angular

Les US ci-dessus spécifient surtout le backend. Le détail des écrans Angular (paliers 1–2 côté
front) vit dans un dossier dédié :

- [Frontend Angular — écrans Patient](frontend-angular/) :
  [proxy dev](frontend-angular/us-angular-01.md),
  [liste](frontend-angular/us-angular-02.md),
  [détail + 404](frontend-angular/us-angular-03.md),
  [création](frontend-angular/us-angular-04.md),
  [navigation](frontend-angular/us-angular-05.md)
  — + US de renforcement (concepts déjà vus, pas encore acquis) :
  [filtre de liste](frontend-angular/us-angular-06.md),
  [titre d'onglet dynamique](frontend-angular/us-angular-07.md),
  [Angular Material](frontend-angular/us-angular-08.md) (non prioritaire)

## Ordre suggéré

US1 → US2 → US3 → US4 → US5 → US6 → US7 → US8 → US9 → US10 → US11 → US12.

Rien n'empêche de faire US4 (sécurité) plus tôt si vous préférez sécuriser au fur et à mesure plutôt
qu'à la fin du palier 1 — dans ce cas, adaptez simplement l'ordre, la structure ci-dessus reste
valable indépendamment de la séquence choisie.
