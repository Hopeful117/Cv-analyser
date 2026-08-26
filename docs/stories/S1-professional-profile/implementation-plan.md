# S1 — Plan d'implémentation

## Modèle dérivé (plus petit modèle cohérent)

Chaque champ a un consommable démontré ou immédiat (matching déterministe futur, réutilisation dans
l'analyse/lettres, affichage) :

```text
ProfessionalProfile (agrégat, singleton local documenté)
├── fullName            VARCHAR(200)   — identité, réutilisable par génération CV/lettres
├── professionalTitle   VARCHAR(200)   — titre professionnel fiable (l'IA en dérive déjà un par analyse)
├── referenceLocation   VARCHAR(300)   — lieu de référence du candidat (PAS une préférence de recherche)
├── skills[]            ProfileSkill       {label, normalizedName, origin(MANUAL|FROM_CV)}
│                                          unicité (profile_id, normalized_name)
├── experiences[]       ProfileExperience  {title, company?, startDate?, endDate?, summary?}
│                                          durée DÉRIVABLE des dates → non persistée
├── educations[]        ProfileEducation   {kind(EDUCATION|CERTIFICATION), label, institution?, obtainedOn?}
│                                          UN concept : même forme, valeur de crédibilité identique
├── languages[]         ProfileLanguage    {language, normalizedLanguage, level?}
│                                          langues humaines uniquement — les technologies restent des skills
├── traçabilité IA      ai_provider / ai_model / prompt_version / cv_assisted_at
└── created_at / updated_at
```

Exclus volontairement : coordonnées détaillées (non consommées), résumé libre (non consommé),
séniorité calculée (dérivable), tout critère de recherche (Story suivante).

## Décisions structurantes

1. **Proposition NON persistée** — l'extraction rend un formulaire de revue éditable (précédent :
   résultats d'analyse rendus depuis le POST `/analyze`). Rien n'est écrit avant validation.
   Traçabilité reportée sur le profil lors de l'application (`ai_*`, `cv_assisted_at`).
2. **Application d'une proposition additive et explicite** :
   - champs scalaires : appliqués seulement si la case « appliquer » est cochée ;
   - items de listes : chaque item est coché/éditable ; application = AJOUT après déduplication
     normalisée (jamais de remplacement silencieux du existant) ;
   - aucune case cochée + profil inexistant ⇒ erreur fonctionnelle (pas de profil vide créé).
3. **Édition manuelle = état complet soumis** — remplacement transactionnel des collections ;
   les compétences resoumises passent à `MANUAL` (revalidation implicite par l'utilisateur).
4. **Singleton local** — `ProfessionalProfileRepository.findLocalProfile()` (default method sur
   `findAll().stream().findFirst()`), point de couture unique remplaçable plus tard par
   `findByWorkspaceId(workspace)` sans toucher au domaine. Couture future documentée, non implémentée.
5. **IA** — nouvel adaptateur `profile.ai.AiProfileExtractor` suivant la convention maison
   (ChatClient, prompt système FR anti-invention, `.entity()`, wrap `AIAnalysisException`) ;
   contrat `ExtractedProfileProposal` non-JPA avec dates en String parsées de façon défensive ;
   `promptVersion = "profile-extraction-v1"`.

## Migration V4 (`V4__create_professional_profile.sql`)

- `career_professional_profile` (cf. modèle ci-dessus) ;
- filles : `career_profile_skill` (PK composite + uk normalisée), `career_profile_experience`,
  `career_profile_education`, `career_profile_language` (idem skill), toutes `ON DELETE CASCADE`,
  style V1/V2 (`TIMESTAMP(6)`, enums STRING, contraintes nommées).

## Frontière applicative

```text
ProfileController (/profile…)
        ↓
ProfessionalProfileService (@Transactional sur écritures ; IA appelé HORS transaction)
        ↓                                    ↓
ProfessionalProfileRepository          AiProfileExtractor (proposition, jamais persistée)
```

## Routes web

| Route | Rôle |
|---|---|
| GET `/profile` | vue profil fiable ou état vide (créer manuellement / initialiser via CV) |
| GET/POST `/profile/new` puis POST `/profile` | création manuelle |
| GET `/profile/edit` + POST `/profile/update` | édition manuelle (état complet) |
| GET `/profile/initialize` | upload CV |
| POST `/profile/initialize` | extraction IA → rendu direct de la revue (`profile-proposal.html`, badge « Proposition non validée ») |
| POST `/profile/apply` | application explicite → PRG `/profile` + succès |

Distinction visuelle fiable vs proposition : badge dédié sur chaque écran.

## Validation

- Bean validation formulaires (`@Size`, formats dates) ;
- service : normalisation compétences/langues (minuscule, accents retirés, ponctuation réduite à
  `+#.`), rejet des doublons normalisés et des libellés vides ;
- expériences : `end >= start` quand les deux présentes, fin sans début interdite ;
- exceptions métier `InvalidProfileException extends CvAnalyzerException` → page erreur 400.

## Échecs couverts

Profil absent (état vide) ; CV invalide/trop gros (service existant) ; échec IA (page produit,
profil intact — rien n'est écrit avant `/profile/apply`) ; soumission invalide (re-render avec
erreurs) ; proposition vide validée (erreur fonctionnelle).

## Plan de commits

1. `docs(story)` — artefacts Story (story/repository-analysis/implementation-plan) ;
2. `feat(profile)` — domaine + persistance (V4) ;
3. `feat(profile)` — service applicatif + extraction IA ;
4. `feat(profile)` — web (contrôleur, templates, navigation) ;
5. `test(profile)` — tests persistance/service/web/extraction ;
6. `docs` — README + rapports (implementation-report, code-review, engineering-report).

## Tests prévus

- Persistance (`@SpringBootTest` H2) : tables migrées, agrégat + filles, contrainte unicité
  normalisée, remplacement de collections, timestamps.
- Service (unitaire Mockito + intégration) : créer/lire/mettre à jour, sémantique locale,
  déduplication, validations, échec extraction sans altération, application partielle d'une
  proposition, rejets exclus, pas d'écrasement silencieux.
- Web (MockMvc standalone) : état vide, création, édition, revue, application, erreurs.
- Extraction : parsing défensif des dates/nuls (parser pur testé exhaustivement).
