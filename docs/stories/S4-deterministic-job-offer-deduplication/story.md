# Story S4 — Deterministic Job Offer Deduplication

## Objectif

Dédupliquer de manière déterministe, conservatrice et explicable les `JobOffer` transitoires présentes dans un
ensemble de candidats acquis. La priorité est de préserver les faux négatifs plutôt que de fusionner deux offres
d'emploi distinctes : conserver deux copies est préférable à supprimer une opportunité réelle.

Cette Story fait suite à la stabilisation de la normalisation et de la provenance dans le commit `f68ae80`
(`feat(job-search): normalize job offers and preserve provenance`). Elle réutilise l'identité existante exposée
par `JobOffer.providerIdentity()`.

## Question produit

Comment retirer uniquement les copies certaines d'une même offre avant son évaluation, sans transformer des offres
similaires provenant de sources différentes en une seule offre canonique ?

## Références architecturales

- [Architecture d'acquisition](../../adr/job-offer-acquisition-architecture.md)
- [Normalisation et provenance des JobOffer](../../adr/job-offer-normalization-and-provenance.md)
- [Architecture Job Search Acquisition](../../architecture/job-search-acquisition.md)
- `JobOffer`, `JobOfferIdentity` et `JobOffer.providerIdentity()` dans le modèle courant

## Portée

### IN SCOPE

- Définition explicite des classifications `EXACT_DUPLICATE`, `POSSIBLE_DUPLICATE`, `DISTINCT` et
  `INSUFFICIENT_EVIDENCE`, ou d'un modèle équivalent plus petit conservant ces sémantiques.
- Déduplication automatique des identités provider-locales strictement identiques :
  `providerKey + providerOfferId`.
- Réutilisation de `JobOfferIdentity`, sans concept concurrent d'identité provider.
- Préservation des offres lorsque l'identité est absente, incomplète ou issue d'un autre provider.
- Analyse des éventuels signaux cross-source actuellement représentés par le modèle avant toute utilisation
  automatique.
- Composant minimal de déduplication, indépendant des providers, sans framework générique.
- Intégration dans le flux d'agrégation France Travail + Adzuna avant Eligibility et Matching, lorsque ce flux
  l'autorise.
- Sélection déterministe d'un représentant existant, sans fusion synthétique de champs.
- Tests unitaires et tests d'intégration du flux de découverte concernés.
- Création d'un ADR dédié à la déduplication déterministe pendant l'implémentation.

### OUT OF SCOPE

- IA, LLM, embeddings, recherche sémantique ou matching probabiliste.
- Levenshtein, Jaro-Winkler, TF-IDF, cosine similarity ou toute autre comparaison fuzzy.
- Similarité de titres, descriptions, dates, entreprise ou localisation comme identité automatique.
- Résolution d'entité entreprise, suppression agressive de suffixes ou inférence de société.
- Géocodage, distance géographique, alias de villes ou inférence de région/pays.
- Déduction de la provenance originale depuis un hostname ou résolution de redirection HTTP.
- Suppression aveugle de paramètres d'URL ou déduplication URL sans sémantique démontrée.
- Nouveau provider, orchestrateur global d'acquisition ou scheduler/passive monitoring.
- Persistence de `JobOffer`, historique de déduplication, fingerprint persistent ou migration Flyway.
- Déduplication entre workflows Greenhouse et France Travail/Adzuna nécessitant persistence ou orchestration.
- Déduplication d'`Opportunity` ou d'`Application`.
- Modification de `EligibilityEvaluator` ou `JobMatchingEngine` pour y incorporer la déduplication.

## Règles métier et invariants

### Identité provider-local

```text
JobOfferIdentity(providerKey, providerOfferId)
```

Deux offres sont `EXACT_DUPLICATE` si et seulement si leurs deux identités provider-locales sont présentes et
strictement égales.

```text
ADZUNA + 123       = ADZUNA + 123       -> doublon exact
ADZUNA + 123       != GREENHOUSE + 123  -> non prouvé comme doublon
ADZUNA + 123       != ADZUNA + 456      -> distinct
```

Une identité absente ou incomplète ne doit jamais égaler une autre identité absente ou incomplète. `UNKNOWN +
UNKNOWN` ne constitue pas une identité partagée.

### Cross-source

La Story doit inspecter les signaux réellement disponibles dans le modèle courant : identité originale
explicitement vérifiée, URL originale canonique fiable ou URL de candidature explicitement fiable. Aucun signal ne
doit être inventé depuis un provider, une redirection ou une ressemblance textuelle.

Il est acceptable que l'implémentation ne permette aucune déduplication automatique entre providers différents si
la provenance actuelle ne fournit pas d'identité cross-source suffisamment forte.

### Preuves faibles

Les éléments suivants peuvent au mieux produire une classification informative, mais ne peuvent jamais déclencher
une suppression automatique :

- même titre ;
- même entreprise ;
- même localisation ;
- même entreprise + titre ;
- même entreprise + titre + localisation ;
- description ou dates similaires.

### URLs

Les URLs doivent d'abord être classifiées par sémantique. Une URL Adzuna, France Travail, Greenhouse ou Manual
Intake ne doit pas être considérée équivalente aux autres par défaut. Aucune redirection réseau ne doit être suivie.
La normalisation éventuelle est limitée aux transformations démontrées comme sémantiquement neutres, sans supprimer
aveuglément les paramètres de requête.

### Représentant

Lorsqu'un doublon exact est confirmé, un seul `JobOffer` existant est conservé selon une règle stable et
explicable. Aucun champ n'est fusionné entre les copies et aucune nouvelle offre synthétique n'est construite.
Si aucune priorité sémantique de source n'est établie, l'ordre d'acquisition stable est utilisé et documenté.

## Flux cible

```text
France Travail ─┐
                ├── JobOffer candidates
Adzuna ─────────┘
                       ↓
                 deduplication
                       ↓
                   Eligibility
                       ↓
                    Matching
```

Greenhouse conserve son workflow ATS direct. La capacité de déduplication doit rester distincte d'un orchestrateur
global et ne doit pas forcer Greenhouse dans `DiscoverJobOffers`.

`JobOffer` reste transient. La déduplication porte uniquement sur l'ensemble de candidats courant ; elle ne mémorise
pas qu'une offre a été vue précédemment.

## Critères d'acceptation

- [ ] `JobOfferIdentity` et `JobOffer.providerIdentity()` sont réutilisés comme identité provider-local.
- [ ] Même provider + même `providerOfferId` est dédupliqué de manière sûre.
- [ ] Providers différents et identifiants textuellement identiques restent distincts.
- [ ] Même provider + identifiants différents restent distincts.
- [ ] Les identités absentes ou incomplètes ne provoquent jamais de fusion.
- [ ] Les preuves textuelles faibles ne déclenchent jamais de fusion automatique.
- [ ] Entreprise + titre + localisation est insuffisant pour fusionner automatiquement.
- [ ] Une identité URL n'est utilisée automatiquement que si sa sémantique fiable est démontrée ; sinon elle est
      différée.
- [ ] La sélection du représentant est déterministe et documentée.
- [ ] Aucun `JobOffer` synthétique n'est produit par fusion de champs.
- [ ] La déduplication intervient avant Eligibility/Matching dans le flux applicable.
- [ ] `EligibilityEvaluator` ne contient aucune logique de déduplication.
- [ ] `JobMatchingEngine` ne contient aucune logique de déduplication.
- [ ] `JobOffer` reste transient et aucune migration/persistence n'est introduite.
- [ ] `Opportunity` et `Application` ne sont pas dédupliquées ni modifiées par cette Story.
- [ ] Aucun fuzzy matching, IA, embedding ou similarité sémantique n'est introduit.
- [ ] France Travail, Adzuna, Greenhouse et Manual Intake restent fonctionnels.
- [ ] Un ADR dédié à la déduplication est créé pendant l'implémentation.
- [ ] La suite de tests complète reste verte.

## Tests attendus

- Même provider + même ID : une seule offre survivante.
- Même provider + IDs différents : deux offres conservées.
- Providers différents + même ID : deux offres conservées.
- Identités absentes/incomplètes : offres conservées séparément.
- Même entreprise, titre et localisation sans identité forte : offres conservées séparément.
- Sélection du représentant stable et explicable.
- Flux `DiscoverJobOffers` : déduplication avant les étapes d'évaluation pertinentes, tri et isolation des erreurs
  conservés.
- Régressions France Travail, Adzuna, Greenhouse, Manual Intake, Eligibility, Matching et sélection Opportunity.

## Gates

1. L'identité provider-local est réutilisée sans concept concurrent.
2. Les doublons exacts seuls sont automatiquement supprimés.
3. La sécurité de toute identité cross-source ou URL est démontrée par le modèle courant, sinon elle est différée.
4. Aucun stockage, orchestrateur global ou mécanisme fuzzy/IA n'est ajouté.
5. La suite de tests complète est verte.

## Decision gate attendu

L'implémentation doit rapporter explicitement :

```text
EXACT_DEDUPLICATION_READY       = YES / NO
CROSS_SOURCE_DEDUPLICATION_SAFE = YES / PARTIAL / NO
PERSISTENCE_REQUIRED_NOW        = YES / NO
```
