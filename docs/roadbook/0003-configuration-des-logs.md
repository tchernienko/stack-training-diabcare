# 0003 — `logging.file.name` vs `logging.file.path`

## Contexte

Configuration voulue : logs dans `./data/logs/diabcare.log`. Config initiale :

```yaml
logging:
  file:
    path: target/logs
    name: diabcare.log
```

L'API démarrait mais **aucun fichier** n'apparaissait dans le répertoire attendu.

## Décision

Une seule propriété, `logging.file.name`, avec le chemin complet :

```yaml
logging:
  file:
    name: ./data/logs/diabcare.log
```

## Pourquoi

`logging.file.name` et `logging.file.path` sont **mutuellement exclusifs** :

| Propriété | Sens |
|---|---|
| `logging.file.name` | chemin **complet + nom** du fichier de log |
| `logging.file.path` | un **répertoire** seulement ; Spring y écrit un fichier nommé `spring.log` |
| les deux ensemble | `name` l'emporte, `path` est **ignoré** |

Avec `name: diabcare.log` (sans répertoire), Spring écrivait bien le fichier — mais relativement au
CWD (cf. [0002](0002-h2-fichier-et-resource-filtering.md)), donc à la racine du repo, pas dans
`target/logs`. Le `path` n'a jamais servi.

Le préfixe commun `logging.file.*` laisse croire à tort que les deux clés se combinent (l'une
donnant le dossier, l'autre le nom). C'est une verrue connue de Spring Boot, héritée du renommage
`logging.file` / `logging.path` → `logging.file.name` / `logging.file.path` en 2.2.

## Conséquences

- Pour tout besoin fichier au-delà du trivial (rotation, format, appenders séparés), ne pas
  s'acharner sur les propriétés : poser un `src/main/resources/logback-spring.xml`. Les balises
  `<springProfile>` et `<springProperty>` y donnent accès à `application.yml`, et `logging.level.*`
  continue de fonctionner par-dessus.
- Ce que Spring Boot **normalise bien** par ailleurs : le routage des logs. JUL, commons-logging et
  log4j2-api sont pontés vers SLF4J/Logback, donc toutes les libs finissent dans le même flux avec
  le même pattern, et `logging.level.<package>` s'applique quel que soit le framework natif de la lib.