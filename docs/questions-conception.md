# Questions à se poser pour chaque agrégat / user story

Liste à parcourir **avant** d'écrire le code d'une US. Chaque question sans réponse devient soit une précision dans l'US, soit une entrée dans `docs/dette-technique` (question ouverte), soit une note de `docs/roadbook` (décision prise). Une question peut être écartée en une ligne ("sans objet parce que…") : l'important est de l'avoir posée.

## 1. Cycle de vie de l'agrégat

- Qui le crée, et dans quel état ? (`planifie`, `creer`… : création métier, pas reconstitution)
- Quels états existent, et quelles transitions sont autorisées entre eux ? Qui les déclenche ? (ex. `PLANIFIE` vers `ANNULE` ou `HONORE`)
- Peut-on le modifier ? Quels champs sont figés après création ?
- Peut-on le supprimer ? **Suppression logique (archivage) ou physique ?** Que deviennent alors les objets qui le référencent ?
- Un objet archivé reste-t-il visible dans les listes ? Peut-on encore lui attacher de nouveaux objets ?

## 2. Références entre agrégats

- Quels autres agrégats référence-t-il, et par quelle identité ? (jamais par objet)
- Qui vérifie que la référence existe, et quand ? (à la création, par le service applicatif, via un port défini par le consommateur)
- Que se passe-t-il si la cible est archivée ou supprimée ensuite ? (orphelins : suppression logique, cascade écrite à la main, événement interne, clé étrangère, nettoyage périodique)
- Y a-t-il une clé étrangère en base entre les deux ? Si non, c'est un choix à noter, pas un oubli.

## 3. Invariants et validation

- Quelles règles sont toujours vraies pour cet agrégat ? Où sont-elles vérifiées : constructeur canonique (reconstitution incluse) ou méthode de création uniquement ?
- Pour chaque règle violée : erreur de l'utilisateur (exception du domaine, code 400) ou erreur du programmeur ou des données (exception technique, code 500) ?
- Quand plusieurs règles sont violées en même temps, laquelle est signalée en premier ? Est-ce spécifié ?
- Existe-t-il des règles entre plusieurs instances ? (ex. un patient ne peut pas avoir deux rdv au même moment ; unicité d'un identifiant métier). Où les vérifie-t-on, et comment résiste-t-on à deux requêtes simultanées ?

## 4. API

- Quels endpoints, quels verbes, quels codes de retour (201, 200, 400, 404, 409) ?
- Quel format d'erreur ? (`ProblemDetail` ; message destiné à l'utilisateur ou au développeur ?)
- Pour une lecture par filtre (ex. liste par patient) : que renvoie-t-on si la cible n'existe pas ? (liste vide, 404, 400)
- Listes : tri, pagination, filtres ? Combien d'éléments au maximum ?
- Les identifiants sont-ils exposés à plat dans le JSON ? (voir roadbook 0005)

## 5. Sécurité et données

- Quel rôle peut faire quoi ? Un utilisateur ne voit-il que ses propres données (propriétaire) ?
- Quelles données sont des données de santé ou personnelles ? Apparaissent-elles dans les logs (niveau `debug`, message d'exception) ?

## 6. Persistance

- Quelles colonnes sont obligatoires (`nullable = false`), uniques, indexées ?
- Les enums sont-ils stockés en texte (`EnumType.STRING`) ? Les UUID, dates et fuseaux sont-ils mappés explicitement ?
- Qui est responsable des transactions, et à quelle couche ? (voir roadbook 0006)
- La structure doit-elle fonctionner sur PostgreSQL comme sur H2 ? (différences à surveiller)

## 7. Temps

- Qui fournit "maintenant" ? Un `Clock` injecté rend les tests déterministes ; `LocalDateTime.now()` dans le domaine oblige à choisir des dates très éloignées dans les tests.
- Fuseau horaire : dates locales ou instants ? Que stocke-t-on ?

## 8. Événements et effets de bord

- La création, la modification ou l'archivage déclenchent-ils quelque chose ailleurs (notification, autre contexte) ?
- Si oui : appel direct, événement interne au processus, ou message externe (Kafka) ? Pourquoi ? (Kafka n'est pas la réponse par défaut)

## 9. Tests : qui prouve quoi

Avant d'écrire un test, noter en une phrase **ce qu'il prouve que les autres ne prouvent pas**.

| Couche | Ce qu'elle prouve | Ce qu'elle ne peut pas prouver |
|---|---|---|
| Domaine (unitaire) | Règles métier, états, exceptions du domaine | Branchement Spring, persistance |
| Service applicatif (ports simulés) | Orchestration : qui est appelé, dans quel ordre, ce qui n'est **pas** appelé | Mapping, SQL, JSON |
| Adaptateur secondaire (base réelle) | Annotations, colonnes, conversion entité / domaine, requêtes | Règles métier |
| Entité JPA (unitaire) | Conversion `of` / `toDomain` précise, localisée | Annotations |
| Adaptateur primaire (`@WebMvcTest`) | JSON, codes HTTP, traduction des exceptions | Persistance, règles métier |

- Un test d'adaptateur avec mock fait double emploi avec le test d'intégration du même adaptateur.
- Chaque test de comportement a une seule raison d'échouer. Les valeurs attendues viennent de l'entrée du test, pas de la sortie observée.
- Test de rougissement : casser volontairement le code testé, le test doit échouer.

## 10. Où consigner la réponse

| Nature | Où |
|---|---|
| Règle métier tranchée | US concernée (section "Règles") |
| Décision d'architecture ou de conception | `docs/roadbook` (contexte, décision, pourquoi, conséquences ; ajouter un statut : proposée / acceptée / remplacée) |
| Question sans réponse | `docs/dette-technique` |

---

## Décisions déjà prises dans diabcare (au 12/11/2026)

- Les agrégats se référencent par identité (`PatientRef` dans `Rdv`), jamais par objet.
- Pas de clé étrangère entre `rdv` et `patient` : conséquence d'une référence par identité, **non pesée au départ**, désormais assumée ; la cohérence est portée par le service applicatif (`patientExiste` avant la sauvegarde).
- Suppression : **suppression logique** (archivage), pas de suppression physique de patient (santé, projet d'apprentissage). Conséquence à préciser le jour où une US de suppression existera : un patient archivé garde ses rdv visibles ; peut-on lui planifier un nouveau rdv ?

## Application à l'US3 (questions encore ouvertes)

- `GET /rdv?patientId=...` : que renvoie-t-on si le patient n'existe pas ? (non spécifié)
- Un même patient peut-il avoir deux rdv au même moment ? (non spécifié ; règle entre instances, voir section 3)
- Les statuts `ANNULE` et `HONORE` existent mais aucune US ne fait passer un rdv à ces états : transitions à spécifier.
- Dates : `LocalDateTime.now()` dans le domaine ou `Clock` injecté ? (section 7)

## Questions en suspend
```
quoi faire du pb que l'adapter peut persister un
rdv invalide si on lui passe, ou le pb que la base autorise un rdv sans patient correspondant
(cf discussion avec claude sur le sujet des domaines qui s'ignorent) ou de "peut-on supprimer des patients?" -> oui de façon logique
et "que faire des rdv associés aux patients supprimés?" -> on peut sans doute les garder par souci de
cohérence du point de vue du planning du coordinateur et du soignant
```

