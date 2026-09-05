# US11 — Le pipeline build et teste à chaque push

`.gitlab-ci.yml` est déjà fourni avec les stages `build`/`test` pour le backend et le build du
frontend. Rien à faire ici sauf constater que ça tourne — sauf si vous voulez enrichir (cache Maven,
build de l'image Docker, etc., en option).

**Concepts et notions concernées**
- GitLab CI : stages, jobs, `image` (exécuteur Docker), `artifacts`, mise en cache des dépendances (`.m2`, `node_modules`).
- CI/CD : notion de pipeline déclenché à chaque push.
