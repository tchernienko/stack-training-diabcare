# 0004 — Validation métier + `@RestControllerAdvice` + `ProblemDetail`

## Contexte

`POST /patients` devait renvoyer **400** sur entrée invalide (nom vide, `dateNaissance` future ou aujourd'hui ou
absente, `typePompe` absent ou inconnu). En pratique :

- la validation vit dans le constructeur de `Patient` (agrégat) et lève
  `InvalidPatientParametersException` (une `RuntimeException`) ;
- aucun handler ne la traduisait → **500** ;
- `dateNaissance` absente → `NullPointerException` dans `dateNaissance.isAfter(...)` → **500** ;
- le controller contenait un `Optional.ofNullable(toDomain(...))` / `isEmpty()` → `badRequest()`
  qui était du **code mort** (`toDomain` ne renvoie jamais `null`, il *throw*).

## Décision

1. **La validation reste dans le domaine** (contrainte de l'US : « un `Patient` invalide ne doit
   pas pouvoir exister en mémoire »). Le constructeur couvre désormais aussi les `null` et ne
   laisse plus échapper de `NullPointerException` :

   ```java
   if (dateNaissance == null) {
       throw new InvalidPatientParametersException("dateNaissance", "Valeur obligatoire.");
   }
   if (dateNaissance.isAfter(LocalDate.now()) || dateNaissance.isEqual(LocalDate.now())) {
       throw new InvalidPatientParametersException("dateNaissance", "Valeur dans le futur ou aujourd'hui interdite.");
   }
   ```

2. L'exception porte le **champ fautif** :

   ```java
   public InvalidPatientParametersException(String field, String message) {
       super("Erreur sur le champ %s, car %s".formatted(field, message));
       this.field = field;
   }
   ```

3. Le controller se contente d'appeler `toDomain` et de laisser remonter — code mort supprimé.

4. Un `@RestControllerAdvice` traduit en `ProblemDetail` (RFC 7807, natif Spring Boot) :

   ```java
   @ExceptionHandler(InvalidPatientParametersException.class)
   ProblemDetail onInvalidPatient(InvalidPatientParametersException e) {
       ProblemDetail d = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
       d.setTitle("Paramètres patient invalides");
       d.setProperty("field", e.getField());
       return d;
   }

   @ExceptionHandler(HttpMessageNotReadableException.class)
   ProblemDetail onUnreadableBody(HttpMessageNotReadableException e) { ... 400 ... }
   ```

## Pourquoi

### Rôle des couches

Le controller (adaptateur primary) **traduit** HTTP ↔ domaine. Il ne valide pas, il ne décide pas
du métier. La validation est une règle d'agrégat ; sa traduction en code HTTP est un
`@RestControllerAdvice`. On ajoute une couche de traduction, on ne déplace pas la validation dans
le DTO ou le controller.

### Deux chemins pour `typePompe` — choix assumé

| Entrée | Ce qui se passe | Handler |
|---|---|---|
| `"typePompe": null` / absent | désérialisé en `null`, atteint le domaine | `InvalidPatientParametersException` → 400 |
| `"typePompe": "TUBULAR"` (valeur inconnue) | Jackson échoue **avant** de construire le domaine | `HttpMessageNotReadableException` → 400 |

On **garde volontairement les deux formats de réponse** (corps `ProblemDetail` différent selon le
cas), au prix d'un peu de complexité côté front sur ce point précis. Raison : ce projet est un
support d'apprentissage, et avoir les deux chemins donne de la matière pour comprendre la mécanique
`@RestControllerAdvice` + `@ExceptionHandler` (exception métier custom *vs* exception d'infra
Spring/Jackson).

L'alternative écartée : déclarer `typePompe` en `String` dans le DTO et convertir dans `toDomain()`,
ce qui ferait tout passer par `InvalidPatientParametersException` (un seul format), au prix de
perdre le binding d'enum automatique de Jackson.

### Pourquoi `ProblemDetail`

Format standard (`application/problem+json`), supporté nativement par Spring Boot, extensible via
`setProperty`. Évite d'inventer un DTO d'erreur maison.

## Conséquences

- `HttpMessageNotReadableException` couvre plusieurs cas d'un coup : JSON malformé, corps absent,
  valeur d'enum inconnue, date au mauvais format. Tous → 400.
- Le `@RestControllerAdvice` est global (pas de `basePackages`). Suffisant tant qu'il n'y a qu'un
  advice ; si d'autres modules en ajoutent, surveiller les conflits de mapping et l'ordre.
- Le front doit gérer deux formes de corps 400 pour l'erreur « type de pompe » (`field: "typePompe"`
  d'un côté, message générique « Requête illisible/invalide » de l'autre). Décision consciente.
- Tests à écrire (US1 / US6) : nom vide → 400 + `field=nom` ; date future ou aujourd'hui → 400 + `field=dateNaissance` ;
  date absente → 400 (plus 500) ; `typePompe` inconnu → 400.