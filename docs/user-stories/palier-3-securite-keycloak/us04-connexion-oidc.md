# US4 — Connexion OIDC côté frontend

**Package** : `frontend/src/app/core` ou équivalent (intégration Keycloak), `backend/bootstrap/security`

**Frontend**
- Intégrez Keycloak côté Angular — soit via `keycloak-js` directement (utile pour bien voir ce qui
  se passe sous le capot, cohérent avec l'objectif d'apprentissage), soit via `keycloak-angular` qui
  encapsule l'intégration avec les mécanismes Angular (guards de route, intercepteur HTTP). À vous
  de choisir selon si vous préférez comprendre le flow brut ou l'idiome Angular habituel.
- Configuration via `environment.ts` / `environment.prod.ts` (URL Keycloak, realm, client ID) plutôt
  que des variables d'environnement au build — cohérent avec les conventions Angular, déjà câblées
  dans `docker-compose.yml`.
- Flow standard (Authorization Code + PKCE, déjà activé côté client `diabcare-frontend` dans le
  realm) : redirection vers Keycloak, retour avec un token, stockage en mémoire (pas de
  localStorage — évitez d'exposer le JWT au XSS).
- Chaque appel vers le backend porte le JWT en `Authorization: Bearer <token>` — un `HttpInterceptor`
  Angular est le point naturel pour l'ajouter automatiquement à chaque requête sortante.

**Backend**
- Configurez Spring Security comme *resource server* (`spring-boot-starter-oauth2-resource-server`,
  déjà en dépendance) : stateless, `anyRequest().authenticated()` sauf `/actuator/health`.
- Keycloak place les rôles dans le claim `realm_access.roles`, format non reconnu nativement par
  Spring Security : écrivez un `Converter<Jwt, Collection<GrantedAuthority>>` qui lit ce claim et
  produit des autorités `ROLE_XXX`, pour que `@PreAuthorize("hasRole('COORDINATEUR')")` fonctionne
  sur vos controllers (US1/US2/US3).

**Critère d'acceptation** : un appel `GET /patients` sans token renvoie 401 ; avec un token valide
mais sans rôle `soignant`/`coordinateur`, 403 ; avec le bon rôle, 200.

**Concepts et notions concernées**
- OAuth2 / OIDC : Authorization Code Flow + PKCE, structure d'un JWT (header/payload/signature), access token, claims, JWKS/issuer.
- Spring Security : resource server (`spring-boot-starter-oauth2-resource-server`), `SecurityFilterChain`, gestion de session stateless, `Converter<Jwt, Collection<GrantedAuthority>>`, `@PreAuthorize`/method security.
- Angular : `keycloak-js` (flow OIDC bas niveau) vs `keycloak-angular` (guards de route, intercepteur HTTP tout fait), `HttpInterceptor` pour injecter le header `Authorization`, `environment.ts`/`environment.prod.ts`.
- Sécurité web : stockage du token en mémoire vs `localStorage` (risque XSS), CORS.
