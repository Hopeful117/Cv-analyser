# S3 — Code review

Revue auteur sur l'intégralité du diff `origin/main..HEAD` (branche `story/job-discovery-vertical-slice`).

## Architecture

| Point | Verdict | Justification |
|---|---|---|
| Hexagonal respecté | OK | port (`JobOfferProvider`) → adaptateur (`FranceTravail*`) → domaine (`JobOffer`, `EligibilityEvaluator`) ; aucun import domaine→infrastructure |
| Couplage France Travail | OK | `FranceTravailProperties` isolé, `DisabledFranceTravailJobOfferProvider` fallback sans crash si credentials manquants |
| Auth OAuth2 | OK | token client en mémoire avec TTL 60s de sécurité, retry automatique sur 401, exception métier dédiée |
| Scope OAuth2 | OK | `application_{client_id}` requis en plus de `api_offresdemploiv2 o2dsoffre` — documenté dans l'investigation |
| Mapping provider→domaine | OK | `FranceTravailOfferMapper` est un adaptateur pur, sans logique métier ; le domaine ne connaît pas FT |
| Évaluateur déterministe | OK | 0 appel IA, 0 appel réseau ; pure function sur `JobOffer` + `JobSearchPreferencesEntity` |
| Contrôleurs minces | OK | routing/PRG uniquement ; la logique métier est dans `DiscoverJobOffers` et `EligibilityEvaluator` |

## Points de vigilance

| Risque | Statut | Commentaire |
|---|---|---|
| Token OAuth2 non persisté entre redémarrages | Accepté | S3 = spike ; la persistance de token (vault/env) est un sujet de production |
| Aucune gestion du rate limit France Travail | Accepté | max 150 résultats/call documenté ; pas de scheduler ni bulk dans S3 |
| `JobOffer.rawDescription()` en HTML brut | Corrigé | `stripHTML()` dans `JobDiscoveryViewModels` retire les tags HTML avant l'affichage échappé via `th:text` ; aucun `th:utext` n'est utilisé |

| Semantique technologie préférée | OK | `PREFERRED_TECHNOLOGY_MISSING` → aucun effet sur l'éligibilité ; `PREFERRED_TECHNOLOGY_FOUND` → preuve positive uniquement |
| `JobOffer.rawContractLabel()` = string FR | Accepté | déjà le cas pour toutes les strings FR du domaine |
| SalaryPeriod créé en double (`search.domain` / pas de `discovery.domain`) | Réutilisé | `SalaryPeriod` et `WorkMode` depuis `search.domain` — pas de duplication |
| Pas de test d'intégration avec vrai FT | Accepté | S3 = spike ; les tests unitaires couvrent le mapping et l'évaluation ; l'intégration réelle est manuelle |

## Décisions discutées

- **ContractType unifié** sur `career.domain` : rejet de la duplication, `fromCode()` ajouté pour l'adaptateur FT ;
- **EligibilityEvaluator statique** : pas de bean Spring, pure function testable sans contexte ;
- **`DisabledFranceTravailJobOfferProvider`** : retourne liste vide + log warning plutôt que de crasher ;
- **Templates séparés** (`job-discovery.html` pour la recherche, `job-discovery-results.html` pour les résultats) plutôt qu'un seul template conditionnel.
