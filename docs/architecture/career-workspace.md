# Architecture du Career Workspace

## Extension CRM Phase 3

```text
MVC Thymeleaf
    ↓
ApplicationCrmService (transactions métier)
    ↓
Company / Opportunity / Application / StatusHistory
    ↓
MySQL via Spring Data JPA
    ↓ après commit
ApplicationProjectionListener
    ↓
GoogleSheetsProjectionPort
    ↓
Google Sheets (projection uniquement)
```

`ExternalProjectionEntity` conserve l’état technique sans polluer `ApplicationEntity`. L’import Excel
réutilise les règles de création du service applicatif après une prévisualisation temporaire. Les classes
du domaine et les repositories n’importent aucune classe de l’API Google.

## Flux principal

```text
Navigateur / Thymeleaf
        ↓
Contrôleurs Spring MVC
        ↓
CareerWorkspaceService (cas d’usage et transactions)
        ├── extraction PDF / résolution d’offre / export PDF
        ├── adaptateurs Spring AI
        └── repositories Spring Data
                    ↓
                 MySQL
              migrations Flyway
```

## Modèle persistant

```text
Opportunity
    ├── ResumeAnalysisRecord
    │       └── ResumeDocument
    │               └── ResumeVersion (1..n)
    └── CoverLetter
            ├── analysis (facultatif)
            └── resumeVersion (facultatif)
```

`ResumeAnalysis` et `GeneratedResume` restent des contrats structurés de l’adaptateur IA, sans annotation
JPA. `CareerWorkspaceService` les mappe vers les entités persistantes et vers des view models immuables.
Les templates ne manipulent donc pas directement les entités.

## Cohérence transactionnelle

Une analyse sauvegarde l’opportunité, l’analyse et la première version du CV dans une même transaction.
Une sauvegarde manuelle ajoute une version : elle ne remplace jamais la version précédente. Les appels IA
restent synchrones ; une file de travaux et des statuts d’échec pourront être introduits ultérieurement.

## Suppression

La suppression d’une analyse supprime explicitement son document et ses versions. Une opportunité portant
encore une analyse ou une lettre ne peut pas être supprimée : l’utilisateur doit supprimer ces ressources
séparément. Ce choix évite les cascades silencieuses.

## Préparation du futur

Les entités n’intègrent pas encore de compte utilisateur. Leur identité indépendante permet d’ajouter plus
tard une relation vers un utilisateur ou un workspace sans transformer les contrats IA.
