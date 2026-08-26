# S1 — Engineering report

## Décisions de domaine

- **Deux concepts séparés** (conforme investigation) : ce Story ne livre que le profil = faits
  professionnels fiables ; aucune préférence de recherche n'a été introduite.
- Modèle minimal à consommateurs démontrés : identité (nom, titre, lieu de référence),
  compétences, expériences, formation/certification (un concept typé), langues humaines.
- Compétences et langues portent un **nom normalisé déterministe** (casse/accents/ponctuation,
  `+#.` conservés pour C++, C#, .NET) avec unicité en base : le matching futur pourra comparer
  structurellement sans parser de chaînes présentées. Les libellés utilisateur sont préservés.
- La durée d'expérience est dérivable des dates → non dupliquée.

## Modèle de confiance

```text
CV (upload transitoire) → extraction IA → ExtractedProfileProposal
        → formulaire de revue ÉDITABLE (jamais persisté)
        → validation case par case (appliquer / corriger / écarter)
        → ProfessionalProfile (seule donnée fiable)
```

- l'IA ne modifie jamais le profil après validation ;
- une proposition appliquée est strictement additive sur les listes (dédup normalisée contre
  l'existant) et conditionnelle sur les scalaires (case cochée + valeur non vide) ;
- une édition manuelle requalifie les compétences en `MANUAL` et réinitialise la traçabilité IA
  (l'utilisateur vient de relire tout l'état).

## Cycle de vie d'une proposition

générée (POST `/profile/initialize`) → revue éditable → appliquée partiellement ou intégralement
(POST `/profile/apply`) → écartée implicitement si navigation sans validation. Aucun état caché :
l'écran porte un bandeau « Proposition non validée » distinct du badge « Profil validé ».

## Persistance

`career_professional_profile` (+ skills/experiences/educations/languages), conventions V1/V2
respectées (`TIMESTAMP(6)`, enums STRING, contraintes nommées, cascade). Unicité
`(profile_id, normalized_name)` et `(profile_id, normalized_language)` vérifiée jusqu'en base.

## Frontière IA

Nouvel adaptateur unique `AiProfileExtractor`, convention maison (ChatClient, prompt système FR
avec règles anti-invention, `.entity()`, wrap `AIAnalysisException`), appelé HORS transaction ;
traçabilité `ai_provider`/`ai_model`/`prompt_version="profile-extraction-v1"`/`cv_assisted_at`
posée sur le profil uniquement lors d'une application effective. Dates IA parsées défensivement
(`ProposalDateParser`, ISO / yyyy-MM / année, sinon ignorées).

## UX

4 écrans (`profile`, `profile-form`, `profile-initialize`, `profile-proposal`) dans le design
existant (fragments sidebar/topbar/alerts, classes os-*). États couverts : vide, fiable,
formulaire création/édition, upload, revue, succès flash, erreurs produit via
`GlobalExceptionHandler`.

## Preuves de test

- suite complète : **94 tests, 0 échec** (60 avant, +34 nouveaux) ;
- persistance : migrations, agrégat complet, unicité en base, remplacement, provenance,
  préservation sur échec, régression unicité après édition conservant une compétence ;
- service : create/read/update, sémantique locale, dédup, validations, proposition additive,
  rejets exclus, aucun sauvetage avant validation, soumission vide rejetée ;
- web : état vide, PRG + flash, redirections, revue rendue depuis POST, échec IA sans perte,
  erreur de binding re-rendue ;
- extraction : parsing de dates exhaustif (ISO/mois/année/hors-range/nul).

## Pipeline qualité

- `./mvnw test` : vert (94/94) ; Flyway validé aussi sur MySQL réel au démarrage manuel
  (« Migrating schema to version 4 » puis démarrage OK) ;
- `git diff --check` : propre ; arbre de travail propre après commits.

## Limites restantes

voir implementation-report.md §Limitations (proposition non persistée, identité du profil
recréée à chaque édition manuelle, format des champs date HTML hérité des conventions existantes).

## Couture propriétaire future

`ProfessionalProfileRepository.findLocalProfile()` centralise la sélection mono-utilisateur ;
le passage à N workspaces se fera par migration (ajout `workspace_id` + unicité partielle) et
remplacement de cette seule méthode — ni le domaine, ni les cas d'usage, ni les contrats IA ne
changeraient.

## DevLog effectiveness

- **Fourni** : fraîcheur confirmée du contexte (revision `8402ea6` alignée), diffs récents
  structurants (CRM Google Sheets, services applicatifs, contrôleurs) et deux fichiers sources
  pertinents — utile pour confirmer la zone à modifier sans balayer le dépôt.
- **Non fourni** : contenu des ADR/doc d'architecture, contrats IA détaillés, conventions de
  formulaires/templates/tests, convention d'Engineering Story (inexistante avant ce Story).
- **Inspection directe restrequis** : lecture des ADR, README, migrations, adaptateurs IA,
  formulaires/fragments Thymeleaf, suites de tests — soit l'essentiel de l'analyse.
- **Bilan** : gain marginal sur la vérification de fraîcheur et la cartographie des fichiers
  chauds ; l'effort d'investigation a reposé principalement sur l'inspection directe et sur
  l'artefact d'investigation précédent.
