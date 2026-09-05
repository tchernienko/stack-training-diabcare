# US6 — Un soignant ne peut pas créer de patient

**Test à écrire** (backend, `@WebMvcTest` ou test d'intégration avec un JWT mocké) : un appel
`POST /patients` avec un token portant uniquement le rôle `soignant` renvoie 403.

**Concepts et notions concernées**
- Tests Spring Security : simulation d'un JWT dans un test (ex. `SecurityMockMvcRequestPostProcessors.jwt()`), test d'une règle d'autorisation négative (403).
