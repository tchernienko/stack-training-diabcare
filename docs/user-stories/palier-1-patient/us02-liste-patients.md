# US2 — Le soignant (et le coordinateur) consulte la liste des patients

**Endpoint**

`GET /patients` — rôle requis : `soignant` ou `coordinateur`
- 200, liste des patients (id, nom, dateNaissance, typePompe)

`GET /patients/{id}` — même rôle
- 200 si trouvé, 404 sinon

**Frontend** (`frontend-angular/src/app/patients`)
- Composant Angular listant les patients (appel `GET /patients` via un service Angular
  dédié — `HttpClient`), tableau simple avec nom / date de naissance / type de pompe.
- Route déclarée dans le routing Angular (`app.routes.ts` ou module de routing dédié selon votre
  organisation).
- Formulaire de création (US1) réservé aux coordinateurs — cachez ou désactivez le bouton si le rôle
  courant ne l'a pas (l'autorisation réelle reste côté backend, l'UI n'est qu'un confort).

**Concepts et notions concernées**
- Spring Boot : endpoints GET, projection en lecture (DTO simplifié), gestion du 404.
- Angular : composants standalone, service `@Injectable`, `HttpClient`, `Observable` (RxJS) et consommation (`async` pipe vs `subscribe`), nouvelle syntaxe de contrôle de flux (`@for`/`@if`) vs `*ngFor`/`*ngIf`, routing (`provideRouter`, `Routes`), Angular Material (modules `Mat*` importés dans le `imports:` de chaque composant standalone). Détail des écrans dans [docs/user-stories/frontend-angular/](../frontend-angular/).
- TypeScript : interfaces pour typer les DTOs reçus du backend.
- UX/autorisation : masquage conditionnel d'UI selon un rôle (à distinguer de la sécurité réelle, côté backend).
