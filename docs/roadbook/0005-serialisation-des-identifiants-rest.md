# 0005 — Identifiant « à plat » dans le JSON (`@JsonValue` / `@JsonCreator`)

## Contexte

`RestResponsePatient.id` est typé `RestPatientId` (wrapper autour d'un `UUID`). Sérialisé tel quel,
le JSON de réponse donnait :

```json
{ "id": { "value": "6c0f43b3-..." }, "nom": "...", ... }
```

alors qu'on veut `"id": "6c0f43b3-..."` — l'`id` à plat.

## Décision

Garder le **type fort** `RestPatientId` et le rendre transparent à Jackson :

```java
public class RestPatientId {

    private UUID value;

    @JsonCreator
    public static RestPatientId of(UUID patientId) {
        return new RestPatientId(patientId);
    }

    @JsonValue
    public UUID getValue() {
        return value;
    }
}
```

## Pourquoi

- `@JsonValue` sur `getValue()` : à la **sérialisation**, l'objet est remplacé par sa seule valeur
  → `"id": "<uuid>"`.
- `@JsonCreator` sur la factory mono-argument : à la **désérialisation**, une chaîne nue redevient
  un `RestPatientId`. Pas utile aujourd'hui (l'id n'apparaît qu'en sortie), mais anticipé :
  une US « récupérer un patient par son id » (`GET /patients/{id}`) est probable, et le type fort
  y sera un point d'ancrage naturel.

### Alternative écartée

Typer `RestResponsePatient.id` directement en `UUID`. Plus simple pour un DTO de sortie, mais on
perd le type fort qu'on veut réutiliser côté requête plus tard. Choix : garder le wrapper.

### Piège Lombok

`@Getter` de Lombok régénérerait un `getValue()` **sans** `@JsonValue`. Il faut donc soit écrire le
getter à la main (fait ici), soit utiliser `@Getter(onMethod_ = @__(@JsonValue))`. La version
manuelle est plus lisible.

## Conséquences

- Réponse conforme : `"id": "<uuid>"`.
- `RestPatientId` est désormais symétrique (entrée/sortie) pour le jour où une US de lecture par id
  arrive.
- `RestResponsePatient` garde `@Getter @Builder` : Jackson sérialise `getId()` → `RestPatientId`
  → `@JsonValue` → chaîne. Chaîne complète vérifiée.