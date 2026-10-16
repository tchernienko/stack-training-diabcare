# Dette technique assumée

Points de détail identifiés en cours de route, **volontairement non corrigés sur le moment** — le
projet vise l'apprentissage de la stack (Spring Boot, DDD, hexagonal, Angular), pas un code
irréprochable. Voir `AGENTS.md` § « Niveau de rigueur attendu ».

Convention : une ligne par point, vérifiée au moment de l'ajout (pas de faux positif). `Zone` =
fichier/emplacement. `Constat` = ce qui cloche. `Pourquoi pas bloquant` = pourquoi ça peut attendre.
Cocher `Traité` une fois corrigé (garder la ligne barrée plutôt que la supprimer, pour l'historique).

**Exception — cas de test manquant sur une classe qui existe déjà** : pas de ligne ici, un
`// TODO` dans la classe de test concernée suffit (le test manquant reste visible là où on
l'implémentera). Ce fichier sert pour tout le reste : doc désynchronisée, style, wording, classes
de test qui n'existent pas encore.

## US1 — Patient

| Traité | Zone | Constat | Pourquoi pas bloquant |
|---|---|---|---|
| ☐ | `docs/roadbook/0004-gestion-des-erreurs-de-validation.md:26` | Le message dans le snippet (`"Valeur dans le futur ou aujourd'hui interdite."`) ne correspond plus au message réel de `Patient.java:45` (`"non strictement dans le passé."`) | Doc légèrement désynchronisée du code, comportement réel correct et testé |
| ☐ | `PatientTest.java:10` | Import `AssertionsForClassTypes.assertThat` au lieu de `Assertions.assertThat` (surcharges `Iterable`/tableau manquantes) | Ne mord que le jour où on asserte sur une collection dans ce fichier |
| ☐ | `Patient.java:44` | `dateNaissance.isAfter(now) \|\| dateNaissance.isEqual(now)` plutôt que `!dateNaissance.isBefore(now)` (2 appels à `now()` au lieu d'1) | Fonctionnellement correct, juste moins concis |

## US2 — Socle frontend

| Traité | Zone | Constat | Pourquoi pas bloquant |
|---|---|---|---|
| ☐ | `README.md` (avant correction) | Mentionnait `backend/Dockerfile` comme déjà en place ; le fichier n'existe pas dans le repo | Doc désynchronisée, le backend tourne en local via `mvn spring-boot:run`, pas encore besoin de l'image Docker |
| ☑ | ~~`frontend-angular/package.json` — scaffoldé avec `@angular/cli` 19.2 (Node 20.16 de l'environnement ne satisfaisait pas le pré-requis ≥22.22/24.15/26 d'Angular CLI 20+)~~ | Résolu : `ng update` (19→20→21→22) effectué avec `node-v26.8.1` disponible dans `/storage/java-dev/tools`, build vérifié OK | — |

## US2 — Patient findById (backend)

| Traité | Zone | Constat | Pourquoi pas bloquant |
|--------|---|---|---|
| ☐      | `PatientApplicationService.java:30` | `PatientNotFoundException` : message `"Pas de avec cet id %s"` (mot « patient » manquant) et `%s` reçoit l'objet `PatientId` (pas de `@ToString` → `PatientId@hash`). De plus ce message est mort : `PatientExceptionHandler#onPatientNotFound` écrase le detail via `setDetail(...)`. Un constructeur à 1 arg (`UUID`) composant son propre message serait plus net | La réponse 404 utilise le detail du handler (correct, testé) ; le message cassé ne sort que dans les logs/stacktrace |
| [X]    | `PatientExceptionHandler.java:7` | Import `ResponseEntity` inutilisé | Bruit, zéro impact runtime |
| ☐      | `PatientApplicationService.java:27` | `throws PatientNotFoundException` déclaré sur une `RuntimeException` | Légal, mais laisse croire à une exception vérifiée à la lecture |
| ☐      | `PatientResource.java` (`getPatientById`) | Id de path non-UUID (`/patients/xyz`) → `MethodArgumentTypeMismatchException` → 400 par défaut, non géré ni testé | L'US ne spécifie que le 404 pour un id valide absent ; 400 sur un id illisible est un défaut raisonnable |
| ☐      | `PatientApplicationServiceTest.java:17` | `import static AssertionsForClassTypes.assertThat` au lieu de `Assertions.assertThat` — idem `PatientTest.java:10` | Ne mord que le jour où on asserte sur une collection dans ce fichier |
