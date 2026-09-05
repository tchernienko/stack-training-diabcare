# US1 — Le coordinateur crée un patient

**Package** : `patient/domain`, `patient/application`, `patient/infrastructure/primary`, `patient/infrastructure/secondary`

**Champs de l'agrégat `Patient`**

| Champ | Type | Règle de validation |
|---|---|---|
| id | identifiant (UUID) | généré à la création |
| nom | String | non vide |
| dateNaissance | LocalDate | dans le passé |
| typePompe | enum (`PATCH`, `TUBULAIRE`) | non nul |

La validation vit dans le constructeur/factory de l'agrégat — pas dans le DTO, pas dans le
controller. Un `Patient` invalide ne doit pas pouvoir exister en mémoire.

**Endpoint**

`POST /patients` — rôle requis : `coordinateur`
- Corps : `nom`, `dateNaissance`, `typePompe`
- 201 avec le patient créé (id, nom, dateNaissance, typePompe) si valide
- 400 si validation échoue (nom vide, date future, type de pompe absent)

**Critères d'acceptation**
- Un `soignant` qui appelle cet endpoint reçoit 403 (cf. US6). Attention, pas de rôle existant à ce stade donc critère pas pertinent.
- Le patient est persisté en base (table à votre convention, ex. `patient`).

**Concepts et notions concernées**
- DDD / hexagonal : agrégat, invariant métier porté par le constructeur/factory (pas de setter), value object vs entity, séparation domain / application / infrastructure.
- Spring Boot : `@RestController`, DTO de requête/réponse, mapping domain ↔ DTO, gestion centralisée des erreurs (`@ControllerAdvice`, `@ExceptionHandler`), codes HTTP 201/400.
- Persistence : JPA/Hibernate (`@Entity`, `@Enumerated`), distinction entre l'agrégat domain et l'entité JPA (et leur mapping), génération d'UUID, DDL MySQL.
- Tests : JUnit 5, AssertJ, `@WebMvcTest`/MockMvc (controller isolé), `@DataJpaTest` (repository).
