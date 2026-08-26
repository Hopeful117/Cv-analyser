# S2 — Plan d'implémentation

## Classification des champs candidats

| Champ candidat | Décision | Justification (consommateurs futurs) |
|---|---|---|
| Intitulés de postes visés (liste libre) | **REQUIRED_NOW** | requête fournisseur (mots-clés), matching sur titre, UX centrale |
| Lieux recherchés (liste libre) | **REQUIRED_NOW** | filtre d'éligibilité + traduction fournisseur |
| Modes de travail acceptés {ONSITE,HYBRID,REMOTE} | **REQUIRED_NOW** | contrainte éliminatoire (ex. remote only) ; traduction workplaceType ATS |
| Types de contrat acceptés | **REQUIRED_NOW** | contrainte éliminatoire ; réutilise `ContractType` existant |
| Technologies exclues | **REQUIRED_NOW** | exclusion déterministe (« jamais de COBOL ») |
| Technologies préférées | **REQUIRED_NOW** | signal de pertinence/ranking |
| Ouverture à la mobilité (booléen) | **REQUIRED_NOW** | bascule le sens des lieux : éliminatoire → simple priorité |
| Salaire minimum (montant + devise + période) | **USEFUL_NOW** | comparaison déterministe quand l'offre l'affiche ; modèle minimal honnête sinon DEFER était préféré — conservé car consommateur démontré au prochain slice et coût faible |
| Distance/temps de trajet max | **DEFER** | exigerait du géospatial, interdit ici |
| Cible de séniorité | **DEFER** | recouvre partiellement les intitulés visés ; pas de consommateur net V1 |
| Temps plein / partiel (`WorkSchedule`) | **DEFER** | discrimine peu au-delà du contrat ; évite un formulaire surchargé |
| Intention libre en texte (« keywords ») | **DEFER** | chevauche intitulés + technologies ; deux concepts flous plutôt qu'un clair |

## Modèle V1

```text
JobSearchPreferences (agrégat actif unique)
├── targetRoles[]            {label, normalizedLabel}      unicité normalisée
├── locations[]              {label, normalizedLabel}      unicité normalisée
├── acceptedWorkModes        Set<WorkMode{ONSITE,HYBRID,REMOTE}>   vide = ouvert à tous
├── contractTypes            Set<ContractType> (enum existant)     vide = aucun filtre
├── preferredTechnologies[]  {label, normalizedName}       unicité normalisée
├── excludedTechnologies[]   {label, normalizedName}       unicité normalisée
├── openToRelocation         BOOLEAN (défaut false)
├── salaryMinAmount          INTEGER nullable (> 0)
├── salaryCurrency           VARCHAR(3) nullable (défaut EUR si montant)
├── salaryPeriod             enum {ANNUAL, MONTHLY} nullable (défaut ANNUAL si montant)
└── created_at / updated_at
```

Hypothèse documentée : montants exprimés BRUTS annuels/mensuels (marché français) ; ni indexation
inflation ni historisation.

## Contraintes ÉLIMINATOIRES vs critères de PERTINENCE (sémantiques pour le slice suivant)

Éliminatoires (l'offre est rejetée) :
- mode de travail de l'offre ∉ modes acceptés (si l'ensemble est non vide) ;
- type de contrat ∉ types acceptés (si non vide) ;
- lieu hors zones listées ET `openToRelocation = false` ;
- technologie exclue présente dans l'offre ;
- salaire affiché par l'offre < minimum (non applicable si l'offre ne l'affiche pas).

Pertinence (l'offre est classée, jamais rejetée) :
- technologies préférées présentes ;
- proximité de l'intitulé visé ;
- lieux listés deviennent simple priorité quand `openToRelocation = true`.

UX : « éliminatoire » ↔ libellés utilisateur « Exclure », « Uniquement », « Souhaité » ; jamais
HARD/SOFT exposé.

## Invariants validés (service)

1. soumission entièrement vide → rejetée (au moins un critère exprimé) ;
2. doublon normalisé dans une même liste → rejeté avec libellé incriminé ;
3. intersection preferredTechnologies ∩ excludedTechnologies ≠ ∅ → rejetée en listant les conflits ;
4. montant de salaire présent ⇒ devise (3 lettres) + période obligatoires, montant > 0 ;
5. longueurs bornées (formulaires + service) ;
6. échec de validation ⇒ agrégat existant strictement intact (validation avant mutation, leçon S1).

## Persistance (V5)

- `career_job_search_preferences` (scalaires + timestamps) ;
- filles FK cascade : `career_preference_role`, `career_preference_location`,
  `career_preference_preferred_technology`, `career_preference_excluded_technology`
  (id, item_order, label, normalized_*, UK(prefs_id, normalized_*)) ;
- ensembles d'énumérés en `@ElementCollection` : `career_preference_work_mode(prefs_id, mode)`
  et `career_preference_contract_type(prefs_id, contract_type)` — relationnel structuré,
  pas de JSON opaque ;
- remplacement d'état = delete ORM de l'agrégat + recréation (leçon S1 : ordre SQL fiable),
  même transaction.

## Coutures futures (documentées, non implémentées)

- propriété : `JobSearchPreferencesRepository.findActivePreferences()` miroir de la couture profil.
- contexte de recherche : le futur slice combinera profil + préférences en entrée du matching —
  PAS de classe `JobSearchContext` maintenant (agrégation prématurée).
- traduction fournisseur : intitulés→mots-clés FT, modes→workplaceType Greenhouse/Lever,
  contrats→vocabulaire local ; lacunes connues notées dans le rapport final.

## Module & routes

Package `search.{domain,persistence,application,web}` — concept distinct = module distinct.

| Route | Rôle |
|---|---|
| GET `/preferences` | vue préférences actives ou état vide |
| GET `/preferences/new` + POST `/preferences` | création |
| GET `/preferences/edit` + POST `/preferences/update` | édition |

Navigation : entrée « Ma recherche » (activePage `search`) après « Profil » ; inspection
explicite anti-placeholder ; test-garde vérifiant une seule occurrence du lien et zéro
« nav-soon » dans la sidebar.

## Plan de commits

1. docs story ; 2. domaine+persistance V5 ; 3. service applicatif ; 4. web ; 5. tests ;
6. rapports.
