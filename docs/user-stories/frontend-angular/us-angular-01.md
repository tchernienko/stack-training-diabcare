# US Angular 1 — Enabler technique : le front parle à l'API Spring sans se cogner au CORS

**Contexte**

En développement, deux serveurs tournent séparément : le dev server Angular sur
`http://localhost:4200` et l'API Spring Boot sur `http://localhost:8080`. Un appel `fetch`/XHR de
l'un vers l'autre est une requête **cross-origin** : le navigateur applique la *Same-Origin Policy*
et masque la réponse au JavaScript appelant tant que le serveur ne renvoie pas les en-têtes CORS
adéquats. On veut lever cet obstacle **sans** disperser des URLs absolues dans le code ni maintenir
une liste d'origines autorisées à la main.

Le backend expose toute son API sous le préfixe `/api` (`server.servlet.context-path: /api` dans
`application.yml`) : `GET /api/patients` répond, `GET /patients` renvoie un 404 Tomcat.

**Frontend** (`frontend-angular/`)

- Fichier de configuration proxy à la racine du projet Angular (`proxy.conf.json` par convention) :
  toute requête dont le chemin commence par `/api` est routée telle quelle vers
  `http://localhost:8080` (`changeOrigin: true`). Pas de `pathRewrite` : le préfixe `/api` du front
  correspond exactement au `context-path` du backend.
- Déclaration du proxy dans `angular.json`, cible `serve` → `options.proxyConfig`, pour qu'un simple
  `npm start` le prenne en compte sans passer de flag `--proxy-config`.
- Contrat pour tout le reste du front : les services Angular appellent des URLs **relatives**
  (`/api/patients`), jamais `http://localhost:8080/...` en dur.

**Critères d'acceptation**

- Backend lancé + `npm start` : un `GET /api/patients` déclenché depuis l'application renvoie `200`
  et le JSON du backend, **sans** erreur CORS dans la console navigateur.
- Aucune occurrence de `localhost:8080` (ni d'une autre URL absolue d'API) dans le code TypeScript.
- `ng build` (prod) ne contient aucune trace du proxy : c'est un artefact de développement.
- Une requête `POST /api/patients` (donc avec *preflight* `OPTIONS`) passe elle aussi.

**Alternative : le CORS backend (déjà en place, complémentaire)**

- `bootstrap/security/CorsConfig` autorise explicitement l'origine `http://localhost:4200` sur
  `/api/**`. Depuis que l'API vit réellement sous `/api` (`context-path`), ce mapping est effectif
  et le CORS fonctionne pour un appel cross-origin direct (`http://localhost:8080/api/...`).
- Proxy et CORS ne s'excluent pas et ne font pas doublon :
  - le **proxy** est le chemin de dev — il garde le code front en URLs **relatives** (aucune base
    URL à configurer, aucun fichier `environments/`), et les requêtes restent same-origin (pas de
    *preflight*, aucune dépendance à la liste d'origines pendant le dev) ;
  - le **CORS** reste nécessaire dès qu'un navigateur tape l'API depuis un autre domaine réel
    (préprod, prod avec front et API sur des hôtes distincts) — sauf si tout est servi same-origin
    derrière un unique reverse-proxy.
- Choix acté pour cette US : on garde le proxy pour le dev. Retirer le proxy resterait possible
  (le CORS suffit techniquement), au prix d'une base URL absolue + d'un fichier `environments/`
  côté front.

**Concepts et notions concernées**

- **Same-Origin Policy & CORS** : ce qu'est une « origine » (schéma + hôte + port — changer l'un des
  trois suffit à rendre la requête cross-origin) ; CORS est une protection **navigateur**, pas
  serveur (`curl`, Postman, les tests backend ne sont jamais bloqués) ; requêtes « simples » vs
  requêtes déclenchant un **preflight** `OPTIONS` (cas d'un `POST` avec `Content-Type:
  application/json`) ; les en-têtes `Access-Control-Allow-Origin / -Methods / -Headers /
  -Credentials`.
- **Proxy de dev** : le builder `@angular/build:dev-server` (esbuild/Vite) embarque un proxy HTTP ;
  clés `target`, `pathRewrite` (réécriture du chemin), `changeOrigin` (réécrit l'en-tête `Host`
  envoyé au backend), `secure` (accepte un certificat TLS auto-signé). Résultat : le navigateur ne
  voit qu'une requête **same-origin** vers `:4200`, le dev server relaie côté serveur.
- **`angular.json`** : rôle du fichier — configuration des *builders* pour chaque cible d'un projet
  (`build`, `serve`, `test`, `extract-i18n`). Distinction `options` (toujours appliquées) vs
  `configurations` (`development` / `production`, sélectionnées par `defaultConfiguration` ou un
  flag).
- **URLs relatives vs absolues** : coder en relatif rend le **même** bundle déployable derrière
  n'importe quel domaine ; le routage réseau (proxy en dev, reverse-proxy type nginx / ingress en
  prod) reste **hors** du code applicatif.
- **Fichier `environments/` Angular** (pour situer) : l'autre levier classique pour injecter une
  base URL différente selon l'environnement (`environment.ts` / `environment.prod.ts`, remplacement
  au build via `fileReplacements`). Ici on s'en passe : le proxy en dev + les URLs relatives
  suffisent, et on évite une base URL configurable à maintenir.
- **`provideHttpClient()`** : sans ce provider dans `app.config.ts`, `HttpClient` n'est pas
  injectable et l'application plante à l'exécution ; `withFetch()` bascule l'implémentation sur
  l'API `fetch` du navigateur (recommandation actuelle) plutôt que `XMLHttpRequest`.