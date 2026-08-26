# Story S3 — Repository Analysis

## Branche de travail

- Base : `main` (`15c83c4`)
- Branche : `story/job-discovery-vertical-slice`
- S1 merged : OUI (`c0b415b`)
- S2 merged : OUI (`15c83c4`)
- Visual correction : OUI (`36e1326`)

## Modules existants

### ProfessionalProfile (S1)

- **Package** : `com.hopeful117.cv_analyzer.profile`
- **Entité** : `ProfessionalProfileEntity` (table `career_professional_profile`)
- **Repository** : `ProfessionalProfileRepository` avec `findLocalProfile()`
- **Service** : `ProfessionalProfileService`
- **Domaine** : `EducationKind`, `SkillOrigin`
- **Enfants** : `ProfileSkillEntity`, `ProfileExperienceEntity`, `ProfileEducationEntity`, `ProfileLanguageEntity`

### JobSearchPreferences (S2)

- **Package** : `com.hopeful117.cv_analyzer.search`
- **Entité** : `JobSearchPreferencesEntity` (table `career_job_search_preferences`)
- **Repository** : `JobSearchPreferencesRepository` avec `findActivePreferences()`
- **Service** : `JobSearchPreferencesService`
- **Domaine** : `WorkMode`, `SalaryPeriod`, `TechnologyPreference`
- **Enfants** : `PreferenceRoleEntity`, `PreferenceLocationEntity`, `PreferenceTechnologyEntity`

### Career (référence provider)

- **Package** : `com.hopeful117.cv_analyzer.career`
- **Provider precedent** : `GoogleSheetsProjectionAdapter` / `DisabledGoogleSheetsProjectionAdapter`
- **Port** : `GoogleSheetsProjectionPort`, `GoogleSheetsConsultationPort`
- **Config** : `CareerGoogleSheetsProperties` (@ConfigurationProperties)
- **Pattern** : Conditional activation, adapter disabled par défaut

## Conventions à suivre

### Package

```
discovery/
  application/        # Use cases, ports
    port/             # Interfaces outbound
  domain/             # JobOffer, EligibilityResult, enums
  infrastructure/     # France Travail adapter
    francetravail/    # Tous les composants FT
  web/                # Form objects si nécessaire
```

### Configuration

- `@ConfigurationProperties` record avec compact constructor
- `@Configuration` + `@EnableConfigurationProperties`
- `@ConditionalOnProperty` pour activation
- Disabled adapter avec `matchIfMissing = true`

### Service

- `@Service` + `@RequiredArgsConstructor`
- Application service dans `application/`
- Domain logic pure dans `domain/`

### Controller

- `@Controller` (pas `@RestController`)
- Retourne noms templates Thymeleaf
- PRG pour mutations
- `activePage` pour navigation

### Tests

- Unit tests : Mockito, AssertJ, pas de Spring context
- MVC tests : `MockMvcBuilders.standaloneSetup(...)`
- Persistence tests : `@SpringBootTest` + H2

## Frontière fournisseur

```
JobOfferProvider (port applicatif)
        |
        v
FranceTravailJobOfferProvider (infrastructure)
        |
        +-- FranceTravailTokenClient
        +-- FranceTravailApiClient
        +-- FranceTravailOfferDto
        +-- FranceTravailOfferMapper
                         |
                         v
                 JobOffer canonique
                         |
                         v
             EligibilityEvaluator pur
```

## Modèle canonique JobOffer

```java
// domain/JobOffer.java
public record JobOffer(
    // Source
    String providerKey,
    String providerOfferId,
    String originUrl,
    Instant fetchedAt,
    // Core
    String title,
    String description,
    String company,
    // Role
    String romeCode,
    String romeLabel,
    String appellationLabel,
    // Location
    String locationLabel,
    String communeCode,
    String postalCode,
    BigDecimal latitude,
    BigDecimal longitude,
    // Contract
    ContractType canonicalContractType,
    String rawContractCode,
    String rawContractLabel,
    // Requirements
    List<JobOfferCompetency> competencies,
    String experienceLabel,
    // Work conditions
    WorkMode workMode,
    String workDurationLabel,
    // Salary
    String rawSalaryText,
    BigDecimal salaryMinAmount,
    BigDecimal salaryMaxAmount,
    SalaryPeriod salaryPeriod,
    // Dates
    Instant providerCreatedAt,
    Instant providerUpdatedAt
) {}
```

## Éligibilité tri-state

```java
// domain/EligibilityStatus.java
public enum EligibilityStatus {
    ELIGIBLE,
    INELIGIBLE,
    REVIEW_REQUIRED
}

// domain/EligibilityResult.java
public record EligibilityResult(
    EligibilityStatus status,
    List<EligibilityReason> reasons
) {}

// domain/EligibilityReason.java
public record EligibilityReason(
    EligibilityReasonType type,
    String message
) {}

// domain/EligibilityReasonType.java
public enum EligibilityReasonType {
    CONTRACT_NOT_ACCEPTED,
    CONTRACT_UNKNOWN,
    LOCATION_NOT_ACCEPTED,
    LOCATION_UNKNOWN,
    WORK_MODE_UNKNOWN,
    WORK_MODE_NOT_ACCEPTED,
    EXCLUDED_TECHNOLOGY_FOUND,
    SALARY_BELOW_MINIMUM,
    SALARY_UNKNOWN,
    PREFERRED_TECHNOLOGY_FOUND
}
```
