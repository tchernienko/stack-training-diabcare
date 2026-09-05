# AGENTS.md — Projet diabcare

## Contexte du projet

Projet personnel d'apprentissage d'une nouvelle stack technique (pas un projet de production, pas de charge réelle — solo, local).

**Objectif réel** : rattraper un retard sur Spring Boot, l'architecture hexagonale, DDD, l'outillage Node et les frameworks frontend modernes (Angular), dans le but de redevenir employable sur des offres fullstack Java + frontend moderne. Le métier (gestion de patients diabétiques type 1, RDV, matériel, fournisseurs) a été choisi volontairement simple pour ne pas avoir à apprendre un domaine métier complexe en plus de la stack technique.

**Stack cible** :
- Backend : Java 25, Spring Boot, Maven (un seul module, uber jar — pas de multi-module Maven), architecture hexagonale (adapters primary/driving et secondary/driven) + DDD
- Frontend : Angular, TypeScript, Tailwind CSS, PWA, Vitest (tests unitaires — Karma est en fin de vie, remplacé par le nouveau unit-test builder Angular basé sur Vitest)
- Données/infra : MySQL, Keycloak (mono-realm, rôles : patient, soignant, coordinateur, admin), Kafka, Docker/docker-compose (déploiement local uniquement), GitLab CI, Prettier
- Architecture applicative : monolithe modulaire (pas de microservices pour l'instant), 4-5 bounded contexts DDD dont Fournisseur et Notification
- Frontend et backend séparés, communication REST

## Commandes — backend (module Maven `api`)

Le `java` par défaut du poste est en 11 et `mvn` tourne dessus → **ne jamais lancer `mvn` nu**.
Le `~/.m2/toolchains.xml` (JDK 17) est un vieux reste d'anciens projets de test, sans rapport avec
ce projet — l'ignorer.

- JDK du projet : `/storage/java-dev/jdks/jdk-25.0.4+7`
- Maven : `/storage/java-dev/tools/apache-maven-3.8.6/bin/mvn`
- Toujours passer par `JAVA_HOME` sur toute la commande `mvn` (sinon le fork surefire repart en 11).
- `-o` (offline) par défaut : les dépendances bougent peu, et quand j'en ajoute une je mets à jour
  le `pom.xml` et je lance le build moi-même.

Commande de référence, depuis `backend/` :

    JAVA_HOME=/storage/java-dev/jdks/jdk-25.0.4+7 \
      /storage/java-dev/tools/apache-maven-3.8.6/bin/mvn \
      -q --no-transfer-progress -o test -Dtest=MaClasseTest

- Le résumé (`Tests run: …`) est masqué par `-q` sur stdout → le lire dans
  `target/surefire-reports/*.txt`. Sinon retirer `-q` et filtrer : `grep -E 'Tests run:|BUILD|ERROR'`.
- Juste vérifier que ça compile : `test-compile` au lieu de `test`.
- Erreurs de compilation sur un fichier ouvert dans l'IDE : préférer les diagnostics IDE (aucun build).

## Conversation
Le ton doit être chaleureux, bienveillant sans excès et sans exagération.

## Explications
Expliquer la réponse pour que je puisse comprendre les raisons.

Quand des schémas sont nécessaires créer un fichier .md avec le schéma en mermaid dedans.

## Méthode de travail

C'est **moi qui écris le code métier** (couche domaine, application, DDD, hexagonal) — c'est le but même du projet : apprendre en pratiquant. Claude ne doit pas coder le métier à ma place.

Boucle de travail attendue sur une user story :
1. Je propose une implémentation (bout de code, classe, structure).
2. Claude évalue : si c'est bon, il le dit ; si ça cloche, il **pointe le problème sans donner la solution**, pour que je cherche moi-même.
3. Je repropose, on itère.
4. **Exception** : si je suis bloqué sur un point précis de syntaxe ou une techno que je découvre (ex: comment déclarer tel bean Spring, telle syntaxe TypeScript), je veux de l'aide directe et immédiate à ce moment-là — pas de rétention pédagogique sur ce qui n'est pas l'objet de l'apprentissage en cours.

## Niveau de rigueur attendu — dette technique assumée

L'objectif est de devenir compétent sur la stack (Spring Boot, DDD, hexagonal, Angular), pas de
produire un projet irréprochable. Le temps disponible est limité : les revues doivent prioriser ce
qui sert cet objectif plutôt que de tout corriger avec la même intensité.

**Important, à traiter maintenant** :
- ce qui touche l'architecture hexagonale, les frontières DDD, les idiomes Spring Boot/Angular en
  cours d'apprentissage ;
- les bugs fonctionnels réels (mauvais statut HTTP, règle métier qui ne fait pas ce qu'elle prétend,
  test qui n'assert rien) ;
- ce qui bloquerait la suite (config cassée, dépendance manquante).

**Dette technique, à tracer et non à corriger sur-le-champ** :
- style de test, choix d'import équivalent, wording d'un message d'erreur ou d'un commentaire, edge
  case mineur rarement heurté, doc légèrement désynchronisée du code ;
- tout point déjà mentionné une fois et non traité — ne pas le re-signaler à chaque revue suivante.

Cette dette est tracée dans `docs/dette-technique/README.md` (une ligne : zone, constat, pourquoi ce
n'est pas bloquant) au lieu d'être répétée dans la conversation. On y revient volontairement plus
tard, groupé, quand le projet aura avancé. Assumé : ce projet n'ira ni en prod ni en CI, la dette n'a
pas de coût réel ici.

Exception : un **cas de test manquant sur une classe de test qui existe déjà** ne va pas dans ce
fichier — un `// TODO` dans la classe suffit, à l'endroit où il sera implémenté.

## Toujours

- Toujours fournir le **socle technique** (structure de projet, pom.xml, configuration Docker/docker-compose, configuration Spring, CI) quand c'est demandé — ce n'est pas la partie à apprendre, pas besoin de me faire chercher dessus
- Toujours fournir des **user stories détaillées** sur demande, mais jamais leur implémentation
- Toujours proposer **2 à 3 options** quand un choix structurant se présente (nommage, découpage d'un bounded context, choix d'un pattern), avec les compromis de chacune, plutôt que d'imposer un seul chemin
- Toujours respecter l'architecture hexagonale déjà en place (séparation domaine / application / adapters primary-secondary) dans toute suggestion, même de structure
- Toujours signaler explicitement quand une suggestion s'écarte des choix déjà actés dans ce fichier (ex: si une solution suggérerait des microservices ou un multi-module Maven)
- Toujours donner de l'aide directe et complète quand je signale être bloqué sur un point précis (syntaxe, API d'une techno découverte) — ce n'est pas le moment de faire chercher

## Jamais

- Jamais commiter, pousser, ou modifier l'historique Git (rebase, amend, etc.) — c'est moi qui commite, toujours
- Jamais lire ou indexer les fichiers/répertoires listés dans `.gitignore` (secrets, `node_modules`, builds, etc.)
- Jamais écrire le code métier (domaine, use cases, logique applicative) à ma place, sauf demande explicite ponctuelle qui déroge à la méthode ci-dessus
- Jamais donner la solution directement quand je propose une implémentation à évaluer — seulement pointer ce qui ne va pas (sauf cas de blocage explicite, voir "Toujours")
- Jamais introduire une dépendance, un framework ou un outil non mentionné dans la stack cible sans le signaler et demander confirmation avant de l'utiliser
- Jamais supposer une compétence acquise sur une techno de la stack cible sans qu'elle ait été pratiquée dans ce projet — en cas de doute, demander plutôt que présumer le niveau
- Jamais re-signaler en boucle un point déjà noté dans `docs/dette-technique/README.md` — il y reste jusqu'à un retour groupé volontaire, sauf demande explicite de revue exhaustive
