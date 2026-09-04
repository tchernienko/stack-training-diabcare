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