# First Real Job Discovery Vertical Slice — Investigation

Statut : investigation uniquement — aucune Engineering Story créée, aucune implémentation.

## Légende de preuve

- **OBSERVED** : vérifié dans Git, le dépôt courant ou une documentation fournisseur citée.
- **INFERRED** : conclusion raisonnable à partir des éléments observés, non prouvée directement.
- **PROPOSED** : recommandation pour un futur Story, non implémentée.
- **NOT VERIFIED** : information à confirmer avant implémentation.

## Question principale

Quel est le plus petit slice vertical sûr permettant à CV Analyzer de récupérer de vraies offres
France Travail, de les normaliser en `JobOffer` canonique, d'évaluer leur éligibilité déterministe
avec `ProfessionalProfile` + `JobSearchPreferences`, puis d'exposer un résultat utile à
l'utilisateur ?

## État Git au démarrage

- **OBSERVED** — arbre propre, branche initiale `main`, worktree unique.
- **OBSERVED** — `main` et le pointeur local `origin/main` = `15c83c4`, merge PR #2 S2.
- **OBSERVED** — l'historique du merge contient le correctif visuel `36e1326`.
- **OBSERVED** — branche documentaire créée : `investigation/job-discovery-vertical-slice`.
- **OBSERVED** — `git fetch` a échoué : l'agent SSH a refusé la signature de la clé ED25519-SK.
- **NOT VERIFIED** — l'état distant postérieur à `15c83c4` ne peut donc pas être confirmé.

## Benchmark DevLog avant inspection du dépôt

### Fraîcheur

| Checkpoint | Révision / état | Source |
|---|---|---|
| observed/current repository revision | `15c83c469b69cdd952fad0c035b14606f05c1d7a` | `get_engineering_context`, resource freshness |
| requested revision | `origin/main` | resource freshness |
| ingested revision | `15c83c469b69cdd952fad0c035b14606f05c1d7a` | resource freshness |
| context revision | `8402ea6011fb392969298c54b979148391a1b09a` | `get_engineering_context` |
| baseline analyzed revision | `8402ea6011fb392969298c54b979148391a1b09a` | resource freshness |
| status | `PARTIALLY_FRESH`, `REFRESH_RECOMMENDED` | les deux projections |
| review queue | 5 pending, 0 accepted, 0 rejected | resource freshness |

- **OBSERVED** — la collecte Git/commit est à jour jusqu'au merge S2, mais la base de connaissance
  analysée reste antérieure aux Stories S1 et S2.
- **OBSERVED** — contrairement au benchmark précédent, la projection ne dit plus `CURRENT` : elle
  expose correctement `PARTIALLY_FRESH` et l'écart de révisions.
- **OBSERVED** — le contexte était tronqué : 60 éléments sélectionnés sur 130, budget appliqué.

### Contexte utile surfacé

- **OBSERVED** — les merges S1 (`c0b415b`) et S2 (`15c83c4`), leurs noms de fichiers et les
  rapports de Story ont été repérés dans les preuves de commits.
- **OBSERVED** — DevLog a signalé les surfaces `profile`, `search`, migrations V4/V5, contrôleurs,
  formulaires, templates et tests.
- **OBSERVED** — la ressource du merge S2 fournit une liste de fichiers exacte, incluant le
  nettoyage visuel final du dashboard.

### Manques et informations trompeuses

- **OBSERVED** — trois recherches d'historique (`ProfessionalProfile JobSearchPreferences`,
  France Travail/provider/job discovery, matching/eligibility/ranking/aggregator) ont retourné
  zéro résultat.
- **OBSERVED** — aucune décision, règle métier, structure de champ, intégration fournisseur ou
  sémantique d'éligibilité n'a été restituée au niveau contenu.
- **OBSERVED** — la ressource directe du merge S1 était illisible via MCP.
- **OBSERVED** — des observations anciennes (par exemple deux tests d'intégration) coexistent avec
  les preuves de commits récentes ; elles ne décrivent pas forcément l'état courant.
- **INFERRED** — les listes de fichiers de commits peuvent donner l'impression que S1/S2 sont
  pleinement représentées, alors que la connaissance analysée et recherchable ne les contient pas.

### Valeur du benchmark

- **OBSERVED** — DevLog a aidé à confirmer la révision observée et à localiser les artefacts S1/S2.
- **OBSERVED** — il n'a pas matériellement réduit l'exploration nécessaire : tous les modèles,
  décisions, comportements et conventions doivent être reconstruits depuis le dépôt.
- **PROPOSED** — ne pas utiliser les résultats DevLog comme vérité de domaine avant acceptation des
  cinq revues et réalignement du baseline sur la révision ingérée.

## État produit actuel

### Capacités existantes

- **OBSERVED** — application locale mono-utilisateur, Java 21 / Spring Boot 4, MVC Thymeleaf,
  JPA/Flyway/MySQL ; aucune sécurité Spring, aucun scheduler, cache, retry ou client OAuth générique
  (`pom.xml`, `README.md`, `application.properties`).
- **OBSERVED** — S1 fournit un agrégat `ProfessionalProfile`, sa saisie manuelle, une proposition
  CV/IA explicitement revue, la normalisation des compétences/langues et la couture mono-utilisateur
  `findLocalProfile()`.
- **OBSERVED** — S2 fournit un agrégat `JobSearchPreferences` indépendant, entièrement saisi par
  l'utilisateur, sa CRUD MVC, sa persistance V5 et `findActivePreferences()`.
- **OBSERVED** — aucun `JobOffer`, fournisseur d'offres, collecteur, matcher d'éligibilité ou
  classement d'offres n'existe en production.
- **OBSERVED** — l'intégration Google Sheets donne un précédent utile : port applicatif,
  `@ConfigurationProperties`, adaptateurs activé/désactivé, credentials hors dépôt, erreurs
  fournisseur traduites et identité externe stable.
- **OBSERVED** — l'UX existante privilégie contrôleur mince, service transactionnel, view records,
  PRG/flash et navigation par `activePage`.

### Concepts à ne pas confondre

- **OBSERVED** — `OpportunityEntity` est un objet de travail choisi par l'utilisateur : offre
  manuelle/URL, analyse CV, lettre et/ou candidature. Il est compté directement dans les métriques
  dashboard/CRM (`career/persistence/OpportunityEntity.java`, `CareerWorkspaceService.java`,
  `ApplicationCrmService.java`).
- **INFERRED** — importer les découvertes comme `OpportunityEntity` polluerait les compteurs et
  confondrait « offre trouvée » avec « opportunité retenue ».
- **PROPOSED** — une future conversion `JobOffer -> Opportunity` doit être une action utilisateur
  explicite, hors du premier slice.
- **OBSERVED** — `ResumeAnalyzer` et `AiResumeAnalyzer` évaluent un CV contre un texte d'offre ; ils
  ne constituent ni une éligibilité déterministe, ni une recherche autonome. Le premier slice ne
  doit pas réutiliser leur score IA comme verdict.

## Rôle de ProfessionalProfile

Les classifications ci-dessous décrivent l'usage sûr dans le premier slice ; elles ne changent pas
S1 et ne prétendent pas que le profil est exhaustif.

| Champ existant | Classification | Usage / limite |
|---|---|---|
| `professionalTitle` | `RANKING_INPUT` | proximité explicable avec titre/ROME, jamais éliminatoire |
| skill `normalizedName` (+ `label`) | `ELIGIBILITY_INPUT`, `RANKING_INPUT` | preuve positive par correspondance exacte avec compétences ; une absence ne prouve pas l'inéligibilité |
| experience `title`, `description` | `RANKING_INPUT` | pertinence textuelle future ; pas de matching fiable V1 |
| experience `startDate`, `endDate` | `ELIGIBILITY_INPUT` | durée dérivable, mais exigence fournisseur non numérique au retour |
| education `kind`, `label` | `ELIGIBILITY_INPUT` | preuve de diplôme/certification ; labels non normalisés, donc pas de rejet sûr V1 |
| language `normalizedLanguage`, `level` | `ELIGIBILITY_INPUT` | langue exigée comparable ; niveau libre non ordonnable déterministement |
| `fullName` | `DISPLAY_ONLY` | identité, jamais utilisée pour découvrir/qualifier |
| `referenceLocation` | `DISPLAY_ONLY` | lieu actuel, PAS zone recherchée |
| skill/language labels, experience company, education institution/date, item order | `DISPLAY_ONLY` | présentation/contexte sans règle établie |
| ids, FKs, timestamps, origines de skill, métadonnées IA | `NOT_RELEVANT_TO_JOB_DISCOVERY` | identité technique/provenance uniquement |

- **OBSERVED** — `ProfessionalProfile` ne possède ni séniorité canonique, ni années d'expérience
  persistées, ni niveau de diplôme ordonné, ni échelle canonique de langues.
- **PROPOSED** — dans ce slice, le profil produit des **preuves positives et des manques à vérifier**,
  pas des rejets automatiques. Rejeter une offre sur absence de skill/diplôme/langue serait dangereux
  sans notion de complétude du profil et sans ontologie commune.

## Rôle de JobSearchPreferences

### Modèle exact S2

Un jeu actif contient : `targetRoles[]`, `locations[]`, `acceptedWorkModes Set<ONSITE|HYBRID|REMOTE>`,
`contractTypes Set<CDI|CDD|ALTERNANCE|STAGE|FREELANCE|INTERIM|OTHER>`,
`preferredTechnologies[]`, `excludedTechnologies[]`, `openToRelocation`, et un salaire minimal
optionnel (`Integer amount`, devise trois lettres, période `ANNUAL|MONTHLY`). Les listes conservent
leur libellé et une forme normalisée déterministe.

| Champ | Classifications | Sémantique vérifiée dans S2 |
|---|---|---|
| target roles | `SEARCH_QUERY_INPUT`, `SOFT_PREFERENCE`, `RANKING_SIGNAL` | mot-clé/titre ; jamais éliminatoire |
| locations | `SEARCH_QUERY_INPUT`, `HARD_ELIGIBILITY`, `SOFT_PREFERENCE`, `RANKING_SIGNAL` | hors zone = rejet si non mobile ; priorité si mobile |
| accepted work modes | `SEARCH_QUERY_INPUT`, `HARD_ELIGIBILITY` | mode connu hors ensemble non vide = rejet |
| contract types | `SEARCH_QUERY_INPUT`, `HARD_ELIGIBILITY` | contrat connu hors ensemble non vide = rejet |
| preferred technologies | `SOFT_PREFERENCE`, `RANKING_SIGNAL` | présence améliore la pertinence, jamais un rejet |
| excluded technologies | `HARD_ELIGIBILITY` | présence démontrée = rejet |
| open to relocation | `HARD_ELIGIBILITY`, `RANKING_SIGNAL` | bascule la zone de hard vers soft |
| salary amount/currency/period | `HARD_ELIGIBILITY` | rejet uniquement si salaire affiché et comparable sous le minimum |
| ids, timestamps, ordre, FKs | `NOT_DIRECTLY_APPLICABLE` | persistance/audit |

- **OBSERVED** — ces règles sont documentées mais aucun code S2 ne les exécute encore.
- **OBSERVED** — les zones sont du texte libre ; une liste vide n'a pas une sémantique de matching
  explicitement arrêtée dans le plan S2.
- **PROPOSED** — le premier matcher doit considérer ensemble vide = aucune restriction, cohérent
  avec les modes/contrats et avec le fait qu'une soumission peut être valide sans zone.

## France Travail — faits fournisseur

Sources officielles consultées le 2026-08-26 :

- [Produit Offres d'emploi](https://francetravail.io/data/api/offres-emploi)
- [OpenAPI 3.0.1, API Offres v2.01](https://francetravail.io/api-peio/v2/api/84/openapi)
- [Documentation interactive](https://francetravail.io/data/api/offres-emploi/documentation)
- [Licence spécifique Offres d'emploi](https://francetravail.io/produits-partages/documentation/conditions-dutilisation-api/licence-offres-emploi)

### Accès et authentification

- **OBSERVED** — accès gratuit/public au sens produit, mais non anonyme : compte
  francetravail.io, application associée, client ID et secret.
- **OBSERVED** — OAuth 2 `client_credentials`, token endpoint
  `https://entreprise.francetravail.fr/connexion/oauth2/access_token?realm=%2Fpartenaire`, scopes
  obligatoires `api_offresdemploiv2 o2dsoffre application_{client_id}`, Bearer token sur chaque ressource.
- **OBSERVED** — durée du token : 1499 secondes (~25 minutes), réponse `expires_in` présente.
- **OBSERVED** — les deux credentials `.env` (`FRANCE_TRAVAIL_ID`, `FRANCE_TRAVAIL_SECRET_KEY`)
  sont suffisants pour authentifier et rechercher. Le scope `application_{client_id}` est requis
  en plus des deux scopes API ; sans lui, le endpoint retourne `invalid_client` même avec des
  credentials valides.

**Contrat d'authentification vérifié :**

```bash
curl -s -X POST \
  "https://entreprise.francetravail.fr/connexion/oauth2/access_token?realm=%2Fpartenaire" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=${FRANCE_TRAVAIL_ID}" \
  -d "client_secret=${FRANCE_TRAVAIL_SECRET_KEY}" \
  -d "scope=api_offresdemploiv2 o2dsoffre application_${FRANCE_TRAVAIL_ID}"
```

**Réponse succès :**
```json
{
  "access_token": "...",
  "scope": "application_PAR_cvanalyzer_... api_offresdemploiv2 o2dsoffre",
  "token_type": "Bearer",
  "expires_in": 1499
}
```

**Réponse erreur (scope application manquant) :**
```json
{
  "error": "invalid_client",
  "error_description": "Client authentication failed"
}
```

### Recherche et pagination

- **OBSERVED** — `GET /partenaire/offresdemploi/v2/offres/search` fonctionne avec succès (HTTP 200).
- **OBSERVED** — critères utiles testés : `motsCles`, `lieux`, `typeContrat`, `range`.
- **OBSERVED** — pagination `range=p-d` fonctionne : `Content-Range: offres 0-49/424` puis
  `Content-Range: offres 100-149/424`. Maximum 150 offres/appel confirmé.
- **OBSERVED** — `motsCles` accepte plusieurs expressions ; recherche "developpeur java" retourne
  424 résultats.
- **OBSERVED** — `typeContrat=CDD` retourne des offres CDD ; `INTERIM` retourne 0 résultats
  (possible que les offres intérim ne soient pas dans cette API).
- **OBSERVED** — quota réel non testé (4 requêtes/seconde documenté par la communauté).

**Exemple de requête vérifiée :**

```bash
curl -s "https://api.francetravail.io/partenaire/offresdemploi/v2/offres/search?motsCles=developpeur+java&lieux=75000&range=0-49" \
  -H "Authorization: Bearer $TOKEN"
```

**En-tête réponse :**
```
Content-Range: offres 0-49/424
```

### Données d'offre

| Capacité | Disponibilité | Champs / remarque | Statut vérification |
|---|---|---|---|
| identifiant fournisseur | `OBSERVED` | `id` France Travail (ex: "212XWYG") | CONFIRMED |
| URL publique | `OBSERVED` | `origineOffre.urlOrigine` | CONFIRMED |
| titre | `OBSERVED` | `intitule` texte libre | CONFIRMED |
| description | `OBSERVED` | `description` HTML/texte très détaillé | CONFIRMED |
| entreprise | `OBSERVED` | `entreprise.nom` | CONFIRMED |
| métier | `OBSERVED` | `romeCode`, `romeLibelle`, `appellationlibelle` | CONFIRMED |
| lieu | `OBSERVED` | `lieuTravail.libelle`, `.codePostal`, `.commune`, `.latitude`, `.longitude` | CONFIRMED |
| contrat | `OBSERVED` | `typeContrat` (CDI/CDD), `typeContratLibelle`, `natureContrat` | CONFIRMED |
| compétences | `OBSERVED` | `competences[]` avec `code`, `libelle`, `exigence` (E/S) | CONFIRMED |
| expérience | `OBSERVED` | `experienceExige` (E/D/S), `experienceLibelle` ("X An(s)") | CONFIRMED |
| salaire | `OBSERVED` | `salaire.libelle` structuré : "Annuel de X à Y Euros sur Z mois" | CONFIRMED |
| télétravail | `OBSERVED` | Champ `teletravail` existe mais souvent `null` | CONFIRMED |
| durée travail | `OBSERVED` | `dureeTravailLibelle` ("37H/semaine"), `dureeTravailLibelleConverti` ("Temps plein") | CONFIRMED |
| dates | `OBSERVED` | `dateCreation`, `dateActualisation` ISO 8601 | CONFIRMED |
| accessibilité | `OBSERVED` | `entrepriseAdaptee`, `employeurHandiEngage` booléens | CONFIRMED |
| total/continuation | `OBSERVED` | `Content-Range: offres p-d/t` | CONFIRMED |

**Différences avec les hypothèses précédentes :**

- **Salaire** : plus structuré que prévu — format "Annuel de X à Y Euros sur Z mois" est parsable
  (min, max, période). Les compléments (`complement1`, `complement2`, `listeComplements`) sont
  optionnels mais présents.
- **Expérience** : `experienceExige` contient "E" (Exigée), pas "D/E/S" comme hypothéssé.
  `experienceLibelle` contient "X An(s)" ou "Débutant accepté".
- **Télétravail** : le champ `teletravail` existe mais est `null` dans la majorité des offres testées.
  Aucune donnée structurée hybride/remote n'est disponible.
- **CDD** : `dureeContrat` est `null` même pour les offres CDD (non renseigné par le fournisseur).
- **INTERIM** : aucun résultat pour `typeContrat=INTERIM` (possible que les offres intérim ne soient
  pas dans cette API ou nécessitent un paramètre différent).

**Exemple de payload réel (sanitizé) :**

```json
{
  "id": "212XWYG",
  "intitule": "DEVELOPPEUR JAVA BACK END SENIOR (H/F)",
  "typeContrat": "CDI",
  "experienceExige": "E",
  "experienceLibelle": "1 An(s)",
  "lieuTravail": {
    "libelle": "92 - CLICHY",
    "latitude": 48.902793,
    "longitude": 2.30484,
    "codePostal": "92110",
    "commune": "92024"
  },
  "entreprise": { "nom": "NEXPUBLICA" },
  "salaire": { "libelle": "Annuel de 65000.0 Euros sur 12.0 mois" },
  "competences": [
    { "code": "300688", "libelle": "Application web", "exigence": "S" }
  ],
  "romeCode": "M1855",
  "dateCreation": "2026-08-26T10:25:04.047Z",
  "origineOffre": { "urlOrigine": "https://candidat.francetravail.fr/offres/recherche/detail/212XWYG" }
}
```

### Licence et conformité

La licence spécifique change matériellement la taille d'un slice persistant :

- **OBSERVED** — attribution France Travail, date de dernière mise à jour et lien vers la licence
  doivent être aisément accessibles.
- **OBSERVED** — pour une réutilisation de rapprochement, l'API doit être sollicitée au moins une
  fois toutes les 24 heures et les créations, modifications et suppressions doivent être reflétées.
- **OBSERVED** — la date de première publication/actualisation de chaque offre doit être conservée.
- **OBSERVED** — la totalité du contenu fourni pour chaque offre, logo compris, doit figurer sur
  l'offre diffusée.
- **OBSERVED** — la méthode/les modifications appliquées (par exemple l'algorithme) doivent être
  mises à disposition des utilisateurs.
- **OBSERVED** — si une offre supprimée est retenue, des données entreprise/contact/lieu listées par
  la licence doivent être anonymisées ; les données personnelles imposent minimisation, durée,
  sécurité et hébergement UE ou protection équivalente.
- **OBSERVED** — il est interdit de vendre les offres ou d'exiger une rétribution du chercheur
  d'emploi en contrepartie d'un service de placement.
- **NOT VERIFIED** — l'interprétation « une page détail complète adjacente satisfait-elle l'obligation
  lorsque la liste n'affiche qu'une carte ? » doit être confirmée auprès de France Travail/juridique.
- **NOT VERIFIED** — la compatibilité d'un éventuel abonnement premium avec l'interdiction de
  rémunération du placement doit être qualifiée juridiquement avant monétisation.

## Faisabilité France Travail

### Verdict : `SUITABLE` (GO_FOR_S3)

France Travail est techniquement validée : authentification réussie, recherche temps réel
fonctionnelle, payloads réels inspectés, compétences structurées, salaire parsable, pagination
confirmée. Le cœur fournisseur -> canonique -> évaluation -> UI est réalisable.

Limitations par étape :

| Étape | Impact |
|---|---|
| collection | OAuth credentials requis ; quota inconnu ; recherche bornée à 3150 résultats ; licence 24 h si contenu persisté/diffusé |
| normalisation | mapping contrats/référentiels à maintenir ; salaire retour textuel ; pas d'expiration |
| éligibilité | contrat fiable ; géographie possible après résolution ; remote non fiable ; expérience/salaire partiellement textuels |
| ranking/qualification | compétences structurées utiles, mais ontologie profil différente ; correspondance exacte seulement sûre |
| UX | attribution/licence/mise à jour et contenu complet obligatoires ; états « à vérifier » nécessaires |

- **INFERRED** — aucun autre fournisseur n'est assez évidemment plus simple pour justifier un
  changement : France Travail reste le meilleur premier candidat français malgré ces limites.
- **PROPOSED** — ne pas demander une autre API (ROME 4/ROMEO) dans le premier slice ; cela ajouterait
  credentials, mapping et/ou IA avant validation de la chaîne minimale.

## Frontière fournisseur

```text
JobOfferProvider (port applicatif)
        |
        v
FranceTravailJobOfferProvider (infrastructure)
        |
        +-- FranceTravailTokenClient
        +-- FranceTravailApiClient
        +-- FranceTravailOfferDto (jamais exposé hors infrastructure)
        +-- FranceTravailSearchTranslator
        +-- FranceTravailOfferMapper
                         |
                         v
                 JobOffer canonique
                         |
                         v
             EligibilityEvaluator pur
```

- **PROPOSED** — le port reçoit une intention neutre (`targetRole`, contraintes pertinentes,
  page/limit) et retourne des `JobOffer` canoniques plus métadonnées de collecte ; aucun code/libellé
  France Travail dans le domaine.
- **PROPOSED** — l'adaptateur possède seul OAuth, DTO JSON, paramètres `motsCles/range/sort`, codes
  référentiels et mapping de contrat.
- **PROPOSED** — le mapper conserve les valeurs brutes avec la valeur canonique nullable : une
  valeur fournisseur inconnue devient `UNKNOWN`, jamais une valeur métier inventée.
- **PROPOSED** — credentials via variables d'environnement/configuration typée, intégration
  désactivée par défaut, secret jamais loggé/persisté/rendu ; token en mémoire selon l'expiration
  retournée, si elle est confirmée.

## `JobOffer` canonique minimal

Record domaine non JPA pour le slice d'essai :

- source : provider key, provider offer ID, origine/partenaire, URLs, fetched-at ;
- identité métier : titre, description, entreprise ;
- métier : code/libellé ROME/appellation ;
- lieu : libellé, commune INSEE, code postal, latitude/longitude ;
- contrat : `ContractType?`, code/libellé/nature bruts, alternance ;
- exigences : compétences, langues, formations, permis et expérience avec exigence `REQUIRED` /
  `DESIRED` / `UNKNOWN` ;
- conditions : durée/contexte/déplacement et `WorkMode UNKNOWN` tant qu'aucune donnée structurée ;
- rémunération : texte/complements bruts + structure optionnelle uniquement si un parseur strict
  prouve montant/période/devise ;
- dates : created-at, updated-at ; expiration absente/unknown ;
- informations de diffusion complémentaires (contact, agence, accessibilité, qualification,
  secteur, postes, logo) nécessaires au détail complet imposé par la licence.

- **PROPOSED** — ne pas ajouter de blob DTO France Travail au domaine. Si l'obligation de totalité
  rend ce modèle trop large, un `SourceOfferDisclosure` neutre et immuable peut accompagner le
  canonique pour la présentation, sans participer à l'éligibilité.
- **PROPOSED** — ne pas créer de classe générique `RuleEngine`, `Aggregator`, `JobSearchContext` ou
  taxonomie universelle dans ce premier slice.

## Éligibilité déterministe et prudente

### Résultat à trois états

- `ELIGIBLE` : aucun hard constraint connu ne rejette et toutes les contraintes applicables sont
  observables.
- `INELIGIBLE` : au moins une règle hard produit une preuve de rejet explicite.
- `REVIEW_REQUIRED` : aucune preuve de rejet, mais une contrainte hard ne peut pas être évaluée à
  cause d'une donnée inconnue/non résolue.

Chaque décision contient des reasons typées + texte produit ; aucun score IA.

### Matrice V1

| Règle | Décision sûre |
|---|---|
| contrat accepté non vide | mapped et absent => `INELIGIBLE`; code inconnu => `REVIEW_REQUIRED` |
| lieu, mobilité false | zones et offre résolues incompatibles => `INELIGIBLE`; résolution incertaine => review |
| lieu, mobilité true | jamais de rejet ; match de zone = signal affichable |
| work mode accepté non vide | mode structuré absent chez FT => `REVIEW_REQUIRED`, jamais rejet textuel |
| technologie exclue | match exact normalisé dans champs explicitement inspectés => `INELIGIBLE`; sinon pas de rejet |
| salaire minimal | valeur strictement parsée et comparable sous seuil => `INELIGIBLE`; salaire absent/incomparable => reste visible avec raison |
| technologie préférée | preuve positive uniquement |
| profil skills/langues/formation/expérience | preuves positives/manques à vérifier uniquement dans V1 ; jamais rejet automatique |

- **PROPOSED** — `ProfileNormalizer` peut comparer libellés exacts normalisés, mais pas faire de
  sous-chaîne naïve (`java` ne doit pas matcher `javascript`).
- **PROPOSED** — aucune déduction remote à partir de description en V1 : le faux rejet est plus
  dommageable qu'un état à vérifier.
- **PROPOSED** — le salaire manquant respecte S2 : visible, jamais rejeté.

## Plus petit slice vertical sûr

### Recommandation

**PROPOSED — recherche live manuelle, bornée et non persistée.**

1. L'écran exige un profil et des préférences existants ; sinon CTA vers les écrans S1/S2.
2. L'utilisateur choisit UN rôle parmi `targetRoles` et déclenche « Rechercher maintenant ».
3. Une seule requête France Travail `motsCles=<rôle>`, `sort=1`, `range=0-49` récupère les 50 offres
   les plus récentes. Contrats peuvent être traduits uniquement après validation du référentiel.
4. L'adaptateur mappe la réponse en `JobOffer` canonique en mémoire.
5. Le service pur évalue chaque offre avec le profil + les préférences actives.
6. La page rend l'ordre fournisseur, le statut/reasons, les preuves de profil/préférences, les
   données complètes de source accessibles, le lien public, l'attribution, la date et la licence.
7. Rien n'est sauvegardé : rechargement = nouvel appel ; aucun `Opportunity` n'est créé.

Pourquoi c'est le minimum sûr :

- valide une vraie authentification, recherche, DTO, mapping, canonique, règles et UX ;
- borne à un appel et 50 résultats, sans scheduler/quota supposé ;
- n'introduit pas une fausse exhaustivité (échantillon explicite d'un rôle) ;
- évite catalogue périmé, réconciliation de suppressions, anonymisation historique et pollution CRM ;
- reste strictement non autonome.

### Ce que le slice ne doit pas inclure

- scheduler, recherche de fond, multi-provider, crawl, alertes, emails ;
- persistance d'offres ou d'évaluations ;
- création automatique d'Opportunity/candidature ;
- pagination au-delà du premier lot, géocodage libre, ROME/ROMEO supplémentaire ;
- scoring IA, embeddings, résumé/réécriture de contenu ;
- classement pondéré ou moteur de règles générique.

### UX minimale

- route/page dédiée « Offres trouvées » avec état non configuré, prérequis manquant, chargement,
  résultats, aucun résultat et erreur fournisseur ;
- bandeau honnête « 50 offres récentes maximum pour ce rôle — résultat non exhaustif » ;
- filtres d'affichage `ELIGIBLE`, `REVIEW_REQUIRED`, `INELIGIBLE`, sans cacher les inconnues ;
- raisons visibles (« contrat non accepté », « télétravail non renseigné », etc.) ;
- détail complet/attribution conforme sous réserve de validation juridique Article 5.3 ;
- erreur API/token sans effacer de données, puisqu'aucune donnée n'est persistée.

## Persistance — décision du premier slice

- **PROPOSED** — aucune table/migration dans le premier slice.
- **INFERRED** — persister même une copie canonique déclenche les obligations de base dérivée,
  synchronisation quotidienne, suppression/anonymisation et documentation des transformations.
- **PROPOSED** — le premier Story persistant devra être distinct et inclure au minimum : identité
  unique `(provider, provider_offer_id)`, full content, publication/update/fetch/last-seen,
  synchronisation <=24 h, reconciliation detail/204, suppression ou anonymisation, attribution,
  méthode publiée, monitoring et tests de panne. Ce n'est plus un slice manuel minimal.
- **PROPOSED** — l'évaluation doit rester calculée à la lecture tant que le volume est faible ;
  persister un verdict exige version/hash profil+préférences+règles pour éviter un état obsolète.

## Risques et protections

| Risque | Protection proposée |
|---|---|
| secret/token exposé | config env typée, redaction logs, adapter disabled sans credentials |
| appel bloquant | timeouts connexion/lecture, taille réponse bornée, statuts explicites |
| quota inconnu | un appel manuel borné, pas de retry aveugle, métrique locale |
| token expiré | cache mémoire fondé sur `expires_in` après validation ; un renouvellement contrôlé |
| mapping contrat inconnu | mapping exhaustif testé sur référentiel réel ; unknown -> review |
| faux rejet remote/salaire/skill | tri-state, parse strict, preuve explicite, unknown conservé |
| contenu altéré | source brute sémantique préservée, analyse clairement séparée |
| licence | gate juridique Article 5.3/monétisation, attribution et méthode accessibles |
| confusion Opportunity | aucune écriture CRM dans le slice |

## Validation attendue d'un futur Story

- contract tests sur exemples OpenAPI/fixtures sanitizées ;
- tests token/cache/401/204/206/400/500/timeouts et secret absent ;
- tests mapper (valeurs connues/inconnues, nulls, accents, salaire texte) ;
- table-driven tests purs de chaque verdict + reasons ;
- tests MVC prérequis/erreur/aucun résultat/résultats/attribution ;
- démarrage application avec intégration désactivée et aucun credential ;
- test manuel réel avec une application France Travail dédiée, sans enregistrer les secrets.

## Décision et gates avant Engineering Story

### Gates techniques — SATISFAITS

1. **SATISFIED** — credentials d'application disponibles et fonctionnels.
2. **SATISFIED** — authentification OAuth2 `client_credentials` réussie, token Bearer obtenu.
3. **SATISFIED** — durée du token confirmée : 1499 secondes (~25 minutes).
4. **SATISFIED** — recherche temps réel exécutée avec succès (HTTP 200, 424 résultats).
5. **SATISFIED** — payload réel inspecté sur plusieurs offres, tous les champs clés confirmés.
6. **SATISFIED** — codes contrats réels : CDI et CDD fonctionnent ; INTERIM non testé (0 résultats).
7. **SATISFIED** — pagination Content-Range fonctionnelle.
8. **SATISFIED** — salaire parsable : format "Annuel de X à Y Euros sur Z mois" confirmed.

### Gates non techniques — NON RÉSOLUS

4. **NOT RESOLVED** — confirmer que l'usage local prévu et l'hébergement respectent RGPD/géographie.
5. **NOT RESOLVED** — faire confirmer par France Travail/juridique que carte + détail complet satisfait
   Article 5.3 et préciser l'attribution/méthode attendues.
6. **NOT RESOLVED** — compatibilité d'un éventuel abonnement premium avec l'interdiction de
   rémunération du placement doit être qualifiée juridiquement avant monétisation.

### Décision

**GO_FOR_S3** — les gates techniques sont satisfaits. Les gates non techniques ne bloquent pas
l'implémentation d'un slice live/non persistant, mais doivent être résolus avant toute
persistance ou monétisation.

## Questions résolues par le spike

- **RÉSOLU** — durée du token : 1499 secondes (~25 minutes), réponse `expires_in` présente.
- **RÉSOLU** — codes contrats réels : CDI et CDD fonctionnent ; `typeContrat=INTERIM` retourne 0 résultats.
- **RÉSOLU** — salaire au retour : format structuré "Annuel de X à Y Euros sur Z mois", parsable.
- **RÉSOLU** — compétences : `competences[]` avec `code`, `libelle`, `exigence` (E/S) confirmé.
- **RÉSOLU** — télétravail : champ `teletravail` existe mais est `null` dans la majorité des offres.
- **RÉSOLU** — durée contrat CDD : `dureeContrat` est `null` même pour les offres CDD.

## Questions non résolues

- **NON RÉSOLU** — unité et politique du `nombreAppelMax: 100` (quota réel non testé).
- **NON RÉSOLU** — comportement exact en cas de rate limit (429) ; retry-After non observé.
- **NON RÉSOLU** — exhaustivité réelle des compétences et présence textuelle du télétravail.
- **NON RÉSOLU** — possibilité/coût de résoudre les zones libres via référentiels sans charger
  toutes les communes.
- **NON RÉSOLU** — sens juridique précis de « totalité du contenu » pour une liste + détail.
- **NON RÉSOLU** — conditions de monétisation d'une qualification d'offres.
- **NON RÉSOLU** — comportement exigé en cas d'indisponibilité fournisseur si persistance créée.

## Configuration requise pour S3

```
FRANCE_TRAVAIL_ID=<votre_client_id PAR_cvanalyzer_...>
FRANCE_TRAVAIL_SECRET_KEY=<votre_client_secret 64 hex>
```

**Note :** le scope `application_{client_id}` doit être ajouté dynamiquement dans la requête
token. Les deux variables `.env` actuelles sont suffisantes ; aucune variable supplémentaire
n'est requise.

---

# Résultat final du spike — GO_FOR_S3

## Résumé exécutif

Le spike technique a validé l'accès à l'API France Travail Offres d'emploi v2.
L'authentification OAuth2 fonctionne, la recherche temps réel retourne des offres réelles,
et tous les champs clés du payload ont été inspectés et confirmés.

## État technique vérifié

| Élément | Résultat |
|---------|----------|
| Authentification | SUCCÈS — token Bearer obtenu |
| Durée token | 1499 secondes (~25 minutes) |
| Recherche | SUCCÈS — 424 résultats pour "developpeur java" Paris |
| Pagination | Content-Range fonctionnel |
| Salaire | Parsable — "Annuel de X à Y Euros sur Z mois" |
| Compétences | Structurées — code, libellé, exigence (E/S) |
| Contrats | CDI et CDD fonctionnent |
| Télétravail | Champ existe mais null — UNKNOWN pour S3 |

## Portée du futur S3

- Recherche live manuelle, bornée à 50 offres
- Évaluation tri-state (ELIGIBLE/INELIGIBLE/REVIEW_REQUIRED)
- Aucune persistance, aucun Opportunity créé
- Attribution conforme licence France Travail

## Modification production
AUCUNE

## Fusion
La branche `investigation/job-discovery-vertical-slice` reste non fusionnée.
