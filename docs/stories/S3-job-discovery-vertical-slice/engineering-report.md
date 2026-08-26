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
- **TECHNOLOGY_PREFERRED_MISSING** : technologie préférée absente (REVIEW_REQUIRED) ;
- **SALARY_BELOW_MINIMUM** : salaire affiché sous le minimum (REVIEW_REQUIRED) ;
- Priorité : toute INELIGIBLE → INELIGIBLE globale ; sinon tout REVIEW_REQUIRED → REVIEW_REQUIRED ; sinon → ELIGIBLE.

## Real API Findings

- Auth : scope requis = `api_offresdemploiv2 o2dsoffre application_{client_id}` (sans le dernier, `invalid_client`) ;
- Résultats : `Content-Range: offres 0-49/424` ; pagination `range` param (max 150/call) ;
- Salaires : format réel `Annuel de 65000.0 Euros sur 12.0 mois` (single) ou `Annuel de 40000.0 Euros à 50000.0 Euros sur 12.0 mois` (range) ;
- `teletravail` : champ existe souvent à null ; pas de filtre remote structuré côté FT ;
- `competences[].exigence` : "S" (Souhaitée) / "N" (Non souhaitée) / "R" (Obligatoire).

## Application Layer

`DiscoverJobOffers` orchestre : provider.search → mapper (interne à l'adaptateur) → EligibilityEvaluator → filtrage + tri. Le contrôleur ne fait que routing et assemblage des viewModels.

## UX

Deux écrans dans le design existant : formulaire de recherche (query + ville + départements) + résultats avec badges colorés (ELIGIBLE = vert, INELIGIBLE = rouge, REVIEW_REQUIRED = orange). La sidebar affiche « Offres trouvées ». Templates responsive existants.

## Tests

21 nouveaux tests unitaires :
- `EligibilityEvaluatorTest` : 10 scénarios (CDI rejeté, remote obligatoire, techno exclue, techno préférée absente, salaire sous minimum, tous ELIGIBLE, priorité INELIGIBLE, REVIEW_REQUIRED sans INELIGIBLE, pas de préférences) ;
- `FranceTravailOfferMapperTest` : 11 scénarios (mapping complet, salaire simple/range/absent, compétences, date, expériences, localisation, contrat INTERIM/AUTRE).

## Quality Pipeline

- `./mvnw compile test-compile` : **0 erreur** ;
- Tests unitaires S3 : **21/21 passent** ;
- Tests existants : 0 régression (16 erreurs pré-existantes = tests intégration persistance sans BDD) ;
- `git log --oneline` : branche propre, 1 commit logique.

## Known Limitations

- Pas de persistance des résultats (scope S3 = spike) ;
- Pas de gestion rate limit / bulk ;
- `rawDescription()` en HTML brut — nettoyage nécessaire pour affichage produit ;
- Pas de test d'intégration avec vrai FT (validé manuellement au curl) ;
- Rendu navigateur/mobile non vérifié visuellement (Chromium absent) ;
- La description HTML des offres est affichée telle quelle via `| raw` dans le template.

## Suggested Next Story

S4 — « Persistance des JobOffer + diff incrémental + éligibilité réévaluée à la mise à jour des préférences » ou S5 — « UI résultat enrichie : tri, filtrage, sauvegarde d'offres, détail offre ».
