# S2 — Engineering report

## Story Summary

JobSearchPreferences est désormais un concept de domaine de première classe : UN jeu actif,
entièrement saisi par l'utilisateur, représentant « ce que je cherche maintenant », distinct du
profil professionnel (« ce qui est vrai de moi »), persisté relationnellement, exposé par une UX
complète et prêt comme entrée du futur matching déterministe. Aucun fournisseur, aucun scoring,
aucune IA.

## Domain Decisions

- ensemble actif unique (mono-utilisateur local), remplaçable intégralement à chaque édition ;
- distinction éliminatoire / pertinence portée par la FORME des champs (ensembles d'acceptation,
  liste exclue, booléen de mobilité) plutôt que par un type `HARD/SOFT` exposé ;
- télétravail = ensemble de modes acceptés (vide = ouvert à tous), pas un booléen ;
- technologies : deux listes distinctes (recherchées = pertinence, exclues = éliminatoire),
  intersection interdite ;
- mobilité : un seul booléen qui BASCULE la sémantique des zones (éliminatoire → priorité).

## ProfessionalProfile Boundary

Aucune lecture du profil par le module search (vérifié par grep) ; aucune écriture croisée.
Seuls points communs volontaires : algorithme de normalisation partagé (`ProfileNormalizer`) et
pattern architectural (couture mono-utilisateur, PRG, exceptions produit).

## JobSearchPreferences Model

Voir implementation-plan.md §Modèle V1 : rôles visés, lieux, modes acceptés, contrats acceptés,
technologies préférées/exclues, mobilité, salaire minimum (montant/devise/période, hypothèse brut
documentée).

## Required vs Deferred Fields

REQUIRED_NOW : rôles, lieux, modes, contrats, techno préférées/exclues, mobilité (+ salaire
USEFUL_NOW minimal). DEFER : trajet max (géospatial interdit), séniorité cible, temps
plein/partiel, intention libre en texte. WRONG_CONCEPT : aucun retenu.

## Hard Constraints vs Soft Preferences

Sémantiques documentées pour le prochain slice (implementation-plan.md §Contraintes) :
éliminatoire = mode/contrat hors ensemble non vide, lieu hors zones sans mobilité, technologie
exclue présente, salaire affiché sous le minimum ; pertinence = technologies préférées, proximité
d'intitulé, zones comme priorité si mobile.

## Normalization

`ProfileNormalizer` réutilisé tel quel (casse/accents/ponctuation réduite à `+#.`). Unicité
normalisée en base sur chaque liste ; les libellés utilisateur sont préservés à l'affichage et
pour les messages d'erreur.

## Validation Invariants

Soumission vide rejetée ; doublons normalisés rejetés avec libellé incriminé ; intersection
préféré/exclu rejetée en listant les conflits par libellés utilisateur ; salaire > 0 et ≤ borne,
devise 3 lettres sinon défaut EUR, période sinon ANNUAL ; validation intégrale AVANT mutation —
échec ⇒ état antérieur intact (prouvé en test ET en conditions réelles).

## Persistence

V5 : 6 tables (`career_job_search_preferences`, roles, locations, technology avec discriminant
`preference_kind`, work_mode, contract_type), FK cascade nommées, UK normalisées, PK composées
sur les collections d'énumérés, enums STRING, timestamps maison.

## Ownership Seam

`JobSearchPreferencesRepository.findActivePreferences()` miroir exact de la couture profil ;
future migration workspace identique à celle documentée en S1.

## Application Layer

Cas d'usage : `getPreferencesView()` (lecture), `saveFromForm()` (upsert create-or-replace
transactionnel, aligné sur les conventions S1). Contrôleurs minces.

## UX

Deux écrans (vue + formulaire) dans le design existant ; états vide/populé/erreurs/succès flash ;
wording domaine (« écartée », « mieux classée », « Uniquement », « Aucune restriction ») sans
jargon technique ; entrée navigation « Ma recherche » unique, placeholder impossible (garde
automatisé).

## Future Provider Translation

Lacunes connues documentées : FT n'a pas de filtre remote structuré → traduction par mots-clés ou
post-filtrage ; workplaceType ATS se mappe directement depuis WorkMode ; zones libres nécessiteront
un résolveur géo côté fournisseur au slice suivant ; salaire périodique devra être normalisé par
offre (annualisation).

## Future Matching Semantics

Entrée attendue du matching : préférences = critères d'éligibilité + signaux de classement ;
profil = base de qualification. Pas de classe contexte avant que le slice réel ne montre son besoin.

## Tests

20 nouveaux tests (persistance 5, service 9, web/MVC 6 dont garde navigation) + 2 gardes sur la
vue d'ensemble (`HomeOverviewTemplateTest`) ; suite complète **116 tests, 0 échec** (94 avant).

## Manual Validation

Workflow complet validé en conditions réelles (curl sur app lancée, MySQL + migration V5
appliquée) : état vide → création → vue peuplée → édition préremplie → mise à jour → rejet de
conflit 400 sans altération → navigation propre. Inspection visuelle complémentaire : la carte
« Évolution prévue » du dashboard montrait encore le profil en « Prochainement » — corrigé en
« Disponible » avec garde-fou (voir implementation-report.md). Rendu navigateur/mobile non validé
(Chromium indisponible) — limité à la vérification HTTP réelle et à l'inspection des templates.

## Quality Pipeline

`./mvnw test` vert (114/114) ; `git diff --check` propre sur la branche ; arbre propre après
commits.

## DevLog Effectiveness

- **Avant Story** : DevLog restait aligné sur la révision pré-fusion S1 (`8402ea6`) malgré un
  statut « CURRENT » — il n'a fourni ni le Story profil, ni la décision de séparation, ni les
  conventions issues de S1 ; toute l'analyse a reposé sur l'inspection directe du dépôt mergé.
- **Après Story** : requête de contrôle demandée en fin de mission ; tant que la synchronisation
  n'aura pas absorbé les commits post-S1, la qualité attendue reste celle observée (contexte Git
  récent, pas de connaissance de projet profonde).
- Comparaison qualitative avec le Story précédent : inchangée — DevLog utile pour la fraîcheur et
  la cartographie des fichiers chauds, insuffisant pour ADR/décisions/formulaires/tests.

## Known Limitations

- identifiant du jeu de préférences recréé à chaque édition (aucune référence externe aujourd'hui) ;
- rendu navigateur/mobile non vérifié visuellement (environnement sans Chromium) ;
- pas de valeur « soft » nuancée pour le remote (accepter hybride tout en préférant remote) :
  classera également — affinable au slice matching si démontré nécessaire ;
- salaire : comparabilité future limitée aux offres qui affichent un salaire ; annualisation à
  charge du prochain slice.

## Suggested Next Story

S3 — « Slice vertical de découverte : JobOffer canonique + adaptateur France Travail + import
déclenché manuellement + filtrage d'éligibilité déterministe sur profil + préférences ». Un seul
Story qui valide bout-en-bout la chaîne de valeur (fournisseur réel → offres normalisées →
éligibilité expliquée → écran) plutôt qu'une fondation supplémentaire déconnectée.
