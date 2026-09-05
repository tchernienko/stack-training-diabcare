# US12 — Un test ArchUnit vérifie les frontières hexagonales

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

**Concepts et notions concernées**
- ArchUnit : `ArchRule`, `classes()...should()...`, `@AnalyzeClasses`, tests de règles architecturales (couches, dépendances interdites).
- Architecture hexagonale/modulaire : vérification automatisée des frontières (domain isolé du framework, primary/secondary non couplés, modules isolés entre eux).
