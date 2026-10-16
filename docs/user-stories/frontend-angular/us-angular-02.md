# US Angular 2 — Le soignant consulte la liste des patients à l'écran

Développe la section « Frontend » d'US2 côté Angular. Dépend d'US Angular 1 (proxy) et d'US2 backend
(`GET /patients`).

**API consommée**

`GET /api/patients` → `200` avec un tableau JSON :

| Champ | Type JSON | Remarque |
|---|---|---|
| id | string | UUID sérialisé (`@JsonValue` côté backend) |
| nom | string | |
| dateNaissance | string | date ISO `AAAA-MM-JJ` — **pas** un objet `Date` |
| typePompe | string | `"PATCH"` ou `"TUBULAIRE"` |

**Frontend** (`frontend-angular/src/app/patients/`)

- Un **modèle TypeScript** décrivant un patient tel que sérialisé par l'API (interface ou type).
- Un **service** `@Injectable({ providedIn: 'root' })` exposant une méthode qui retourne un
  `Observable<Patient[]>` via `HttpClient.get<Patient[]>('/api/patients')`.
- Un **composant standalone** « liste » : déclenche l'appel, expose le flux au template, rend un
  tableau (Angular Material) avec nom / date de naissance / type de pompe.
- Template en **nouvelle syntaxe de contrôle de flux** : `@for` (avec `track`) pour les lignes,
  `@if` / `@else` pour les trois états (chargement, erreur, succès) et `@empty` pour la liste vide.
- La route `/patients` est déclarée dans US Angular 5.

**Critères d'acceptation**

- À l'ouverture de `/patients`, la table affiche les patients renvoyés par l'API.
- Liste vide → message explicite (« aucun patient »), pas une table réduite à son entête.
- Erreur réseau ou 5xx → message d'erreur lisible, jamais un écran blanc ni une exception non
  gérée en console.
- Pas de souscription laissée ouverte (soit `async` pipe, soit désabonnement explicite).
- Aucune URL absolue : l'appel passe par `/api/...`.

**Concepts et notions concernées**

- **Composant standalone** : `@Component` avec `imports:` au niveau du composant (plus de
  `NgModule`), `selector`, `template` / `templateUrl`, `styleUrl`. Cycle de vie : `constructor`
  (injection, pas d'effet de bord) vs `ngOnInit` (là où se déclenche un appel HTTP), `ngOnDestroy`
  (nettoyage).
- **Injection de dépendances Angular** : fonction `inject()` vs paramètres de constructeur ;
  `providedIn: 'root'` = singleton applicatif *tree-shakable* ; notion d'arbre d'injecteurs
  (racine → route → composant).
- **`HttpClient`** : `get<T>()` et son générique — le `<T>` est une **assertion de confiance**, il
  ne valide **rien** à l'exécution ; la requête est *lazy* (cold observable) : rien ne part tant
  que personne ne souscrit ; sérialisation/désérialisation JSON automatique.
- **RxJS `Observable`** : cold vs hot, `subscribe(next, error, complete)`, fuite mémoire d'une
  souscription non fermée, opérateurs `map` / `catchError`, `pipe()`. Comparaison avec `Promise` :
  un flux (0..n valeurs) vs une valeur unique, *lazy* et **annulable** vs *eager*.
- **`async` pipe** : souscrit et se désabonne automatiquement avec le composant, marque la vue à
  rafraîchir ; piège classique : plusieurs `async` sur le même Observable = plusieurs souscriptions
  = plusieurs requêtes → pattern `@if (patients$ | async; as patients)` pour n'en garder qu'une.
- **Nouvelle syntaxe de flux** (`@for`, `@if`, `@switch`) : intégrée au **compilateur** (rien à
  importer, contrairement à `NgIf` / `NgFor`) ; `@for` **impose** `track` (clé d'identité pour un
  diff DOM efficace, typiquement `track patient.id`) ; blocs `@empty`, `@else`, `@else if`.
  Différences avec `*ngFor` / `*ngIf` (micro-syntaxe `*`, `trackBy` optionnel et souvent oublié).
- **Modèle de données côté front** : `interface` vs `type` vs `class` (une interface disparaît à la
  compilation, une classe non) ; **union de littéraux de chaînes** pour refléter un enum backend
  (`type TypePompe = 'PATCH' | 'TUBULAIRE'`) ; le fait qu'une « date » reçue de `HttpClient` reste
  une **chaîne** — la convertir en `Date` est un choix explicite.
- **Angular Material** : importer les modules dont le composant a besoin directement dans son
  `imports:` (`MatTableModule`, ou `MatListModule` / `MatCardModule` pour une version plus simple) —
  pas de `NgModule` global ; `mat-table` avec `matColumnDef` / `*matHeaderRowDef` / `*matRowDef` et
  une `DataSource` (ou un simple tableau), vs une `<table>` HTML stylée par les classes Material.
  Notions de theming (le thème Indigo/Pink préfabriqué fournit les tokens `--mat-sys-*`),
  typographie globale (`mat-typography` sur le `body`), icônes (`<mat-icon>` + police Material
  Icons chargée dans `index.html`).
- **Les trois états d'un appel asynchrone** : en cours / succès / erreur — pourquoi les rendre
  explicites dans l'UI plutôt que d'afficher « rien » ; `catchError` qui renvoie un flux de repli
  (`of([])` ou un objet d'erreur) au lieu de laisser l'erreur remonter.
- **Détection de changement** : le projet est en **zoneless** (`provideZonelessChangeDetection()`
  dans `app.config.ts`, plus de `zone.js`). Angular ne « devine » plus qu'un état a changé en
  interceptant tous les callbacks async ; il faut le lui **notifier** — un signal qui change, un
  `AsyncPipe` qui émet, un binding d'événement dans le template. D'où le fait que l'`async` pipe et
  les signals sont les bons outils ici, et l'intérêt de `ChangeDetectionStrategy.OnPush`.