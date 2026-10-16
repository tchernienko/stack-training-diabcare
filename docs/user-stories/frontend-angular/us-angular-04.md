# US Angular 4 — Le coordinateur saisit un nouveau patient

Développe la partie « formulaire de création » de la section Frontend d'US2. Dépend d'US Angular 2
(modèle + service) et d'US Angular 5 (route `/patients/new`). Le *gating* par rôle (bouton réservé
au coordinateur) est reporté au palier 3 (sécurité Keycloak) — ici le formulaire est accessible.

**API consommée**

- `POST /api/patients`, corps `{ nom, dateNaissance, typePompe }` → `201` avec le patient créé
  (`id` inclus).
- `400` avec `ProblemDetail` portant une propriété `field` (`"nom"`, `"dateNaissance"` ou
  `"typePompe"`) quand une règle métier du domaine échoue.
- `400` avec `title` « Requête illisible/invalide » (sans `field`) si `typePompe` est hors enum ou
  si le JSON est mal formé.

**Frontend** (`frontend-angular/src/app/patients/`)

- Un **composant standalone** « formulaire de création ».
- **Choix d'approche à trancher et justifier** : *Reactive Forms* (recommandé — validation
  composable, typage, testabilité) vs *Template-driven*.
- Champs :
  - `nom` — texte, requis ;
  - `dateNaissance` — date, requise, **dans le passé strict** (miroir de la règle backend) ;
  - `typePompe` — `select` parmi `PATCH` / `TUBULAIRE`, requis.
- Validation **client** (retour immédiat) **et** affichage des erreurs `400` renvoyées par le
  backend, replacées sur le bon champ via `field`.
- Sur `201` : redirection vers la fiche du patient créé (`/patients/:id`) ou la liste, avec un
  retour visuel de succès.
- Bouton « Créer » désactivé pendant l'envoi (anti double-POST).

**Critères d'acceptation**

- Formulaire invalide → soumission bloquée + message par champ concerné.
- Soumission valide → `201`, redirection, le patient apparaît ensuite dans la liste.
- Réponse `400` `field: "nom"` (ex. règle contournée côté client) → l'erreur s'affiche **sur le
  champ `nom`**.
- Double-clic sur « Créer » → **un seul** `POST` parti.
- Date du jour ou future saisie → refus côté client, cohérent avec le refus backend.

**Concepts et notions concernées**

- **Reactive Forms** : `FormGroup` / `FormControl` / `FormBuilder` ; `Validators` intégrés
  (`required`, `minLength`…) et **validateur custom** (`ValidatorFn` : une fonction
  `(control) => ValidationErrors | null` pour « date dans le passé ») ; état d'un contrôle —
  `pristine` / `dirty`, `touched` / `untouched`, `valid` / `invalid` / `pending` ; `valueChanges`
  et `statusChanges` (des `Observable`).
- **Typed Forms** : `FormGroup` typé, `NonNullableFormBuilder`, ce que ça change au type de
  `.value` (plus de `null` implicite partout).
- **Template-driven forms** : `ngModel`, `FormsModule`, `NgForm` ; pourquoi c'est plus limité dès
  qu'il faut de la validation composite ou du typage — et les cas simples où ça suffit.
- **Liaison de données** : `[prop]` (entrant, one-way), `(event)` (sortant), `[(ngModel)]` — le
  « banana in a box » n'est que du sucre pour `[x]="v"` + `(xChange)="v = $event"` ; one-way vs
  two-way et pourquoi Angular a resserré autour du one-way.
- **Validation à deux niveaux** : client = confort et réactivité, **serveur = autorité**. Ne jamais
  faire confiance au client ; mapper `ProblemDetail.field` d'une réponse `400` sur le bon
  `FormControl` via `setErrors({ serveur: message })`, et nettoyer cette erreur au prochain
  `valueChanges`.
- **`HttpClient.post`** : corps sérialisé en JSON automatiquement, en-têtes par défaut, `Observable`
  de la réponse typée, exploitation du `201` (récupérer l'`id` généré dans le corps de réponse).
- **Anti double-soumission** : un drapeau `submitting`, `finalize` (RxJS) pour le remettre à `false`
  quoi qu'il arrive (succès **ou** erreur), `disabled` du bouton lié à `form.invalid || submitting`.
- **Angular Material — champs de formulaire** : `MatFormFieldModule` + `MatInputModule` +
  `MatSelectModule` + `MatDatepickerModule` (avec un adaptateur de date : `provideNativeDateAdapter`
  ou Luxon), importés dans le `imports:` du composant standalone. `<mat-form-field>` enveloppe un
  `matInput` / `mat-select` ; `<mat-error>` s'affiche tout seul quand le `FormControl` est invalide
  et *touched* (l'accessibilité — `aria-describedby`, association label/erreur — est gérée par le
  composant, à connaître mais pas à recâbler à la main) ; `appearance`, `hintLabel`,
  `<mat-datepicker>` et le format de sortie (un `Date`, pas la chaîne d'un `<input type="date">` —
  d'où une conversion à prévoir avant le POST). `MatButtonModule` pour le bouton de soumission,
  état `disabled` lié au formulaire.
- **Navigation après action** : `Router.navigate(['/patients', id])`, ou un service de
  notification (« toast ») pour le feedback de succès si on reste sur place.
- **Rendu des erreurs** : `@if (control.touched && control.hasError('required'))`, factoriser un
  petit composant / une fonction d'aide plutôt que répéter la condition par champ.
- **Enum backend ↔ `mat-select`** : garder **une** source de vérité — l'union
  `'PATCH' | 'TUBULAIRE'` — et en dériver les `<mat-option>` (ou un tableau typé `readonly`), pas de
  liste dupliquée à la main qui divergera.
- **Conversion de valeur** : le backend attend `dateNaissance` en chaîne ISO `AAAA-MM-JJ` ;
  `<mat-datepicker>` travaille avec un objet `Date` (ou un `DateTime` Luxon selon l'adaptateur) →
  formater avant le `POST`. Comprendre où placer cette conversion (dans le composant ? un mapper
  dédié ?) et pourquoi ne pas laisser fuiter le type de la lib de date jusqu'à l'appel HTTP.