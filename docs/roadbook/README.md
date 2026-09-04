# Roadbook

Notes de décision et d'apprentissage prises au fil de l'implémentation (proches d'ADR :
contexte / décision / conséquences). Elles capturent le « pourquoi » d'un choix ou l'explication
d'un piège rencontré, pour ne pas le réapprendre deux fois.

| # | Sujet | US concernée |
|---|---|---|
| [0001](0001-package-racine-et-scan-spring.md) | Placement de la classe `@SpringBootApplication` et scan des repositories / entités | US1 |
| [0002](0002-h2-fichier-et-resource-filtering.md) | Base H2 en mode fichier, Maven resource filtering, répertoire de travail | US1 |
| [0003](0003-configuration-des-logs.md) | `logging.file.name` vs `logging.file.path` | US1 |
| [0004](0004-gestion-des-erreurs-de-validation.md) | Validation métier + `@RestControllerAdvice` + `ProblemDetail` | US1 |
| [0005](0005-serialisation-des-identifiants-rest.md) | Identifiant « à plat » dans le JSON (`@JsonValue` / `@JsonCreator`) | US1 |
| [0006](0006-frontieres-hexagonales-et-transactions.md) | Frontières domaine / application / infra et placement de `@Transactional` | US1 |

## Convention

Chaque note suit la trame :

- **Contexte** — la situation, le symptôme, ou la question.
- **Décision** — ce qui a été choisi et fait.
- **Pourquoi** — le raisonnement, les alternatives écartées.
- **Conséquences** — ce que ça implique, les pièges restants, les points à revisiter.