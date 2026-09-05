# US3 — Le coordinateur planifie un RDV pour un patient

**Package** : `rdv/domain`, `rdv/application`, `rdv/infrastructure/primary`, `rdv/infrastructure/secondary`

**Champs de l'agrégat `RendezVous`**

| Champ | Type | Règle de validation |
|---|---|---|
| id | identifiant (UUID) | généré à la création |
| patientId | référence à un patient | non nul |
| dateHeure | LocalDateTime | dans le futur |
| statut | enum (`PLANIFIE`, `ANNULE`, `HONORE`) | `PLANIFIE` à la création |

**Point de conception à implémenter vous-même** : avant de créer le RDV, il faut vérifier que le
patient existe. Définissez un port dans `rdv/application` qui exprime ce besoin (ex. "est-ce que ce
patient existe ?"), puis implémentez-le dans `rdv/infrastructure/secondary` en appelant l'interface
de lecture exposée par `patient/application` (pas le domain Patient). Si le patient n'existe pas :
levez une exception métier, mappez-la en 400 côté controller.

**Endpoint**

`POST /rdv` — rôle requis : `coordinateur`
- Corps : `patientId`, `dateHeure`
- 201 avec le RDV créé si le patient existe et la date est valide
- 400 si patient inconnu ou date passée

`GET /rdv?patientId=...` — rôle requis : `soignant` ou `coordinateur`
- 200, liste des RDV du patient

**Frontend** (`frontend/src/app/rdv`)
- Formulaire de planification (sélection d'un patient existant + date/heure) en composant Angular,
  réservé coordinateur.

**Concepts et notions concernées**
- DDD / hexagonal : port secondary défini par le consommateur (Dependency Inversion Principle), agrégat référençant un autre agrégat par ID (jamais par objet), exception métier mappée en HTTP.
- Architecture modulaire : communication inter-module via `application` uniquement (jamais via domain/infrastructure d'un autre module).
- Angular : formulaires réactifs (`ReactiveFormsModule`, `FormGroup`, `FormControl`, `Validators`), binding d'une liste (select) alimentée par un service.
- Java : `LocalDateTime`, validation d'une date future.
