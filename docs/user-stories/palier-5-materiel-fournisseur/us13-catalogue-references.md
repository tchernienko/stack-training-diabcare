# US13 — Le catalogue encadre les références de matériel assignables (renforcement)

**US de renforcement** (pas prioritaire, pas bloquante pour US9/US10) : introduit un **troisième**
cas du pattern "port défini par le consommateur" déjà pratiqué en US3 (`rdv` → `patient`) et US9
(`materiel` → `patient` + `fournisseur`) — cette fois un agrégat qui référence un **autre agrégat
du même domaine fonctionnel** (matériel/fournisseur), pas seulement une vérification d'existence
cross-module basique.

**Contexte**

Dans US9, `AssignationMateriel.reference` est une chaîne libre, non vérifiée. Cette US introduit
une donnée de référence (*reference/master data*) : un catalogue des modèles d'équipement que
chaque fournisseur peut fournir, qui existe indépendamment de toute assignation à un patient —
l'équivalent d'une fiche produit, pas d'une commande.

**Package** : `catalogue/domain`, `catalogue/application`, `catalogue/infrastructure/primary`,
`catalogue/infrastructure/secondary` — nouveau module, dédié (ni dans `fournisseur`, ni dans
`materiel`).

**Champs de l'agrégat `ReferenceCatalogue`**

| Champ | Type | Règle de validation |
|---|---|---|
| id | identifiant (UUID) | généré à la création |
| type | enum (`POMPE`, `CAPTEUR_GLYCEMIE`, `CATHETER`) | non nul |
| modele | String | non vide |
| fournisseurId | référence à un fournisseur | non nul, fournisseur doit exister |

**Point de conception à implémenter vous-même** : même logique de port que US3/US9 —
`catalogue/application` définit ce dont il a besoin pour vérifier l'existence du fournisseur,
implémenté dans `catalogue/infrastructure/secondary` en s'appuyant sur l'interface de lecture
exposée par `fournisseur/application`.

**Endpoints**

`POST /catalogue` — rôle requis : `coordinateur`
- Corps : `type`, `modele`, `fournisseurId`
- 201 avec la référence créée si le fournisseur existe et les champs sont valides
- 400 si fournisseur inconnu, modèle vide ou type absent

`GET /catalogue` — rôle requis : `soignant` ou `coordinateur`
- 200, liste des références. Filtrage par `type` et/ou `fournisseurId` en query params optionnel —
  à l'image du filtre introduit en US Angular 6, à votre discrétion.

**Impact sur US9 (`AssignationMateriel`)**

Le champ `reference` (`String` libre) devient `referenceCatalogueId`, vérifié à l'assignation : la
référence doit exister dans le catalogue **et** appartenir au `fournisseurId` fourni — une
vérification à deux critères croisés, nouvelle par rapport aux vérifications d'existence simples
déjà pratiquées. Le champ `type` de l'assignation peut alors être déduit de celui de la référence
catalogue plutôt que redondant — à trancher en implémentant.

**Critères d'acceptation**

- Une référence catalogue valide se crée et apparaît dans `GET /catalogue`.
- Une assignation (US9) qui pointe vers une référence catalogue inexistante échoue en 400.
- Une assignation qui pointe vers une référence catalogue existante mais appartenant à un **autre**
  fournisseur que celui fourni échoue également en 400 (cohérence croisée référence/fournisseur).
- Deux fournisseurs différents peuvent proposer deux références distinctes du même `type`.

**Concepts et notions concernées**

- DDD / hexagonal : troisième occurrence du pattern "port défini par le consommateur" (US3, US9),
  cette fois un module dédié (`catalogue`) consommé par un autre module métier (`materiel`), avec
  une vérification cross-champs (référence ET fournisseur cohérents) plutôt qu'une simple existence.
- Données de référence (*reference/master data*) vs donnée transactionnelle : différence entre un
  catalogue (stable, peu de volume, existe sans contexte patient) et une assignation (un fait
  métier daté, propre à un patient).
- Modélisation : un agrégat qui référence un autre agrégat **par ID** à l'intérieur du même domaine
  fonctionnel (matériel/fournisseur), distinct des références cross-module déjà pratiquées
  (RDV → Patient, Materiel → Patient + Fournisseur).
- Persistence : contrainte d'unicité éventuelle (une même paire modèle/fournisseur ne devrait
  probablement pas exister deux fois) — à juger utile ou non.