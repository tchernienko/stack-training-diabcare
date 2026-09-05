# US5 — Le patient ne voit que ses propres RDV

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

**Concepts et notions concernées**
- Keycloak : protocol mapper (User Attribute → claim custom), gestion des attributs utilisateur.
- Spring Security : lecture d'un claim custom via `@AuthenticationPrincipal Jwt`.
- Sécurité applicative : filtrage des données par utilisateur courant (data scoping), principe du moindre privilège.
