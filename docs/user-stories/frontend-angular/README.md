# Frontend Angular — écrans Patient

Détail des écrans Angular qui consomment l'API Patient (paliers 1–2 côté backend). Ces US
développent la section « Frontend » d'US2.

Même convention que les autres US : seul le **quoi** est spécifié (fichiers concernés sous
`frontend-angular/src/app/`, comportement attendu, critères d'acceptation). Le **comment** (noms de
classes, découpage fin des composants) reste à faire. La section « Concepts et notions concernées »
est la partie centrale : elle liste ce qu'il y a à comprendre, pas à recopier.

Stack : Angular 22, TypeScript, Angular Material (thème préfabriqué Indigo/Pink, typographie Roboto
globale), composants standalone, nouvelle syntaxe de contrôle de flux (`@for` / `@if`), **zoneless**
(pas de `zone.js`, `provideZonelessChangeDetection()`).

Socle déjà en place (fait hors US) : Tailwind a été retiré au profit d'Angular Material —
`@angular/material` + `@angular/cdk` installés, thème Indigo/Pink câblé dans `angular.json`, polices
Roboto + Material Icons dans `index.html`, `body class="mat-typography mat-app-background"`. Angular
Material 22 ne dépend plus de `@angular/animations` : rien à provisionner dans `app.config.ts` pour
les animations. Les modules Material (`MatTableModule`, `MatFormFieldModule`, …) s'importent
directement dans le `imports:` de chaque composant standalone qui en a besoin.

Autre choix de socle : **zoneless**. `zone.js` retiré (`package.json`, `polyfills` de
`angular.json`), `provideZoneChangeDetection` → `provideZonelessChangeDetection()`. Conséquence pour
les écrans : la détection de changement doit être **notifiée** (signal, `AsyncPipe`, binding
d'événement) — Angular n'intercepte plus les callbacks async pour la déclencher tout seul.

- [US Angular 1 — Enabler technique : proxy de dev vers l'API Spring (CORS)](us-angular-01.md)
- [US Angular 2 — Liste des patients (`GET /api/patients`)](us-angular-02.md)
- [US Angular 3 — Détail d'un patient + gestion du 404 (`GET /api/patients/{id}`)](us-angular-03.md)
- [US Angular 4 — Formulaire de création (`POST /api/patients`)](us-angular-04.md)
- [US Angular 5 — Navigation entre les écrans (routing)](us-angular-05.md)

## Ordre suggéré

US Angular 1 → 5 → 2 → 3 → 4.

Le proxy (1) débloque tout appel réseau. Le routing (5) est le squelette dans lequel les écrans
s'insèrent — le poser tôt, quitte à brancher des composants encore vides. Puis la liste (2)
introduit modèle + service + `HttpClient` + `async` pipe, socle réutilisé par le détail (3) et le
formulaire (4).

## US de renforcement

Pas des écrans nouveaux à livrer dans le chemin fonctionnel ci-dessus : des exercices délibérés sur
des concepts déjà vus une fois (donc encore fragiles) une fois 1→5→2→3→4 en cours ou terminé, à
caser quand ça sert l'apprentissage plutôt que dans un ordre imposé.

- [US Angular 6 — Filtre de la liste par nom](us-angular-06.md) — rejoue le chaînage signal source →
  dérivé d'US Angular 3, en deux temps (`computed()` puis `httpResource` avec un vrai endpoint
  backend filtré).
- [US Angular 7 — Titre d'onglet dynamique](us-angular-07.md) — introduit `effect()`, jamais pratiqué
  jusque-là, en contraste avec `computed()`.
- [US Angular 8 — Vraie utilisation d'Angular Material](us-angular-08.md) — **non prioritaire**,
  notée pour plus tard : le socle Material est posé mais quasi inexploité (liste, nav, theming
  personnalisé).