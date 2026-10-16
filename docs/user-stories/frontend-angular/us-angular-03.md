# US Angular 3 — Le soignant ouvre la fiche d'un patient (et tombe parfois sur un 404)

Dépend d'US Angular 2 (modèle + service patients) et d'US Angular 5 (route paramétrée).

**API consommée**

- `GET /api/patients/{id}` → `200` avec `{ id, nom, dateNaissance, typePompe }`.
- `404` si l'`id` est un UUID valide mais inconnu — corps au format `ProblemDetail` :
  `type`, `title` (« Patient inexistant »), `status`, `detail`, `idNotFound`.
- `400` si le segment d'URL n'est **pas** un UUID (converti côté Spring avant d'atteindre le
  contrôleur).

**Frontend** (`frontend-angular/src/app/patients/`)

- Route paramétrée `/patients/:id` (déclarée en US Angular 5).
- Un **composant standalone** « détail » : lit le paramètre `id` de la route, appelle le service,
  affiche les quatre champs.
- Une méthode `findById(id): Observable<Patient>` sur le service patients.
- **Gestion du `404`** : écran « patient introuvable » explicite (jamais l'erreur brute), avec un
  retour vers la liste.
- **Distinguer** un `404` (cas métier : id valide, patient absent) d'une panne technique (réseau,
  `5xx`) → deux messages différents, deux tonalités différentes.

**Critères d'acceptation**

- `/patients/<id existant>` affiche les quatre champs.
- `/patients/<uuid inexistant>` : message « introuvable » clair + lien retour, visuellement
  distinct d'une erreur serveur, **sans** `console.error`.
- `/patients/<pas-un-uuid>` : comportement défini et assumé (le backend renvoie `400`) — même écran
  « introuvable » ou message « identifiant invalide », au choix, mais pas un crash.
- Le bouton « retour à la liste » fonctionne.
- Naviguer d'une fiche à une autre (même composant réutilisé) recharge bien les données.

**Concepts et notions concernées**

- **Routing paramétré** : `ActivatedRoute`, `paramMap` (un `Observable`, réagit quand seul le
  paramètre change sans recréer le composant) vs `snapshot.paramMap` (lecture unique, statique) —
  quand chacun suffit. Alternative moderne : `withComponentInputBinding()` qui injecte `:id`
  directement comme `@Input()` (voir US Angular 5).
- **Composition de flux RxJS** : `switchMap` pour enchaîner « le paramètre de route émet → nouvelle
  requête HTTP » ; pourquoi `switchMap` (**annule** la requête précédente encore en vol) plutôt que
  `mergeMap` / `concatMap` / `exhaustMap` dans ce cas précis.
- **`HttpErrorResponse`** : forme d'une erreur dans `HttpClient` ; `status === 0` = échec réseau /
  CORS (pas de réponse HTTP), `status` 4xx/5xx = réponse d'erreur du serveur ; le corps d'erreur
  est dans `error.error`.
- **`catchError`** : à quel niveau l'intercepter (service ? composant ?), renvoyer un `Observable`
  de repli vs `throwError(() => …)`, `EMPTY` pour « terminer proprement sans valeur ». Idée de
  *rethrow* sélectif : traiter le `404` ici, laisser filer le reste.
- **`ProblemDetail` (RFC 9457)** : le format d'erreur standard renvoyé par Spring
  (`application/problem+json`) ; comment le typer côté TS ; s'appuyer sur `status` (contrat stable)
  plutôt que parser le texte de `detail` (message d'affichage, susceptible de changer).
- **Modélisation des états** : ici **quatre** cas — chargement / trouvé / introuvable (404) / panne
  technique. Une **union discriminée**
  (`{ kind: 'loading' } | { kind: 'found'; patient: Patient } | { kind: 'notFound' } | { kind: 'error' }`)
  comme alternative plus sûre à trois booléens indépendants qui peuvent se contredire.
- **UX d'un 404 métier** : ce n'est pas un bug — pas de log d'erreur, pas de message anxiogène ;
  le réserver aux vraies pannes. Cohérence de ton avec le reste de l'app.
- **Navigation** : `routerLink` dans le template vs `Router.navigate([...])` programmatique,
  `relativeTo`, `replaceUrl` (ne pas empiler le 404 dans l'historique).
- **Rendu conditionnel** : `@switch` sur le `kind` de l'état, ou `@if` / `@else if` en cascade.
- **Angular Material** : `MatCardModule` pour présenter la fiche, `MatProgressSpinnerModule` (ou
  `MatProgressBarModule`) pour l'état de chargement, `MatButtonModule` + `routerLink` pour le retour
  à la liste. Modules importés dans le `imports:` du composant standalone.