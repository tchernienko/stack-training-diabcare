# Diabcare

Application de suivi de patients diabétiques de type 1 sous pompe : patients, rendez-vous, matériel, fournisseurs, notifications.

**C'est un projet d'apprentissage personnel, pas un produit.** Le métier est volontairement simple, pour pouvoir se concentrer sur la stack et l'architecture : Spring Boot, architecture hexagonale, DDD, sécurité Keycloak, Kafka, Angular.

Le projet est construit user story par user story, avec des tests à chaque couche et des commits conventionnels.

## Stack

| Couche | Choix |
|---|---|
| Backend | Java 25, Spring Boot 4.1, Maven (un seul module) |
| Architecture | Monolithe modulaire, DDD, hexagonal (adaptateurs primary / secondary) |
| Données | H2 en mode fichier pour l'instant ; PostgreSQL prévu |
| Sécurité | Keycloak (realm unique, 4 rôles), OAuth2 resource server : prévu |
| Messagerie | Kafka : prévu |
| Frontend | Angular, TypeScript, Angular Material, zoneless, PWA, Vitest |
| Tests backend | JUnit 5, AssertJ, Mockito, tests web et JPA |

## Architecture

Le backend est un monolithe modulaire. Chaque contexte métier (`patient`, `rdv`, puis `materiel`, `fournisseur`, `notification`) est un package découpé en quatre :

```
<contexte>/
├── domain/                      # agrégats, objets-valeurs, règles métier, exceptions du domaine
├── application/                 # cas d'usage et ports (interfaces)
└── infrastructure/
    ├── primary/                 # ce qui appelle l'application : REST, DTO, gestion d'erreurs
    └── secondary/               # ce que l'application appelle : base de données, autres contextes
```

Règles suivies :

- Les dépendances pointent vers le domaine : le domaine ne connaît ni Spring, ni JPA, ni REST.
- Un contexte ne dépend jamais du domaine ni de l'infrastructure d'un autre contexte. Quand `rdv` a besoin de savoir si un patient existe, il définit lui-même le port dont il a besoin (`CheckPatientExistencePort`). Un adaptateur secondaire l'implémente en appelant le service applicatif de `patient`.
- Les agrégats se référencent entre eux par identité (`PatientRef` dans `Rdv`), jamais par objet.
- Les frontières seront vérifiées automatiquement avec ArchUnit (US12).

Schémas : [docs/rappels-hexagonal.md](docs/rappels-hexagonal.md).

## Où en est le projet

| Palier | Contenu | État |
|---|---|---|
| 1 | Patient : création (US1), liste (US2) | Fait, testé (domaine, application, REST, JPA) |
| 2 | Rendez-vous : planification (US3) | En cours : domaine et cas d'usage faits ; reste la persistance, l'API REST et la gestion d'erreurs |
| 3 | Sécurité Keycloak : connexion OIDC, droits par rôle (US4 à US6) | À faire |
| 4 | Kafka : événement « rdv planifié » et consommateur de notification (US7, US8) | À faire |
| 5 | Matériel et fournisseurs (US9, US10, US13) | À faire |
| 6 | Pipeline CI et tests d'architecture ArchUnit (US11, US12) | À faire |

Côté frontend : squelette Angular avec la liste et le détail des patients. Le reste suit les US Angular.

Le détail des US (champs, endpoints, règles, critères d'acceptation) est dans [docs/user-stories](docs/user-stories/).

## Démarrer

Prérequis : JDK 25, Maven, Node (version dans `frontend-angular/.nvmrc`).

### Backend

```bash
cd backend
mvn spring-boot:run     # http://localhost:8080
mvn test
```

La base H2 est un fichier créé dans `backend/data/db/`. Sa console est disponible sur `/h2-console` quand l'application tourne.

### Frontend

```bash
cd frontend-angular
npm install
npm start               # http://localhost:4200, les appels /api sont redirigés vers :8080
npm test                # tests unitaires (Vitest)
```

### Docker

`docker-compose.yml` prépare l'infrastructure des paliers suivants (PostgreSQL, Kafka, Keycloak, avec le realm importé depuis `infra/keycloak/realm-export.json`). Le backend ne s'en sert pas encore : seul `docker compose up -d postgres kafka keycloak` a un sens, et uniquement quand les paliers 3 et 4 seront démarrés.

## Documentation du projet

- [docs/user-stories](docs/user-stories/) : les user stories, palier par palier.
- [docs/roadbook](docs/roadbook/) : notes de décision et pièges rencontrés, au format contexte / décision / pourquoi / conséquences.
- [docs/dette-technique](docs/dette-technique/README.md) : points connus à reprendre.
- [docs/rappels-hexagonal.md](docs/rappels-hexagonal.md) : schémas de l'architecture.

## Conventions

- Commits au format [Conventional Commits](https://www.conventionalcommits.org/) : `type(scope): description`.
- Un test par comportement, à chaque couche : domaine pur, service applicatif avec ports simulés, REST avec `@WebMvcTest`, adaptateur JPA.
