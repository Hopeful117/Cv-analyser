# S2 — Rapport d'implémentation

## Livré

| Élément | Détail |
|---|---|
| Migration | `V5__create_job_search_preferences.sql` (6 tables, contraintes nommées) |
| Domaine | `WorkMode`, `TechnologyPreference`, `SalaryPeriod`, réutilise `ContractType` et `ProfileNormalizer` |
| Persistance | `JobSearchPreferencesEntity` + 3 entités filles + `@ElementCollection` pour les ensembles d'énumérés + `findActivePreferences()` |
| Application | `JobSearchPreferencesService` (getPreferencesView / saveFromForm), `SearchPreferencesViewModels` (vue + mapping formulaire) |
| Web | `JobSearchPreferencesController` (5 routes) + `preferences.html` / `preferences-form.html` + entrée sidebar « Ma recherche » |
| Exception | `InvalidSearchPreferencesException` ; entrée `/preferences` dans `retryUrl` |

## Commits (`story/job-search-preferences`)

1. `a514c89` docs — artefacts de conception ;
2. `dee03b2` domaine + persistance V5 (+ service applicatif) ;
3. `477df83` interface web ;
4. `565af5e` suite de tests ;
5. `2c1ee37` correctifs issus de la validation manuelle.

## Validation manuelle (application réelle, port 8082, MySQL)

Scénario exécuté au curl sur l'UI réelle :

1. **état vide** : `/preferences` affiche « Définissez votre recherche » avec CTA ;
2. **création** : POST du formulaire complet (rôles, lieux, techno préférée/exclue,
   modes REMOTE+HYBRID, contrat CDI, mobilité cochée, salaire 42 000) → PRG 302 ;
3. **vue peuplée** : badge « Recherche active », toutes les sections présentes
   (« Hybride, Télétravail », « CDI », « 42 000 EUR / an », libellés de mobilité différenciés) ;
4. **édition** : formulaire prérempli fidèlement (rôles accentués échappés correctement,
   technologies multi-lignes, salaire, cases cochées) ;
5. **mise à jour** : remplacement complet vérifié (« Développeur Java Senior », Lyon ajouté,
   Docker ajouté, COBOL retiré car non resoumis, 50 000 EUR) ;
6. **conflit rejeté en conditions réelles** : Spring recherché + java exclu → page erreur 400
   avec message listant le libellé ; état antérieur intact (50 000 toujours affiché) ;
7. **navigation** : une seule entrée fonctionnelle « Ma recherche », aucun placeholder
   « Bientôt » (vérifié aussi par test-garde automatisé).

Non validé : rendu navigateur/mobile (Chromium indisponible dans l'environnement) — la validation
a été faite au niveau HTTP sur l'application réelle, pas visuellement.

## Bugs trouvés pendant la validation manuelle

1. la vue référençait des champs supprimés lors d'un refactor (`workModeLabels`) → écran
   `/preferences` en erreur ; remplacés par des accesseurs de présentation dérivés
   (`workModeDisplay()`, `contractTypeDisplay()`) ;
2. l'action du formulaire testait `preferences.id` absent du modèle d'édition → bascule sur
   `preferencesId` fourni par le contrôleur.
