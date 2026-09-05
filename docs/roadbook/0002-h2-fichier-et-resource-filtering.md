# 0002 — Base H2 en mode fichier, Maven resource filtering, répertoire de travail

## Contexte

Passage de H2 en mémoire (`jdbc:h2:mem:diabcare`) à une base **fichier** pour conserver les
données entre deux démarrages, avec l'idée que le chemin ne soit pas en dur.

Première tentative : `jdbc:h2:file:///${project.basedir}/data/diabcare.db` avec Maven resource
filtering activé sur `application.yml`. Le placeholder `${project.basedir}` n'était **pas**
remplacé.

## Décision

Chemin **relatif**, résolu par H2 par rapport au répertoire de travail du process, sans resource
filtering :

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/db/diabcare
```

Et bloc `<resources><filtering>true</filtering>…` retiré du `pom.xml`.

## Pourquoi

### Le filtering `${…}` ne marche pas sous Spring Boot

`spring-boot-starter-parent` reconfigure `maven-resources-plugin` :

```
<resource.delimiter>@</resource.delimiter>
<useDefaultDelimiters>false</useDefaultDelimiters>
```

Pendant le filtering, Maven ne reconnaît donc que le délimiteur `@…@`, pas `${…}`. C'est
**volontaire** : ça évite que Maven touche aux placeholders de Spring lui-même
(`${DB_USER:SA}`, `${KAFKA_BOOTSTRAP_SERVERS:…}` présents dans le fichier). `${project.basedir}`
passe au travers intact, puis Spring essaie de le résoudre au runtime, échoue, et laisse la chaîne.

Pour filtrer avec Maven il aurait fallu écrire `@project.basedir@`. Mais :

- ça fige un chemin **absolu machine** dans `target/classes/application.yml` et dans le jar → non portable ;
- le filtering n'a lieu que si les resources passent par Maven ; l'IDE peut copier le fichier sans
  filtrer selon la config de build.

### Alternatives runtime (sans Maven)

- `jdbc:h2:file:./data/db/diabcare` — relatif au CWD. **Choisi.**
- `jdbc:h2:file:${user.dir}/data/db/diabcare` — `user.dir` est une propriété JVM résolue par Spring,
  donne un absolu stable quel que soit l'appelant.

### Syntaxe d'URL H2 — pièges

- `jdbc:h2:file:<path>` : un seul `/` initial pour un chemin absolu. `file:///…` (triple slash)
  est incorrect.
- Ne pas mettre `.db` dans le nom : H2 ajoute `.mv.db`. `diabcare.db` → fichier `diabcare.db.mv.db`.
  D'où le nom final `diabcare` → `data/db/diabcare.mv.db`.

## Répertoire de travail (CWD) au lancement

Le CWD dépend de **comment** on lance :

| Lancement | CWD |
|---|---|
| IntelliJ, run config Spring Boot **sans** `Working directory` défini | racine du projet (constaté : `data/` créé à la racine du repo, pas dans `backend/`) |
| `mvn spring-boot:run` | `${project.basedir}` → `backend/` |
| `java -jar target/api.jar` | le répertoire du shell |

Un chemin relatif rend donc l'emplacement de la base **non déterministe**. Pour ce projet solo
c'est acceptable ; si ça devient gênant, passer à `${user.dir}` ou à une variable d'env dédiée
(`${DB_PATH:./data/db/diabcare}`) et fixer `Working directory` dans la run config.

## Conséquences

- `data/db/diabcare.mv.db` et `data/logs/` sont créés là où l'API est lancée (aujourd'hui : racine
  du repo). **À ajouter au `.gitignore`** (`*.log` couvre déjà les logs, pas `*.mv.db`).
- `ddl-auto` est passé à `create` : avec une base **fichier persistante**, cela **efface et recrée
  le schéma à chaque démarrage** → les données ne survivent pas au restart, ce qui contredit
  l'objectif du mode fichier. À repasser à `update` (ou `validate` + Flyway/Liquibase plus tard)
  si on veut réellement de la persistance inter-sessions.
- Anciens artefacts à nettoyer : `diabcare.log` et `data/` éventuellement créés à la racine par les
  essais précédents.