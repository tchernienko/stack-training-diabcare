# US Angular 7 — Le titre de l'onglet reflète le patient consulté (renforcement)

**US de renforcement**, concept jamais pratiqué : `effect()`, à distinguer de `computed()` (déjà vu
sur US Angular 3 et 6). Un `computed()` produit une **valeur** lue ailleurs ; un `effect()` produit
un **effet de bord** — ici, modifier le titre de l'onglet du navigateur, qui n'est pas une valeur
qu'un template affiche.

**Frontend** (`frontend-angular/src/app/patient-details/`)

- Un `effect()` (posé dans un contexte d'injection — constructeur, ou champ initialisé à la
  déclaration) qui lit le signal `patient` et met à jour le titre de l'onglet en conséquence.
- Utiliser le service `Title` d'Angular (`@angular/platform-browser`, `titleService.setTitle(...)`)
  plutôt que manipuler `document.title` directement — service déjà fourni par le framework pour
  cet usage précis.
- Titre par défaut (nom de l'appli, ex. « Diabcare ») tant que le patient n'est pas encore chargé ou
  reste introuvable ; titre du type « Diabcare — <nom du patient> » une fois chargé.

**Critères d'acceptation**

- À l'arrivée sur `/patients/:id`, l'onglet affiche d'abord le titre par défaut, puis se met à jour
  dès que la réponse arrive — sans intervention manuelle dans le template.
- Naviguer d'un patient à un autre **sans repasser par la liste** (changer juste l'`id` dans l'URL,
  ou suivre un lien direct) met à jour le titre correctement pour le nouveau patient. C'est le point
  qui casse le plus souvent en pratique : si le composant est réutilisé par le Router entre deux
  navigations sur la même route (`/patients/:id` → `/patients/:id2`), un code qui ne réagit qu'au
  premier chargement ne se redéclenchera pas — l'occasion de vérifier si le signal `patientId`
  réagit vraiment au changement de paramètre dans ce cas, ou si la RouteReuseStrategy par défaut
  d'Angular joue un rôle ici.
- Quitter l'écran détail restaure le titre par défaut (ou celui de l'écran suivant, selon comment
  chaque écran gère son propre titre).

**Concepts et notions concernées**

- **`effect()`** : se ré-exécute automatiquement quand un des signaux qu'il lit change ; doit être
  créé dans un **contexte d'injection** (sinon erreur au runtime) ; fonction de nettoyage optionnelle
  (`onCleanup`) pour annuler un effet précédent avant le suivant — à voir si pertinent ici.
- **`effect()` vs `computed()`** : la différence n'est pas de syntaxe mais d'intention — jamais
  utiliser `effect()` pour calculer une valeur consommée ailleurs (`computed` est fait pour ça et
  évite les effets de bord en cascade difficiles à tracer).
- **Service `Title` d'Angular** : pourquoi passer par l'API du framework plutôt que par le DOM
  directement (cohérence avec du rendu serveur éventuel, testabilité — un service est mockable,
  `document` beaucoup moins proprement).
- **Réutilisation de composant par le Router** : par défaut, Angular ne détruit/recrée **pas**
  forcément un composant quand seul un paramètre de route change (contrairement à un changement de
  route complètement différente) — implication directe sur quels signaux se mettent à jour tout
  seuls et lesquels non.