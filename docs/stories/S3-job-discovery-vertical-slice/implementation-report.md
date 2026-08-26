# S3 — Rapport d'implémentation

## Livré

| Élément | Détail |
|---|---|
| Domaine | `JobOffer`, `JobOfferCompetency`, `ContractType` (étendu de `career.domain`), `EligibilityEvaluator`, `EligibilityResult`, `EligibilityReason`, `EligibilityReasonType`, `EligibilityStatus` |
| Port | `JobOfferProvider` (interface), `JobOfferSearchRequest`, `JobOfferSearchResult` |
| Adaptateur FT | `FranceTravailProperties`, `FranceTravailConfiguration`, `FranceTravailTokenClient` (OAuth2 + cache mémoire 60s), `FranceTravailApiClient`, `FranceTravailOfferDto` (6 DTOs imbriqués), `FranceTravailOfferMapper`, `FranceTravailJobOfferProvider`, `DisabledFranceTravailJobOfferProvider`, `FranceTravailAuthenticationException` |
| Cas d'usage | `DiscoverJobOffers` (orchestration), `DiscoveryResult`, `EligibleOffer` |
| Web | `JobDiscoveryController` (2 routes), `JobDiscoveryViewModels`, `job-discovery.html`, `job-discovery-results.html` |
| Config | `france-travail.*` dans `application.properties` (disabled par défaut) |
| ContractType | `fromCode()` ajouté ; unifié sur `career.domain.ContractType` |

## Commits (`story/job-discovery-vertical-slice`)

1. `fa30950` (feat) story S3: discovery vertical slice — 34 fichiers, 2 794 insertions.

## Validation technique

- Compilation : `./mvnw compile test-compile` — **0 erreur**.
- Tests unitaires : `EligibilityEvaluatorTest` (10 scénarios), `FranceTravailOfferMapperTest` (11 scénarios) — **tous passent**.
- Tests existants : aucun échec supplémentaire par rapport à `main` (les 16 erreurs sont des tests d'intégration persistance déjà en échec avant S3).
- Application démarre : `ApplicationStartedEvent` en mode `france-travail.enabled=false` (pas de credentials).

## Bugs trouvés et corrigés

1. **Regex salaire** : le pattern supposait `Annuel de X [à Y ]Euros sur Z mois` alors que le vrai format France Travail est `Annuel de X Euros à Y Euros sur Z mois` — "Euros" apparaît après les deux montants. Corrigé dans `FranceTravailOfferMapper`.
2. **ContractType dupliqué** : S3 avait créé `discovery.domain.ContractType` avec `getLabel()`/`fromProviderCode()` alors que `career.domain.ContractType` existait déjà. Corrigé par suppression du doublon et ajout de `fromCode()` sur le type existant.
3. **EligibilityEvaluatorTest** : références à `search.domain.ContractType` remplacées par `career.domain.ContractType`.

## Tests

21 nouveaux tests unitaires (10 EligibilityEvaluator + 11 FranceTravailOfferMapper). Aucun test d'intégration ajouté (hors scope S3).
