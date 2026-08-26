# Story S3 — Implementation Plan

## Architecture cible

```
discovery/
  application/
    DiscoverJobOffers.java           # Use case orchestration
    port/
      JobOfferProvider.java          # Port applicatif
      JobOfferSearchRequest.java     # Requête neutre
      JobOfferSearchResult.java      # Résultat avec métadonnées
  domain/
    JobOffer.java                    # Canonique non JPA
    JobOfferCompetency.java          # Compétence offer
    ContractType.java                # Enum canonique
    WorkMode.java                    # Réutiliser search.domain
    SalaryPeriod.java                # Réutiliser search.domain
    EligibilityStatus.java           # Tri-state
    EligibilityResult.java           # Résultat + reasons
    EligibilityReason.java           # Raison typée
    EligibilityReasonType.java       # Types de raisons
    EligibilityEvaluator.java        # Logique pure
  infrastructure/
    francetravail/
      FranceTravailProperties.java  # @ConfigurationProperties
      FranceTravailConfiguration.java # @Configuration
      FranceTravailTokenClient.java # OAuth2 token
      FranceTravailApiClient.java   # HTTP search
      FranceTravailOfferDto.java    # DTO réponse
      FranceTravailOfferMapper.java # DTO → canonique
      FranceTravailJobOfferProvider.java # Adaptateur
      DisabledFranceTravailJobOfferProvider.java # No-op
  web/
    JobDiscoveryController.java      # MVC controller
    JobDiscoveryViewModels.java      # View records
src/main/resources/templates/
  job-discovery.html                 # Page résultats
src/test/java/.../discovery/
  application/
    DiscoverJobOffersTest.java
  domain/
    EligibilityEvaluatorTest.java
    JobOfferTest.java
  infrastructure/
    francetravail/
      FranceTravailTokenClientTest.java
      FranceTravailApiClientTest.java
      FranceTravailOfferMapperTest.java
  web/
    JobDiscoveryControllerTest.java
```

## Étapes d'implémentation

### Phase 1 — Domaine canonique

1. Créer `ContractType` enum dans `discovery/domain/`
2. Créer `JobOffer` record dans `discovery/domain/`
3. Créer `JobOfferCompetency` record dans `discovery/domain/`
4. Créer `EligibilityStatus`, `EligibilityResult`, `EligibilityReason`, `EligibilityReasonType` dans `discovery/domain/`
5. Créer `EligibilityEvaluator` classe pure dans `discovery/domain/`

### Phase 2 — Port applicatif

6. Créer `JobOfferProvider` interface dans `discovery/application/port/`
7. Créer `JobOfferSearchRequest` record dans `discovery/application/port/`
8. Créer `JobOfferSearchResult` record dans `discovery/application/port/`

### Phase 3 — Adaptateur France Travail

9. Créer `FranceTravailProperties` record dans `discovery/infrastructure/francetravail/`
10. Créer `FranceTravailConfiguration` dans `discovery/infrastructure/francetravail/`
11. Créer `FranceTravailTokenClient` dans `discovery/infrastructure/francetravail/`
12. Créer `FranceTravailApiClient` dans `discovery/infrastructure/francetravail/`
13. Créer `FranceTravailOfferDto` records dans `discovery/infrastructure/francetravail/`
14. Créer `FranceTravailOfferMapper` dans `discovery/infrastructure/francetravail/`
15. Créer `FranceTravailJobOfferProvider` dans `discovery/infrastructure/francetravail/`
16. Créer `DisabledFranceTravailJobOfferProvider` dans `discovery/infrastructure/francetravail/`

### Phase 4 — Use case

17. Créer `DiscoverJobOffers` dans `discovery/application/`

### Phase 5 — Web

18. Créer `JobDiscoveryViewModels` dans `discovery/web/`
19. Créer `JobDiscoveryController` dans `WebInterfaceController/`
20. Créer template `job-discovery.html`

### Phase 6 — Configuration

21. Ajouter propriétés dans `application.properties`
22. Vérifier démarrage sans credentials

### Phase 7 — Tests

23. Tests unitaires EligibilityEvaluator
24. Tests unitaires FranceTravailOfferMapper
25. Tests unitaires FranceTravailTokenClient
26. Tests unitaires FranceTravailApiClient
27. Tests unitaires DiscoverJobOffers
28. Tests MVC JobDiscoveryController
29. Tests intégration démarrage

### Phase 8 — Validation

30. Test manuel avec API réelle
31. Rapports de clôture

## mapping ContractType

| France Travail | CV Analyzer |
|----------------|-------------|
| CDI | CDI |
| CDD | CDD |
| INTERIM | (non testé) |
| Autre | null → REVIEW_REQUIRED |

## mapping WorkMode

| France Travail | CV Analyzer |
|----------------|-------------|
| teletravail=true | REMOTE |
| teletravail=null | UNKNOWN |
| (pas de champ hybride) | UNKNOWN |

## Salaire parsing

Pattern confirmé : `"Annuel de X à Y Euros sur Z mois"`

- Min/Max : `BigDecimal`
- Période : `ANNUAL` si mois=12, `MONTHLY` si mois=1
- Devise : `EUR` (toujours Euros dans les fixtures)
- Échec parsing : `salaryMinAmount=null`, `rawSalaryText` préservé

## Évaluation éligibilité

### Ordre de priorité

1. **CONTRACT_NOT_ACCEPTED** → INELIGIBLE
2. **CONTRACT_UNKNOWN** → REVIEW_REQUIRED
3. **LOCATION_NOT_ACCEPTED** (si openToRelocation=false) → INELIGIBLE
4. **LOCATION_UNKNOWN** (si openToRelocation=false) → REVIEW_REQUIRED
5. **WORK_MODE_NOT_ACCEPTED** → INELIGIBLE
6. **WORK_MODE_UNKNOWN** → REVIEW_REQUIRED
7. **EXCLUDED_TECHNOLOGY_FOUND** → INELIGIBLE
8. **SALARY_BELOW_MINIMUM** → INELIGIBLE
9. **SALARY_UNKNOWN** (si salaire min actif) → REVIEW_REQUIRED
10. Sinon → ELIGIBLE

### Preuves positives (sans rejet)

- PREFERRED_TECHNOLOGY_FOUND
- Skills profile correspondantes
- Langues profile correspondantes
