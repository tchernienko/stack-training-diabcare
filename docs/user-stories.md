# User stories — Diabcare

Convention pour chaque US : package cible (domain/application/infrastructure primary|secondary),
champs à modéliser, endpoints REST, règles métier, critères d'acceptation.
Le "comment" (nom exact des classes) reste à vous — seul le "quoi" est spécifié.

Rappels d'architecture actés dans la conception (à respecter, pas à redécouvrir) :
- Monolithe modulaire, un seul module Maven, frontières par **package** (`domain` / `application` /
  `infrastructure/primary` / `infrastructure/secondary`), vérifiées par ArchUnit (US12).
- Un module ne dépend **jamais** du `domain` ni de l'`infrastructure` d'un autre module — uniquement
  de son `application` (interfaces exposées). Exemple : RDV consulte Patient via une interface de
  lecture exposée par `patient/application`, jamais via `Patient` (domain) ou `PatientJpaEntity`.
- Un port `secondary` propre à chaque module qui en a besoin (ex. RDV définit lui-même ce qu'il
  attend de Patient — pas Patient qui impose son API).
- Les IDs référencés par plusieurs modules (ex. l'ID d'un patient utilisé par RDV et Matériel) sont
  candidats à vivre dans un `shared/` — à vous de le constater en implémentant RDV, pas de le
  préconfigurer.
- Rôles Keycloak (realm `diabcare`, déjà créés dans `infra/keycloak/realm-export.json`) : `patient`,
  `soignant`, `coordinateur`, `admin`. `admin` reste un rôle **technique**, jamais utilisé pour les
  droits métier ci-dessous.

---

## Palier 1 — Premier agrégat + API (Patient)

### US1 — Le coordinateur crée un patient

**Package** : `patient/domain`, `patient/application`, `patient/infrastructure/primary`, `patient/infrastructure/secondary`

**Champs de l'agrégat `Patient`**

| Champ | Type | Règle de validation |
|---|---|---|
| id | identifiant (UUID) | généré à la création |
| nom | String | non vide |
| dateNaissance | LocalDate | dans le passé |
| typePompe | enum (`PATCH`, `TUBULAIRE`) | non nul |

La validation vit dans le constructeur/factory de l'agrégat — pas dans le DTO, pas dans le
controller. Un `Patient` invalide ne doit pas pouvoir exister en mémoire.

**Endpoint**

`POST /patients` — rôle requis : `coordinateur`
- Corps : `nom`, `dateNaissance`, `typePompe`
- 201 avec le patient créé (id, nom, dateNaissance, typePompe) si valide
- 400 si validation échoue (nom vide, date future, type de pompe absent)

**Critères d'acceptation**
- Un `soignant` qui appelle cet endpoint reçoit 403 (cf. US6).
- Le patient est persisté en base MySQL (table à votre convention, ex. `patient`).

### US2 — Le soignant (et le coordinateur) consulte la liste des patients

**Endpoint**

`GET /patients` — rôle requis : `soignant` ou `coordinateur`
- 200, liste des patients (id, nom, dateNaissance, typePompe)

`GET /patients/{id}` — même rôle
- 200 si trouvé, 404 sinon

**Frontend** (`frontend/src/app/patients`)
- Page listant les patients (appel `GET /patients`), tableau simple avec nom / date de naissance / type de pompe.
- Formulaire de création (US1) réservé aux coordinateurs — cachez ou désactivez le bouton si le rôle courant ne l'a pas (l'autorisation réelle reste côté backend, l'UI n'est qu'un confort).

---

## Palier 2 — RDV

### US3 — Le coordinateur planifie un RDV pour un patient

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
- Formulaire de planification (sélection d'un patient existant + date/heure), réservé coordinateur.

---

## Palier 3 — Sécurité Keycloak (mono-realm, 4 rôles)

### US4 — Connexion OIDC côté frontend

**Package** : `frontend/src/lib` (intégration Keycloak), `backend/bootstrap/security`

**Frontend**
- Intégrez `keycloak-js` (déjà en dépendance dans `package.json`) : initialisation avec les variables
  d'environnement déjà câblées dans `docker-compose.yml` (`NEXT_PUBLIC_KEYCLOAK_URL`,
  `NEXT_PUBLIC_KEYCLOAK_REALM`, `NEXT_PUBLIC_KEYCLOAK_CLIENT_ID`).
- Flow standard (Authorization Code + PKCE, déjà activé côté client `diabcare-frontend` dans le
  realm) : redirection vers Keycloak, retour avec un token, stockage en mémoire (pas de
  localStorage — évitez d'exposer le JWT au XSS).
- Chaque appel vers le backend porte le JWT en `Authorization: Bearer <token>`.

**Backend**
- Configurez Spring Security comme *resource server* (`spring-boot-starter-oauth2-resource-server`,
  déjà en dépendance) : stateless, `anyRequest().authenticated()` sauf `/actuator/health`.
- Keycloak place les rôles dans le claim `realm_access.roles`, format non reconnu nativement par
  Spring Security : écrivez un `Converter<Jwt, Collection<GrantedAuthority>>` qui lit ce claim et
  produit des autorités `ROLE_XXX`, pour que `@PreAuthorize("hasRole('COORDINATEUR')")` fonctionne
  sur vos controllers (US1/US2/US3).

**Critère d'acceptation** : un appel `GET /patients` sans token renvoie 401 ; avec un token valide
mais sans rôle `soignant`/`coordinateur`, 403 ; avec le bon rôle, 200.

### US5 — Le patient ne voit que ses propres RDV

**Prérequis** : ajoutez au client `diabcare-frontend` (dans `infra/keycloak/realm-export.json` ou
directement dans l'admin console Keycloak) un *protocol mapper* de type "User Attribute" qui
expose l'attribut utilisateur `patientId` sous le claim `patient_id` dans le token. Créez ensuite
un utilisateur Keycloak de test avec le rôle `patient` et cet attribut renseigné avec l'id d'un
patient existant en base.

**Endpoint**

`GET /rdv/me` — rôle requis : `patient`
- Lit le claim `patient_id` du token (`@AuthenticationPrincipal Jwt jwt`)
- 200, liste des RDV de ce patient uniquement

**Critère d'acceptation** : deux utilisateurs `patient` différents, avec des `patient_id` distincts,
ne voient jamais les RDV l'un de l'autre.

### US6 — Un soignant ne peut pas créer de patient

**Test à écrire** (backend, `@WebMvcTest` ou test d'intégration avec un JWT mocké) : un appel
`POST /patients` avec un token portant uniquement le rôle `soignant` renvoie 403.

---

## Palier 4 — Kafka

### US7 — Un événement `RdvPlanifie` est publié quand un RDV est créé

**Package** : `rdv/domain` (l'événement lui-même), `rdv/infrastructure/secondary` (le producteur)

**Payload de l'événement** (à adapter, mais a minima) :

| Champ | Type |
|---|---|
| eventId | UUID |
| occurredAt | Instant |
| rdvId | String |
| patientId | String |
| dateHeure | LocalDateTime |

- Topic suggéré : `diabcare.rdv.v1` (versionnez le nom de topic dès le départ — c'est une habitude à
  prendre, pas un besoin réel à cette échelle).
- Publication déclenchée juste après la sauvegarde réussie du RDV, dans le use case applicatif
  (`rdv/application`), via un port que vous définissez et implémentez avec `KafkaTemplate` côté
  `infrastructure/secondary`.

**Critère d'acceptation** : après un `POST /rdv` réussi, un message apparaît sur le topic
(vérifiable avec `kafka-console-consumer` dans le conteneur, ou un test avec `spring-kafka-test`
déjà en dépendance de test).

### US8 — Le module Notification consomme l'événement et logue une notification

**Package** : `notification/application`, `notification/infrastructure/primary` (le `@KafkaListener`
est un adaptateur **primary** — c'est l'extérieur, Kafka, qui pilote l'appli ici, contrairement au
producteur RDV qui est **secondary**)

- Consommateur du topic `diabcare.rdv.v1`, groupe `diabcare-backend` (déjà configuré dans
  `application.yml`).
- À réception, loguez (niveau INFO) un message simulant l'envoi d'une notification : par exemple
  "Notification envoyée au patient {patientId} pour le RDV du {dateHeure}". Pas besoin de vraie
  intégration email/SMS pour ce projet.

**Critère d'acceptation** : créer un RDV via l'API déclenche, dans les logs du backend, la ligne de
notification correspondante, de façon asynchrone (le `POST /rdv` répond avant que la notification
ne soit forcément déjà loguée).

---

## Palier 5 — Matériel + Fournisseur

### US9 — Le coordinateur assigne du matériel à un patient

**Package** : `fournisseur/*` (module simple, CRUD minimal) puis `materiel/*`

**`Fournisseur`** — champs : `id`, `nom` (non vide). Endpoint `POST /fournisseurs` (rôle
`coordinateur`), `GET /fournisseurs` (rôle `soignant` ou `coordinateur`).

**`Materiel`** — pas de gestion de stock : un `Materiel` représente une **assignation**, pas une
entrée de stock.

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

### US10 — Un événement `MaterielAssigne` déclenche une notification différente

Même patron que US7/US8 : événement publié par `materiel/infrastructure/secondary` sur un topic
dédié (ex. `diabcare.materiel.v1`), consommé par `notification/infrastructure/primary` avec un
`@KafkaListener` distinct de celui de US8, loguant un message différent (ex. "Matériel {type}
assigné au patient {patientId}, fourni par {fournisseurId}").

**Critère d'acceptation** : les deux flux (RDV → notification, Matériel → notification) coexistent
sans interférence, chacun sur son topic, chacun avec son propre listener.

---

## Palier 6 — CI/CD

### US11 — Le pipeline build et teste à chaque push

`.gitlab-ci.yml` est déjà fourni avec les stages `build`/`test` pour le backend et le build du
frontend. Rien à faire ici sauf constater que ça tourne — sauf si vous voulez enrichir (cache Maven,
build de l'image Docker, etc., en option).

### US12 — Un test ArchUnit vérifie les frontières hexagonales

**Package** : `backend/src/test/java/.../architecture`

Règles minimales à écrire (une classe de test avec la bibliothèque ArchUnit, déjà en dépendance) :

1. Les classes de `*.domain` ne dépendent d'aucune classe `org.springframework.*` ni
   `jakarta.persistence.*`.
2. Aucune classe de `*.infrastructure.primary` ne dépend d'une classe de `*.infrastructure.secondary`
   du même module (elles communiquent via `application`, jamais directement).
3. Un module ne dépend jamais du `domain` ni de l'`infrastructure` d'un autre module — seulement de
   son `application`. (Exemple concret à faire échouer volontairement une fois pour vérifier que la
   règle fonctionne : faites temporairement dépendre `rdv` de `PatientJpaEntity`, le test doit rouge.)

**Critère d'acceptation** : le test échoue si vous introduisez sciemment une violation, passe sinon.
C'est ce test qui remplace la discipline "à l'œil" par une garantie automatisée.

---

## Ordre suggéré

US1 → US2 → US3 → US4 → US5 → US6 → US7 → US8 → US9 → US10 → US11 → US12.

Rien n'empêche de faire US4 (sécurité) plus tôt si vous préférez sécuriser au fur et à mesure plutôt
qu'à la fin du palier 1 — dans ce cas, adaptez simplement l'ordre, la structure ci-dessus reste
valable indépendamment de la séquence choisie.
