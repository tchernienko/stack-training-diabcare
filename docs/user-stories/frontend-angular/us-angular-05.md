# US Angular 5 — L'utilisateur circule entre les écrans

Structure de routing de l'application. Sous-tend US Angular 2 (`/patients`), 3 (`/patients/:id`) et
4 (`/patients/new`).

**Frontend** (`frontend-angular/src/app/`)

- `app.routes.ts` — table de routes :
  - `''` → redirection vers `/patients` (`pathMatch: 'full'`) ;
  - `/patients` → écran liste ;
  - `/patients/new` → formulaire de création ;
  - `/patients/:id` → écran détail ;
  - `**` (wildcard, **en dernier**) → écran « page non trouvée » de l'application.
- `app.config.ts` — `provideRouter(routes)` (déjà en place) ; features à évaluer :
  `withComponentInputBinding()`, `withInMemoryScrolling()`.
- `app.component.html` — `<router-outlet />` + une barre de navigation minimale (`routerLink`,
  `routerLinkActive`).
- Un composant standalone « page non trouvée » pour la wildcard.
- **Ordre de déclaration** : `/patients/new` **avant** `/patients/:id`, sinon `new` est capté comme
  un `:id`.
- **Lazy loading** : `loadComponent` (chargement à la demande) vs `component` (import direct) — à
  arbitrer.

**Critères d'acceptation**

- `/` redirige vers `/patients`.
- La barre de navigation met en évidence l'écran courant (`routerLinkActive`).
- Une URL inconnue (`/nawak`) affiche l'écran 404 **de l'application** — distinct du « patient
  introuvable » d'US Angular 3.
- `/patients/new` ouvre le formulaire, pas le détail d'un patient dont l'`id` vaudrait `"new"`.
- Le bouton « précédent » du navigateur reste cohérent (pas de redirections qui piègent
  l'historique).

**Concepts et notions concernées**

- **Routeur Angular** : `Routes` = tableau de `Route` (`path`, `component` / `loadComponent`,
  `redirectTo`, `pathMatch`, `children`, `data`, `title`) ; le matching se fait **dans l'ordre de
  déclaration**, premier gagnant ; `pathMatch: 'full'` indispensable pour une redirection depuis
  `''` ; la wildcard `**` toujours en dernier.
- **`<router-outlet>`** : point d'insertion du composant de la route active ; outlets nommés
  (survol), routes enfants et outlets imbriqués pour des layouts.
- **`routerLink`** vs `href` : navigation SPA sans rechargement de page ; forme tableau
  `[routerLink]="['/patients', patient.id]"` vs chaîne ; `routerLinkActive="..."` +
  `ariaCurrentWhenActive="page"` pour l'état actif accessible.
- **Paramètres et données de route** : paramètre de chemin (`:id`), *query params* (`?tri=nom`),
  `data` statique, `title` de route ; **`withComponentInputBinding()`** qui projette params, query
  params et `data` directement sur des `@Input()` du composant — évite `ActivatedRoute` pour les
  cas simples (pertinent pour US Angular 3).
- **Lazy loading** : `loadComponent: () => import('./patients/patient-list').then(m => m.PatientList)` ;
  effet sur le découpage du bundle (`chunks`) ; `loadChildren` pour un sous-arbre de routes par
  *feature* ; quand ça vaut le coup (ici l'app est petite — comprendre le mécanisme sans
  forcément l'appliquer partout).
- **Guards et resolvers** (survol, détaillés au palier 3) : `CanActivateFn` (accès conditionnel),
  `CanDeactivateFn` (quitter un formulaire non enregistré → confirmation), `ResolveFn`
  (pré-charger la donnée avant d'afficher l'écran, au prix d'une transition plus lente) — ce sont
  désormais des **fonctions**, plus des classes.
- **Stratégie d'URL** : `PathLocationStrategy` (défaut, HTML5 *history API*, URLs propres — impose
  un *fallback* serveur qui renvoie `index.html` pour toute route) vs `HashLocationStrategy`
  (`/#/patients`, aucun réglage serveur). Lien avec le déploiement (nginx, `try_files`).
- **Organisation des routes** : tout à plat dans `app.routes.ts` vs un fichier de routes par
  *feature* (`patients.routes.ts`) importé dans la table racine — compromis de lisibilité quand
  l'application grossit.
- **404 applicatif vs 404 API** : deux notions distinctes — une URL front qui ne correspond à
  aucune route, vs une ressource backend absente (`GET /api/patients/{id}` → `404`). Elles méritent
  deux écrans et deux messages.
- **Navigation programmatique** : `Router.navigate` / `navigateByUrl`, `NavigationExtras`
  (`queryParams`, `replaceUrl`, `state`), écoute des `Router.events` (ex. remonter le scroll en
  haut à chaque navigation).
- **Angular Material — coquille de navigation** : `MatToolbarModule` pour la barre supérieure,
  `MatButtonModule` / `MatIconModule` avec `routerLink` + `routerLinkActive` pour les liens ; pour
  une nav latérale, `MatSidenavModule` + `MatListModule` (`mat-nav-list`, `<a mat-list-item>`).
  Ces modules s'importent dans le composant hôte (`AppComponent` standalone).