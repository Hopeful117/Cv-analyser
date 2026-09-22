# ADR — Job Offer Acquisition Architecture

## Statut

Accepted

## Contexte

ES-006 à ES-013 ont terminé la phase `SOURCE_DISCOVERY`. Les investigations ont couvert les familles
institutional, aggregator, alternance, ATS direct, tech spécialisé, freelance, Europe, startup, remote,
manual intake et recruiter prospect.

Les sources observées ne partagent pas le même mécanisme d'acquisition :

```text
search global       -> France Travail, Adzuna, Jooble
board ciblé         -> Greenhouse, Lever
saisie humaine      -> Manual Intake, sources sans accès autorisé
provenance fédérée  -> EURES, agrégateurs, marketplaces
prospection         -> recruteurs sans offre publiée
```

Le repository possède aujourd'hui un port étroit `JobOfferProvider` adapté à une recherche France Travail,
un `JobOffer` canonique transient et une saisie manuelle qui produit le même type. `Opportunity` et
`Application` sont des objets durables appartenant au workflow utilisateur ; une découverte ne doit pas les
créer automatiquement.

## Décision

L'acquisition est une frontière applicative/infrastructure distincte du domaine métier :

```text
Sources externes
    -> acquisition adapter/capability
    -> JobOffer canonique transient
    -> normalisation inter-sources
    -> enrichment déterministe
    -> Eligibility + Matching
    -> sélection explicite
    -> Opportunity / Application
```

Le domaine `Eligibility`, `Matching`, `Opportunity` et `Application` ne dépend d'aucun provider concret.

### Port et capacités

`JobOfferProvider` est conservé comme port de recherche d'offres à la demande. Il ne doit pas être renommé
ou élargi uniquement pour anticiper les autres mécanismes.

- Une source searchable globale peut implémenter ce port.
- Un futur connector ATS aura une capacité de board/company ciblé distincte si ses paramètres et son
  opération diffèrent réellement de `search(targetRole, maxResults)`.
- `ManualJobOfferForm` n'est pas un provider : c'est une entrée humaine qui construit un `JobOffer`.
- Une future importation par URL sera une capacité d'acquisition autorisée séparée ; elle ne justifie pas
  aujourd'hui un scraper ou un nouveau port.
- La composition multi-provider et un orchestrateur ne sont introduits qu'avec le premier second provider
  automatisé réellement implémenté.

Cette décision évite à la fois le couplage du domaine aux APIs et une abstraction générale non prouvée.

### Portfolio

Le portfolio distingue valeur produit, mode d'acquisition et état de portefeuille. Les états sont une
classification architecturale, pas un enum de production à ce stade.

```text
ACTIVE_AUTOMATED      source opérationnelle automatisée
READY_FOR_INTEGRATION source validée mais pas encore implémentée
MANUAL_SUPPORTED      valeur conservée via Manual Intake
DEFERRED              utile mais non prioritaire ou preuve incomplète
PARTNERSHIP_REQUIRED  accès/usage nécessitant un accord
REJECTED              valeur insuffisante ou incompatibilité établie
```

Portfolio retenu :

| Source | Catégorie | Valeur produit | Acquisition | État | Décision |
|---|---|---|---|---|---|
| France Travail | Institutional | France, données structurées, source officielle | Search API | `ACTIVE_AUTOMATED` | conserver en production |
| Manual Intake | Universal | fallback pour toute offre externe | Manual | `MANUAL_SUPPORTED` | conserver en production |
| Adzuna | Aggregator | couverture généraliste et Java/backend complémentaire | Search API | `READY_FOR_INTEGRATION` | première intégration future |
| Greenhouse | ATS direct | provenance entreprise, descriptions riches, IDs stables | Board/company connector | `READY_FOR_INTEGRATION` | deuxième intégration future |
| Lever | ATS direct | modèle ATS intéressant | Board/company connector | `DEFERRED` | résoudre la découverte des tenants actifs |
| Jooble | Aggregator | volume généraliste | Search API | `DEFERRED` | quota et duplication défavorables |
| La bonne alternance | Alternance | contrat et diplôme alternance | Search/API à confirmer | `DEFERRED` | overlap France Travail important |
| Free-Work | Tech/Freelance | IT français, TJM, mission, technologies | Manual | `MANUAL_SUPPORTED` | conserver sans automatisation |
| WeLoveDevs | Tech | développeur, junior, technologies, salaire | Manual | `MANUAL_SUPPORTED` | conserver sans automatisation |
| EURES | Europe | mobilité et couverture européenne | Manual | `MANUAL_SUPPORTED` | conserver sans automatisation |
| Wellfound | Startup | stage, taille, equity, remote, timezone | Manual | `MANUAL_SUPPORTED` | conserver sans agrégation automatisée |
| Remote.com Jobs | Remote | pays/régions/timezones/sponsorship | Manual | `MANUAL_SUPPORTED` | conserver sans agrégation automatisée |

`PARTNERSHIP_REQUIRED` reste disponible pour une source dont la valeur est démontrée mais dont l'accès
automatisé ou la réutilisation commerciale nécessite un accord explicite. Aucune source actuelle n'est
placée dans cet état par défaut : Wellfound et Remote.com restent manuelles selon les preuves disponibles.

### ATS et Target Company

Greenhouse et Lever ne sont pas des moteurs de recherche globaux. Ils sont des mécanismes d'accès à des
boards publics d'entreprises connues ou découvertes séparément.

```text
Target acquisition configuration
    -> ATS identification
    -> company board
    -> board offers
    -> JobOffer
```

Une future configuration d'acquisition pourra distinguer une entreprise ciblée, son ATS, son board et son
identifiant externe. Elle ne doit pas surcharger `Company`, qui reste une entité métier durable liée aux
`Opportunity`. Aucun concept `TargetCompany` ou `CompanyJobBoard` n'est créé dans ES-014 : son besoin est
architecturalement établi par Greenhouse, mais sa forme et son ownership seront décidés lors de la Story
d'implémentation ATS.

### Acquisition, normalisation et enrichment

Les responsabilités sont séparées ainsi :

#### Acquisition

- authentification et limites du provider ;
- paramètres de recherche ou de board ;
- pagination et erreurs externes ;
- DTO et payloads provider ;
- identité externe et URLs observées ;
- conversion technique vers `JobOffer` ;
- conservation des valeurs brutes utiles.

#### Normalisation

- conversion de types techniques vers des représentations communes ;
- dates vers `Instant` ou représentation équivalente ;
- codes de contrat connus vers `ContractType` ;
- montants/périodes lorsque le format est strictement comparable ;
- lieu et devise lorsque la sémantique est établie.

Une normalisation locale est acceptable dans un adapter lorsqu'elle traduit directement un contrat
provider connu. Elle ne doit pas inventer une valeur canonique lorsqu'une donnée est inconnue.

#### Enrichment

- extraction de technologies et compétences depuis du texte ;
- séniorité et expérience ;
- remote scope, pays autorisés, timezone, sponsorship et relocation ;
- mécanisme d'emploi et voyage ;
- equity, stage, taille et industrie startup ;
- TJM, durée et début de mission ;
- langue, classification européenne et relation client/intermédiaire.

L'enrichment reste dérivé et traçable. Aucun LLM, embedding, RAG ou agent n'est introduit par cet ADR.

### Provenance

`providerKey + providerOfferId` reste l'identité logique immédiate de `JobOffer`, conformément à l'ADR du
modèle de domaine. L'architecture doit cependant distinguer, lorsque l'information est réellement connue :

```text
acquisitionSource  = mécanisme qui a fourni l'offre à Developer OS
originalSource     = source qui a publié l'offre
externalOfferId    = identifiant dans l'espace de la source connue
originalUrl        = URL de l'offre originale si connue
applyUrl           = destination de candidature si distincte
company            = entreprise annoncée
intermediary       = recruteur/agrégateur/intermédiaire si explicitement connu
```

Les valeurs inconnues restent `UNKNOWN`. L'architecture n'infère pas Greenhouse depuis la similarité d'un
texte, ni France Travail depuis une présence EURES.

Exemples valides uniquement si la preuve existe :

```text
acquisitionSource = WELLFOUND
originalSource    = GREENHOUSE
```

```text
acquisitionSource = EURES
originalSource    = UNKNOWN
```

### Déduplication

La déduplication n'appartient pas à `Eligibility` ou `Matching`.

Elle sera organisée en étapes déterministes :

1. Déduplication exacte dans un résultat provider par `(provider, externalOfferId)`.
2. Déduplication inter-sources après acquisition et normalisation minimale.
3. Revue explicite des doublons probables ; aucune fusion fuzzy ou IA automatique.

Identités :

| Identifiant | Force |
|---|---|
| provider + providerOfferId | `STRONG` dans l'espace du provider |
| originalSource + originalId | `STRONG` lorsque vérifié |
| canonical original URL | `USEFUL`, à normaliser prudemment |
| apply URL | `USEFUL` mais peut être partagé par plusieurs offres |
| company + title + location | `WEAK` |
| similar description/title | `UNSAFE` pour une fusion automatique |

Une offre saisie manuellement ne doit pas être fusionnée automatiquement avec une offre automatisée sur
son seul titre, sa société ou son URL. Elle conserve une provenance manuelle et pourra seulement être
signalée comme doublon probable dans une évolution explicitement conçue pour cela.

### JobOffer transient

`JobOffer` reste transient et non JPA.

La multi-source ne justifie pas à elle seule une persistence. Une persistence ou projection séparée pourra
être étudiée si le produit exige catalogue, historique, monitoring passif ou déduplication durable. Elle
devra alors traiter licences, rétention, suppression, provenance, synchronisation et données personnelles.

Un cache ou index technique futur ne transforme pas automatiquement `JobOffer` en aggregate user-owned et
ne crée pas d'`Opportunity`.

### Remote eligibility

La compatibilité remote appartient à la frontière `Eligibility`, pas à `Matching`.

Dans une première évolution, `EligibilityEvaluator` pourra consommer des données normalisées de pays,
région, timezone, sponsorship, mécanisme d'emploi et relocation. Une nouvelle classe/service dédié n'est
pas nécessaire avant que la complexité réelle dépasse un ensemble de règles pures.

`workMode = REMOTE` signifie uniquement que le travail est à distance. Il ne signifie pas que le poste est
exécutable depuis la France.

### Freelance, startup et recruiter prospects

- Une mission freelance reste un `JobOffer` avec des données de rémunération/durée spécifiques ; aucune
  branche freelance séparée n'est créée.
- TJM, durée, date de début, client et intermédiaire sont des extensions possibles du canonique ou de son
  enrichment, selon les besoins d'une future Story.
- Stage, taille, industrie et funding sont principalement du contexte `Company` ou enrichment ; equity et
  compensation peuvent concerner directement l'offre.
- Un recruiter prospect sans vacancy reste hors du pipeline `JobOffer`. Il appartient à une future
  capacité de prospection ou de candidature spontanée, pas à l'acquisition d'offres.

### Passive monitoring

L'architecture reste compatible avec plusieurs modes futurs :

```text
on-demand search
daily acquisition
passive monitoring
company-board monitoring
```

Mais la capacité est déclarée par source, et non supposée globalement. Les sources manuelles, les quotas,
les licences et les boards ciblés peuvent interdire le monitoring. Aucun scheduler, cache, scan ou event
streaming n'est introduit maintenant.

## Portfolio automatisé recommandé

Le portefeuille automatisé initial recommandé est :

1. France Travail, déjà actif.
2. Adzuna, pour valider un second provider global searchable et la normalisation inter-sources.
3. Greenhouse, pour valider un premier ATS direct et la stratégie board/company.

Lever, Jooble et La bonne alternance ne font pas partie de la première vague. Les autres sources restent
accessibles par Manual Intake.

## Séquence d'intégration

```text
France Travail existant
        ↓
ES-015 — Adzuna Provider Integration
        ↓
ES-016 — Greenhouse Target Board Integration
```

Cette séquence apporte d'abord une comparaison globale multi-provider, puis une acquisition directe par
board. Les Stories d'intégration devront préserver la conversion explicite `JobOffer -> Opportunity` et ne
devront pas ajouter de persistence de catalogue sans décision dédiée.

## Alternatives rejetées

- Renommer immédiatement `JobOfferProvider` en abstraction universelle : les patterns non-search ne sont
  pas suffisamment homogènes et cela créerait une interface artificielle.
- Faire de Manual Intake un provider : la saisie humaine est une origine d'entrée, pas un service externe.
- Faire dépendre `EligibilityEvaluator` ou `JobMatchingEngine` des providers : cela rendrait les règles
  impossibles à maintenir et violerait le canonique provider-neutral.
- Introduire un orchestrateur multi-provider avant l'intégration du deuxième provider : aucune exigence
  d'exécution actuelle ne le justifie.
- Persister toutes les offres pour rendre la déduplication possible : cela introduit licence, rétention,
  synchronisation et catalogue sans besoin produit confirmé.
- Utiliser un titre/société similaire comme identité globale : collisions et faux merges.
- Créer un domaine Recruiter/Prospect dans ES-014 : l'observation est réelle mais le besoin produit n'est
  pas encore une offre d'emploi.
- Microservices, broker, event streaming, Elasticsearch ou vector database : complexité sans preuve.

## Conséquences

### Positives

- Les sources automatisées, ATS et manuelles sont distinguées sans polluer le domaine.
- `JobOffer`, `Eligibility`, `Matching`, `Opportunity` et `Application` restent découplés.
- La stratégie Greenhouse/Lever peut évoluer vers des boards ciblés sans prétendre être une recherche globale.
- Les sources à forte valeur mais non automatisables restent utilisables.
- La provenance originale peut être ajoutée sans mentir sur l'origine.
- La déduplication est préparée sans imposer une persistence prématurée.
- Le monolithe Spring Boot actuel reste suffisant.

### Négatives

- Le port actuel reste mono-provider jusqu'à l'intégration du second provider.
- La recherche multi-source et l'orchestration seront une évolution explicite.
- La déduplication durable n'est pas disponible tant que les offres restent transientes.
- Le modèle actuel de `JobOffer` ne représente pas encore toute la provenance, les pays autorisés ou les
  données freelance/startup enrichies.
- Les offres manuelles et automatisées ne peuvent pas être fusionnées automatiquement de manière sûre.

## Hors périmètre

- Toute implémentation provider.
- DTO, mapper, migration, UI ou scheduler.
- Persistence ou cache de `JobOffer`.
- Déduplication effective.
- Enrichment ou extraction de texte.
- Nouvelle règle remote dans le code.
- LLM, embeddings, RAG ou agents.
- Modèle Prospect/Recruiter.
- Migration Spring Boot, microservices, broker ou moteur de recherche distribué.

## Décisions différées

- Forme exacte du connector ATS et configuration des boards ciblés.
- Concept technique de target company/board.
- Orchestration multi-provider et stratégie de fusion des résultats.
- Persistence, cache ou index des offres.
- Schéma détaillé de provenance durable.
- Règles complètes d'éligibilité remote.
- Extraction déterministe des technologies et compétences.
- Déduplication probable inter-sources.
- Réévaluation Lever, Jooble et La bonne alternance.
- Toute réutilisation automatisée nécessitant une partnership.

## Prochaine Engineering Story

```text
ES-015 — Adzuna Provider Integration
```

Cette Story doit implémenter un seul provider global validé, vérifier le port de recherche avec une seconde
source, mapper vers `JobOffer`, préserver les inconnues et maintenir les règles d'Eligibility/Matching sans
couplage provider. Elle ne doit pas commencer Greenhouse, la persistence catalogue ou l'enrichment général.
