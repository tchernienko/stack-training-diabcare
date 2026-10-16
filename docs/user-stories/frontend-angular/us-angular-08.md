# US Angular 8 — Les écrans exploitent vraiment Angular Material (renforcement, non prioritaire)

**Priorité basse** — à traiter après avoir consolidé signals/`effect()`/Router (US Angular 6, 7, et
le reste du chemin principal 2→3→4→5). Notée maintenant pour ne pas perdre le contexte, pas pour
être attaquée tout de suite.

**Contexte**

Le socle Angular Material est posé (`@angular/material` + `@angular/cdk`, thème Indigo/Pink
préfabriqué câblé dans `angular.json`, police Roboto + Material Icons) mais très peu exploité dans
le code actuel : la liste patients est un `<ul>`/`<li>` maison (`patients-list.component.html`), la
barre de navigation (`site-header`) est deux `<a>` bruts sans `MatToolbarModule`, et
`routerLinkActive="..."` n'a aucune classe CSS associée (donc aucun état actif visible).

**Frontend** (`frontend-angular/src/app/`)

- Remplacer le `<ul>` de `patients-list` par un vrai `mat-table` (`MatTableModule`,
  `matColumnDef` / `*matHeaderRowDef` / `*matRowDef`) plutôt qu'un tableau HTML stylé à la main.
- `site-header` : `MatToolbarModule` pour la barre supérieure, `MatButtonModule` / `MatIconModule`
  pour les liens de navigation, avec un vrai style pour l'état actif (`routerLinkActive="..."` avec
  une classe qui fait quelque chose).
- Personnalisation du thème : un générateur de thème Material a été repéré (non encore exploré) —
  à creuser pour remplacer le Indigo/Pink préfabriqué par des couleurs propres au projet, en gardant
  la mécanique de tokens `--mat-sys-*` déjà en place plutôt qu'en la contournant.

**Critères d'acceptation**

- La liste patients se rend comme un `mat-table` fonctionnellement identique à l'actuelle (mêmes
  colonnes, même contenu), pas une refonte de la donnée affichée.
- La barre de navigation utilise les composants Material et indique visuellement l'écran actif.
- Le thème reste cohérent (mêmes tokens de couleur/typo utilisés partout), qu'il s'agisse du
  Indigo/Pink d'origine ou d'un thème généré personnalisé.

**Concepts et notions concernées**

- **`mat-table`** : `dataSource`, définition de colonnes déclarative (`matColumnDef`), lignes
  d'en-tête/de contenu (`matHeaderRowDef` / `matRowDef`) — différence avec une `<table>` juste
  stylée.
- **`MatToolbarModule` / navigation Material** : composition avec `routerLink` /
  `routerLinkActive` déjà pratiqués (US Angular 5), cette fois avec un vrai rendu Material et un
  état actif stylé.
- **Theming Material (tokens `--mat-sys-*`)** : comment un thème généré (palette personnalisée)
  s'intègre au mécanisme déjà en place plutôt que de le dupliquer ; ce que le thème préfabriqué
  fournit déjà vs ce qu'un thème custom changerait concrètement.