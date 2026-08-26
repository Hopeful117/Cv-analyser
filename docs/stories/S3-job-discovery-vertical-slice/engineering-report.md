# S3 — Engineering report

## Story Summary

Le premier slice vertical réel de découverte d'emplois est opérationnel : une recherche textuelle + localisation est envoyée à l'API France Travail, les résultats sont mappés en `JobOffer` canoniques, chaque offre est évaluée de manière déterministe contre les préférences utilisateur (contrat, modes, technologies, salaire) avec un statut tricolore ELIGIBLE/INELIGIBLE/REVIEW_REQUIRED et des raisons explicites, et les résultats sont affichés dans une UI existante. Le tout fonctionne en bout-en-bout avec de vraies credentials de production, sans persistance, sans IA, sans scheduler.

## Domain Decisions

- **JobOffer** : record immutable, 20+ champs, tous valorisés depuis le mapping FT ; les champs FT spécifiques (romeCode, dureeTravailLibelle, etc.) sont préservés pour le futur sans polluer l'évaluation ;
- **EligibilityEvaluator** : 4 dimensions évaluées (contrat, modes, technologies, salaire) ; priorité INELIGIBLE > REVIEW_REQUIRED > ELIGIBLE ; chaque rejet produit une raison typée et un message FR ;
- **ContractType** : vocabulaire commun assumé (`career.domain.ContractType`) plutôt que duplication, étendu de `fromCode()` pour l'adaptateur FT ;
- **WorkMode/SalaryPeriod** : réutilisés depuis `search.domain` — pas de duplication.

## Provider Adapter Pattern

Le port `JobOfferProvider` définit un contrat minimal (search) que tout fournisseur doit implémenter. L'adaptateur FT comprend :
- `FranceTravailTokenClient` : OAuth2 client_credentials avec cache TTL et retry 401 ;
- `FranceTravailApiClient` : recherche paginée (max 150/call), Content-Range parsing ;
- `FranceTravailOfferMapper` : mapping complet FT→domaine, regex salaire validée sur le vrai format ;
- `DisabledFranceTravailJobOfferProvider` : fallback gracieux si credentials absents.

## Eligibility Semantics

- **CONTRACT_NOT_ACCEPTED** : type contrat hors ensemble accepté (non vide) ;
- **WORK_MODE_NOT_ACCEPTED** : mode hors ensemble accepté (non vide) ;
- **TECHNOLOGY_EXCLUDED** : technologie exclue présente dans l'offre ;
- **TECHNOLOGY_PREFERRED_FOUND** : technologie préférée présente — preuve positive uniquement, sans effet sur l'éligibilité formelle ;
- **PREFERRED_TECHNOLOGY_MISSING** : technologie préférée absente — AUCUN EFFET sur l'éligibilité (ne produit ni INELIGIBLE ni REVIEW_REQUIRED) ;
- **SALARY_BELOW_MINIMUM** : salaire déterministicement connu et comparé inférieur au minimum — INELIGIBLE ;
- **SALARY_UNKNOWN** : salaire non disponible — REVIEW_REQUIRED uniquement si le salaire minimum est une contrainte active ;
- **PREFERRED_TECHNOLOGY_FOUND** : technologie préférée détectée — preuve positive uniquement ;
- Priorité : INELIGIBLE > REVIEW_REQUIRED > ELIGIBLE.

Any explicit hard violation
→ INELIGIBLE

else any unresolved ACTIVE HARD constraint
→ REVIEW_REQUIRED

else
→ ELIGIBLE.

Therefore:

salary below minimum + unknown work mode → INELIGIBLE (not REVIEW_REQUIRED).

preferred technology missing + otherwise fully compatible offer → ELIGIBLE.

excluded technology found → INELIGIBLE.

java preference/exclusion does not accidentally match javascript.

## Provider Adapter Pattern

Le port `JobOfferProvider` définit un contrat minimal (search) que tout fournisseur doit implémenter. L'adaptateur FT comprend :
- `FranceTravailTokenClient` : OAuth2 client_credentials avec cache TTL et retry 401 ;
- `FranceTravailApiClient` : recherche `range=0-49`, un seul appel par recherche utilisateur, max 50 offres ;
- `FranceTravailOfferMapper` : mapping complet FT→domaine, regex salaire validée sur le vrai format ;
- `DisabledFranceTravailJobOfferProvider` : fallback gracieux si credentials absents.

## Quality Pipeline

- `./mvnw compile test-compile` : **0 erreur** ;
- Tests unitaires S3 : **34/34 passent** (EligibilityEvaluatorTest + FranceTravailOfferMapperTest + JobDiscoveryViewModelsTest) ;
- Tests existants : 0 régression (16 erreurs pré-existantes = tests intégration persistance sans BDD) ;
- `git diff --check` : propre sur la branche.

## Known Limitations

- Pas de persistance des résultats (scope S3 = spike) ;
- Pas de gestion rate limit / bulk ;
- Description provider : échappée par défaut via `th:text` + `stripHTML` dans le view model ;
- Pas de test d'intégration avec vrai FT (validé manuellement au curl) ;
- Rendu navigateur/mobile non vérifié visuellement (Chromium absent) ;
- La description HTML des offres est nettoyée (tags HTML retirés) avant affichage échappé.

## Suggested Next Story

S4 — « Persistance des JobOffer + diff incrémental + éligibilité réévaluée à la mise à jour des préférences » ou S5 — « UI résultat enrichie : tri, filtrage, sauvegarde d'offres, détail offre ».
