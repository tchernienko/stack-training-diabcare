# US9 — Le coordinateur assigne du matériel à un patient

**Package** : `fournisseur/*` (module simple, CRUD minimal) puis `materiel/*`

**`Fournisseur`** — champs : `id`, `nom` (non vide). Endpoint `POST /fournisseurs` (rôle
`coordinateur`), `GET /fournisseurs` (rôle `soignant` ou `coordinateur`).

**`AssignationMateriel`** — pas de gestion de stock : cet agrégat représente une **assignation**,
pas une entrée de stock. Le nom évite le terme nu `Materiel`, réservé à la notion de
catalogue/référence introduite en [US13](us13-catalogue-references.md) (renforcement, non
bloquante pour cette US — le champ `reference` ci-dessous reste un `String` libre tant qu'US13
n'est pas traitée).

| Champ | Type | Règle |
|---|---|---|
| id | identifiant | généré |
| patientId | référence patient | non nul, patient doit exister |
| fournisseurId | référence fournisseur | non nul, fournisseur doit exister |
| type | enum (`POMPE`, `CAPTEUR_GLYCEMIE`, `CATHETER`) | non nul |
| reference | String | non vide |

Même logique de port que US3 : `materiel/application` définit ce dont il a besoin pour vérifier
l'existence du patient et du fournisseur, implémenté dans `materiel/infrastructure/secondary` en
s'appuyant sur les interfaces `application` exposées par `patient` et `fournisseur`.

**Endpoint**

`POST /materiel` — rôle requis : `coordinateur`
- Corps : `patientId`, `fournisseurId`, `type`, `reference`
- 201 si patient et fournisseur existent, 400 sinon

**Concepts et notions concernées**
- Réutilisation des concepts DDD/hexagonaux de US3 (port secondary, vérification d'existence cross-module), appliqués à deux dépendances externes simultanées (Patient + Fournisseur).
- CRUD minimal : module Fournisseur comme cas simple, à contraster avec la complexité du reste.
