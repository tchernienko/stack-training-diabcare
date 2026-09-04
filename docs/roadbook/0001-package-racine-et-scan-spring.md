# 0001 — Placement de la classe `@SpringBootApplication` et scan des repositories / entités

## Contexte

Au démarrage de l'API : `NoSuchBeanDefinitionException` pour `SpringJpaPatientRepository`,
alors que l'interface était annotée `@Repository`.

La classe `DiabcareApplication` était dans `com.example.diabcare.bootstrap`, et le reste du code
dans `com.example.diabcare.patient.*`. Un `@SpringBootApplication(scanBasePackages = "com.example.diabcare")`
avait été ajouté pour élargir le scan.

## Décision

Déplacer `DiabcareApplication` dans le package racine `com.example.diabcare`, et retirer
`scanBasePackages` ainsi que le `@EnableJpaRepositories` explicite.

```java
package com.example.diabcare;

@SpringBootApplication
public class DiabcareApplication {
    static void main(String[] args) {
        SpringApplication.run(DiabcareApplication.class, args);
    }
}
```

## Pourquoi

Deux mécanismes de scan distincts, qui ne réagissent pas aux mêmes réglages :

| Scan | Ce qu'il ramasse | Package de base par défaut | Sensible à `scanBasePackages` ? |
|---|---|---|---|
| Component scan | `@Component`, `@Service`, `@RestController`, `@Configuration` | package de la classe `@SpringBootApplication` | **oui** |
| `@EnableJpaRepositories` (auto-config Boot) | interfaces Spring Data | package de la classe `@SpringBootApplication` | **non** |
| `@EntityScan` (auto-config Boot) | `@Entity` | package de la classe `@SpringBootApplication` | **non** |

`scanBasePackages` n'élargit que le premier. Les repositories et les entités continuaient d'être
cherchés dans `com.example.diabcare.bootstrap` uniquement → l'interface dans
`patient.infrastructure.secondary` n'était jamais transformée en bean → échec d'injection dans
`DbPatientRepositoryImpl`.

Mettre la classe principale à la racine fait pointer les **trois** scans sur `com.example.diabcare`
et tous ses sous-packages. C'est la convention Spring Boot ; s'en écarter oblige à re-déclarer
manuellement `@EnableJpaRepositories(basePackages = …)` et `@EntityScan(basePackages = …)`.

### Sur `@Repository` posé sur l'interface Spring Data

Inutile et trompeur : ce n'est pas cette annotation qui crée le bean (on n'instancie pas une
interface). Le proxy est généré par l'infrastructure Spring Data dès que le scan repository voit
l'interface. `@Repository` a été retiré.

## Conséquences

- Convention respectée, configuration minimale.
- Si un jour le code sort de `com.example.diabcare.*`, il faudra `@EnableJpaRepositories` /
  `@EntityScan` explicites — mais ce n'est pas souhaitable dans ce monolithe modulaire.
- `@Repository` a du sens en revanche sur l'**adaptateur** (`DbPatientRepositoryImpl`) : c'est là
  que la traduction d'exceptions de persistance de Spring s'applique. Pour l'instant il porte
  `@Component`, ce qui suffit.