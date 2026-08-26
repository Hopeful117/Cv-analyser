# S1 — Rapport d'implémentation

## Livré

| Élément | Détail |
|---|---|
| Migration | `V4__create_professional_profile.sql` (5 tables, contraintes nommées) |
| Domaine | `SkillOrigin`, `EducationKind`, `ProfileNormalizer` |
| Persistance | `ProfessionalProfileEntity` + 4 entités enfants + `ProfessionalProfileRepository` (`findLocalProfile()`) |
| Application | `ProfessionalProfileService` (getProfileView / saveFromForm / proposeFromCv / applyProposal), `ProposalDateParser`, `ProfileViewModels` |
| IA | `AiProfileExtractor` + contrat `ExtractedProfileProposal` (prompt `profile-extraction-v1`) |
| Web | `ProfileController` (7 routes) + 4 templates + entrée sidebar « Profil » |
| Exception | `InvalidProfileException extends CvAnalyzerException` ; mapping `retryUrl` `/profile` |

## Commits (branche `story/professional-profile`)

1. `dfc6ee7` docs — artefacts Story ;
2. `2a46069` domaine + persistance V4 ;
3. `99f0f50` service applicatif + extraction IA ;
4. `d7a5310` interface web ;
5. `89cec2f` suite de tests ;
6. `d4b5219` correctif remplacement de collections (trouvé en validation manuelle) + tests de régression ;
7. `e98e70c` refactor mapping formulaire vers view models.

## Validation manuelle (application réelle, port 8081, MySQL + OpenAI)

Scénario exécuté au curl sur l'UI réelle :

1. démarrage sans profil → `/profile` affiche l'état vide (« Créez votre profil professionnel ») ;
2. `POST /profile/initialize` avec le CV d'exemple → extraction IA réelle : proposition complète
   rendue avec bandeau « Proposition non validée » (nom, titre, lieu, 8 compétences, 1 expérience,
   3 formations/certifications, 2 langues) ;
3. revue : nom édité (« BROT Ludovic » → « Ludovic BROT »), titre professionnel rejeté (case
   décochée), compétence « Git » rejetée, reste validé → 302 ;
4. `/profile` : badge « Profil validé » + « Initialisé avec l'aide du CV », valeurs éditées/
   acceptées présentes, rejets absents ;
5. redémarrage de l'application → profil intact (persistance vérifiée) ;
6. deuxième proposition : « HTML » existant non dupliqué, « Docker » ajouté, titre accepté à
   posteriori, langue ajoutée ;
7. édition manuelle complète (lieu modifié, état de compétences soumis) → 302, valeurs en base,
   traçabilité IA réinitialisée (`ai_provider`/`cv_assisted_at` NULL).

Le bug n°1 ci-dessous a été découvert et corrigé pendant cette validation.

## Bugs trouvés et corrigés

1. **Remplacement de collections non fiable** : clear+flush+réinsertion et delete bulk dérivé
   produisaient des ordres SQL incorrects ou inopérants (insert avant delete / violation d'unicité).
   Correctif : suppression ORM de l'agrégat puis recréation, dans la même transaction ; deux tests
   de régression ajoutés.
2. **Soumission manuelle vide** créait un profil fantôme : rejet fonctionnel explicite ajouté.
3. **Validation non cascade** sur les listes du formulaire de proposition : `List<@Valid …>`
   ajouté.

## Limitations connues (assumées V1)

- la proposition vit dans l'écran de revue : quitter sans valider impose de relancer l'extraction ;
- l'identifiant du profil change à chaque édition manuelle (recréation d'agrégat) — aucune table
  ne le référence aujourd'hui ; à revoir si une FK apparaît ;
- les champs date HTML reçoivent un format localisé (précédent commun aux formulaires existants) :
  certains navigateurs peuvent afficher une date extraite comme vide, la valeur reste éditable.
