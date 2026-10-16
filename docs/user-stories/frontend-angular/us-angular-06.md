# US Angular 6 — Le soignant filtre la liste par nom (renforcement)

**US de renforcement** (pas un nouveau concept) : reprend le chaînage *signal source → dérivé* déjà
pratiqué sur US Angular 3 (`patientId` → `httpResource`), en deux étapes qui permettent de rejouer
ce pattern deux fois, avec deux natures de source différentes.

Volontairement **pas d'Angular Material** dans cette US (`<input>` HTML brut) — l'objectif est de
retravailler les signaux (et un peu de backend), pas d'ajouter une nouvelle bibliothèque en même
temps. Voir US Angular 8 pour la passe Material.

## Étape A — filtrage côté client (échauffement)

Filtre la liste déjà chargée par `PatientService.getPatients()`, sans toucher au réseau.

**Frontend** (`frontend-angular/src/app/patients-list/`)

- Un signal writable pour le texte tapé (mis à jour via `(input)`, pas de `[(ngModel)]` — concept
  forms pas encore vu, et de toute façon superflu ici, un binding one-way suffit).
- Un `computed()` qui dérive la liste filtrée à partir de la liste chargée **et** du signal de
  recherche — recalcul automatique à chaque frappe.
- Filtrage insensible à la casse, sur une sous-chaîne du `nom`.

## Étape B — bascule vers un filtrage côté serveur

Remplace le filtrage en mémoire par un vrai appel `GET /api/patients?nom=...`, en réutilisant
**exactement** le pattern signal → `httpResource` d'US Angular 3 (`patientId` →
`findPatientById`), cette fois avec le texte de recherche comme source.

**Backend** (`backend/src/main/java/com/example/diabcare/patient/`)

`GET /api/patients?nom=xxx` — même endpoint que l'existant, paramètre `nom` **optionnel**
(`@RequestParam(required = false) String nom`) :
- absent ou vide → comportement actuel inchangé (liste complète) ;
- présent → filtre côté base, correspondance **partielle et insensible à la casse** sur `nom`.

Traversée hexagonale à adapter à chaque couche :
- `PatientResource` : nouveau paramètre de requête sur `getPatients`.
- `PatientApplicationService.findAll(...)` : accepter le critère de filtre (`Optional<String>` ou
  surcharge), le transmettre au port.
- `PatientRepositoryPort` : étendre le contrat (nouvelle méthode ou paramètre sur `findAll`).
- `SpringJpaPatientRepository` : requête dérivée Spring Data (`findByNomContainingIgnoreCase`) ou
  `@Query` explicite.
- `DbPatientRepositoryAdapter` : implémente le port en déléguant à la bonne méthode du repository
  JPA selon que le filtre est présent ou non.

Complète au passage le `TODO` déjà présent sur cet endpoint (`PatientResource.java` —
« gérer la pagination ? le tri ? ») en y ajoutant la recherche à la liste des critères à traiter un
jour, plutôt que de laisser un `TODO` obsolète une fois la recherche faite.

**Frontend** (`frontend-angular/src/app/patients-list/` et `patient.service.ts`)

- Le signal de recherche de l'étape A devient la source lue **à l'intérieur** de la fonction passée
  à un nouvel `httpResource` (au lieu d'alimenter un `computed()` local) — même mécanique que
  `findPatientById(patientId: Signal<string>)`.
- Attention au **debounce** : contrairement au détail patient où le signal change rarement (une
  navigation), ici il change à chaque frappe. Sans tempo, chaque caractère tapé déclenche un appel
  réseau. `httpResource` ne le fait pas pour vous — à gérer explicitement (RxJS `debounceTime` sur
  un flux dérivé du signal via `toObservable`/`toSignal`, ou un `setTimeout` maison à défaut).

**Critères d'acceptation**

- Étape A : taper dans le champ met à jour la liste affichée sans rechargement ni appel réseau
  supplémentaire ; champ vide → liste complète ; aucune correspondance → message explicite
  (« aucun patient ne correspond »), pas une table vide sans explication.
- Étape B : la même UX est obtenue via un vrai appel réseau filtré côté serveur, avec un
  ralentissement (debounce) perceptible mais raisonnable entre la frappe et la requête — pas un
  appel par caractère tapé.
- Un test backend (au niveau qui vous semble pertinent — application service ou resource) couvre au
  minimum : filtre absent (comportement inchangé), filtre qui ne matche personne (liste vide, pas
  d'erreur), filtre insensible à la casse.

**Concepts et notions concernées**

- **Signal source (writable) vs signal dérivé** : que la dérivation soit un `computed()` pur (étape
  A) ou un `httpResource` asynchrone (étape B), c'est le **même** principe de source unique modifiée
  via `.set()`, jamais assignée directement en aval.
- **Pourquoi `computed()` suffit en étape A mais pas en étape B** : filtrer une liste déjà en
  mémoire est pur et synchrone ; filtrer côté serveur est asynchrone et a un coût réseau — bon
  contraste pour comprendre quand chaque outil s'applique.
- **Debounce sur un signal** : un signal qui change à haute fréquence (saisie clavier) a besoin d'un
  tempo avant de déclencher un effet coûteux — notion déjà croisée côté RxJS (`debounceTime`), à
  relier à l'écosystème signals (`toObservable`/`toSignal` comme pont entre les deux mondes).
- **Requête dérivée Spring Data** vs `@Query` : convention de nommage de méthode
  (`findByXxxContainingIgnoreCase`) qui génère la requête, limites de cette approche (composition de
  critères multiples) qui justifieraient de passer à `@Query` ou des Specifications plus tard.
- **Paramètre optionnel côté REST** : `@RequestParam(required = false)`, distinction absent vs vide,
  et pourquoi ne **jamais** faire confiance à un filtre client pour la sécurité/l'autorisation (pas
  un souci ici, mais le réflexe à ancrer).