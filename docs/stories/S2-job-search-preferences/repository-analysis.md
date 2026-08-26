# S2 — Analyse du dépôt existant

État vérifié : `origin/main` = `c0b415b` (PR #1 = S1 profil + nettoyage placeholder fusionnés) ;
arbre propre ; migrations V1–V4 présentes ; package `profile` complet.

## Ce qui se réutilise structurellement (pattern S1)

| Élément S1 | Réemploi S2 |
|---|---|
| Module vertical `profile.{domain,persistence,application,web}` | même découpage, nouveau module `search` |
| `ProfessionalProfileRepository.findLocalProfile()` + javadoc couture | miroir : `findActivePreferences()` |
| Migration additive nommée, contraintes `fk_/uk_`, enums STRING | V5 |
| Formulaire `*Form` jakarta + BindingResult re-render + PRG flash | identique |
| View models records (`ProfileViewModels`) + mapping présentation hors contrôleur | identique |
| `InvalidProfileException extends CvAnalyzerException` → page erreur 400 | `InvalidSearchPreferencesException` |
| `GlobalExceptionHandler.retryUrl` (entrée `/profile` ajoutée en S1) | ajouter `/preferences` |
| Sidebar `activePage` clé par page ; placeholder « Bientôt » supprimé en fin de S1 | nouvelle entrée fonctionnelle « Ma recherche », aucun placeholder |
| Tests : `@SpringBootTest @Transactional` H2 MODE=MySQL ; MockMvc standalone avec vue neutre gérant les redirections | mêmes gabarits |
| Normalisation déterministe `ProfileNormalizer` (casse/accents/`+#.`) | **algorithme réutilisé**, persistance distincte |

## Ce qui doit rester indépendant

- Aucune écriture croisée profil ↔ préférences (agrégats séparés, services séparés).
- Les technologies préférées/exclues ne sont PAS des `ProfileSkillEntity` : entités propres,
  sémantique de normalisation partagée uniquement.
- Le module `search` n'importe rien du domaine CRM et rien des fournisseurs (inexistants).

## Enumerations déjà disponibles

- `career.domain.ContractType` {CDI, CDD, ALTERNANCE, STAGE, FREELANCE, INTERIM, OTHER} —
  vocabulaire métier français déjà utilisé par le CRM et les opportunités : réemployé tel quel.
- `career.domain.RemoteMode` est mono-valué côté offre (ONSITE/HYBRID/REMOTE/UNSPECIFIED) :
  ses VALEURS inspirent le vocabulaire des modes de travail acceptés côté préférences, mais la
  forme (ensemble multi-valué) diffère → nouvel enum local `WorkMode` {ONSITE, HYBRID, REMOTE}.

## Conventions de validation

Bean validation sur les forms (@Size, formats) + règles métier dans le service levant l'exception
métier dédiée ; erreurs cross-champ jamais dans le contrôleur.

## Historique DevLog

DevLog reste aligné sur la révision `8402ea6` (pré-fusion S1) : il n'apporte pas encore l'état
post-S1 ; l'analyse ci-dessus provient de l'inspection directe du dépôt mergé.
