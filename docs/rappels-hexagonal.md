# Architecture hexagonale de diabcare : schémas

Les noms de packages reprennent ceux du dépôt (`patient/domain`, `application`, `infrastructure/primary`, `infrastructure/secondary`).

## 1. Sens des dépendances dans un contexte (exemple : `rdv`)

Une flèche « A → B » signifie « A connaît B » (A importe des classes de B).
La règle : **tout pointe vers le domaine, rien ne sort du domaine**.

```mermaid
flowchart LR
    subgraph primaire["infrastructure/primary (côté qui appelle)"]
        Resource["RdvResource<br/>(contrôleur REST)"]
        DtoReq["RestRequestRdv /<br/>RestResponseRdv"]
        Handler["RdvExceptionHandler"]
    end

    subgraph appli["application"]
        Service["RdvApplicationService<br/>(cas d'usage)"]
        Port["RdvRepositoryPort<br/>(interface)"]
    end

    subgraph domaine["domain"]
        Agregat["RendezVous<br/>(racine d'agrégat)"]
        VO["RdvId, Statut, ...<br/>(objets valeur)"]
    end

    subgraph secondaire["infrastructure/secondary (côté qui est appelé)"]
        Adapter["DbRdvRepositoryAdapter"]
        Entity["RdvJpaEntity"]
        SpringRepo["SpringJpaRdvRepository"]
    end

    Resource --> Service
    Resource --> DtoReq
    Handler --> Service
    Service --> Port
    Service --> Agregat
    Port --> Agregat
    Agregat --> VO
    Adapter -. "implémente" .-> Port
    Adapter --> Agregat
    Adapter --> Entity
    Adapter --> SpringRepo
```

Points à retenir :
- `domain` n'importe **rien** d'autre que du Java (ni Spring, ni JPA, ni Jackson).
- `application` connaît le domaine, et définit les **ports** (interfaces) dont il a besoin.
- Les adapters connaissent les ports et le domaine. **Jamais l'inverse.**
- L'entité JPA et les DTO REST sont distincts de l'agrégat : l'adapter convertit dans les deux sens.

## 2. Ordre d'implémentation d'un endpoint

On construit du centre vers l'extérieur : chaque étape ne s'appuie que sur ce qui existe déjà.

```mermaid
flowchart LR
    A["1. Domaine<br/>règles, invariants<br/><i>test unitaire pur</i>"] --> B["2. Application<br/>cas d'usage + ports<br/><i>test avec faux port</i>"]
    B --> C["3. Adapters secondaires<br/>JPA, Keycloak, Kafka<br/><i>test d'intégration</i>"]
    C --> D["4. Adapter primaire<br/>contrôleur, DTO, erreurs HTTP<br/><i>@WebMvcTest</i>"]
```

## 3. Entre deux contextes : on passe par un port, pas par le domaine de l'autre

Le contexte `rdv` a besoin de savoir si un patient existe, mais il ne doit pas importer `Patient` ni son domaine.
Il **définit son propre port**, formulé dans son vocabulaire. Un adapter de `rdv` le réalise en appelant l'API applicative de `patient`.

```mermaid
flowchart LR
    subgraph rdv["contexte rdv"]
        RdvService["RdvApplicationService"]
        PortPatient["PatientExistantPort<br/>(« ce patient existe-t-il ? »)"]
        RdvDomaine["RendezVous<br/>garde seulement un identifiant de patient"]
        AdapterPatient["PatientAdapter<br/>(infrastructure/secondary)"]
    end

    subgraph patient["contexte patient"]
        PatientService["PatientApplicationService<br/>(API publique du contexte)"]
        PatientDomaine["Patient"]
    end

    RdvService --> PortPatient
    RdvService --> RdvDomaine
    AdapterPatient -. "implémente" .-> PortPatient
    AdapterPatient --> PatientService
    PatientService --> PatientDomaine
```

Points à retenir :
- `RendezVous` référence le patient par son **identifiant** (un `PatientId` ou un simple identifiant), jamais par un objet `Patient`.
- Le seul code de `rdv` qui connaît `patient` est l'adapter. Si `patient` change, un seul fichier bouge.
- Quand les deux contextes seront séparés plus tard (ou communiqueront par Kafka), seul l'adapter change.

## 4. Agrégat : où passe la frontière ?

```mermaid
flowchart TB
    subgraph agg1["Agrégat RendezVous (cohérence garantie par la racine)"]
        R["RendezVous<br/><b>racine</b>"]
        R --> D1["date, durée, statut"]
        R --> D2["type de RDV"]
    end

    subgraph agg2["Agrégat Patient"]
        P["Patient<br/><b>racine</b>"]
        P --> P1["pompe, ..."]
    end

    R -. "référence par identifiant<br/>(PatientId), pas par objet" .-> P
```
