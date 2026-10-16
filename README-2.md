# Diabcare — projet d'apprentissage

Monolithe modulaire (DDD / hexagonal, primary/secondary), Spring Boot 4 / Java 25,
MySQL, Kafka, Keycloak (mono-realm), frontend Angular/TypeScript/Angular Material (PWA).

## Ce qui est déjà en place (le socle)

- `backend/pom.xml` — dépendances Maven (Spring Boot 4.1, Kafka, OAuth2 resource server, ArchUnit)
- `backend/src/main/java/.../bootstrap/DiabcareApplication.java` — point d'entrée Spring Boot
- `backend/src/main/resources/application.yml` — config datasource / kafka / issuer Keycloak
- `frontend-angular/` — squelette Angular + TypeScript + Angular Material + manifest PWA (aucune page métier).
  `frontend-react/` est un premier essai React/Next.js, mis de côté pour l'instant (cf. `AGENTS.md`,
  stack cible = Angular).
- `docker-compose.yml`, `frontend-angular/Dockerfile`
- `infra/keycloak/realm-export.json` — realm `diabcare`, 4 rôles, client `diabcare-frontend` (PKCE)
- `.gitlab-ci.yml` — pipeline build (backend + frontend), stage test à compléter (US12)

## Ce qui reste à implémenter (vous)

Tout le code métier : `patient/`, `rdv/`, `materiel/`, `fournisseur/`, `notification/`
(domain / application / infrastructure/primary / infrastructure/secondary de chaque module),
ainsi que `shared/` (les types partagés que vous jugerez nécessaires au fil de l'eau),
la sécurité Spring (`bootstrap/security/`), et les pages front correspondantes.

Voir `docs/user-stories/` pour le détail US par US (champs, endpoints, règles) — un répertoire par
palier, un fichier par US.

## Démarrer

```bash
docker compose up -d mysql kafka keycloak   # infra seule, pour développer le backend en local
cd backend && mvn spring-boot:run
cd frontend-angular && npm start             # ng serve, http://localhost:4200
```
