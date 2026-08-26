# Engineering Story S2 — Préférences de recherche d'emploi actives

Statut : EN COURS.
Branche : `story/job-search-preferences` (base : `origin/main` `c0b415b`, merge PR #1 de S1).
Précédents : S1 (`docs/stories/S1-professional-profile/`),
[investigation](../../investigations/user-profile-autonomous-job-aggregation.md).

## Problème

Le profil professionnel dit « qui est l'utilisateur » ; rien ne dit encore « ce qu'il cherche
maintenant ». La découverte autonome d'offres et le matching déterministe ont besoin de cet objectif
courant comme entrée dédiée.

## Invariant produit

```text
ProfessionalProfile = faits professionnels (qui je suis)
JobSearchPreferences = objectif courant de recherche (ce que je veux)

Un fait ne devient jamais une préférence silencieusement.
Une préférence ne modifie jamais le profil.
```

Aucune inférence depuis le CV, le profil, l'IA, le CRM ou les résultats fournisseurs : les
préférences sont 100 % saisies et validées par l'utilisateur.

## Décisions clés (justifiées dans implementation-plan.md)

- UN seul jeu de préférences actif (mono-utilisateur local, même couture que le profil) ;
- distinction explicite contraintes ÉLIMINATOIRES vs critères de PERTINENCE, exprimée en langage
  domaine dans l'UX (pas de jargon HARD/SOFT) ;
- télétravail modélisé comme ensemble des modes acceptés (pas un booléen) ;
- réutilisation des sémantiques de normalisation du profil (algorithme partagé, persistance séparée) ;
- réutilisation de l'enum existant `ContractType` ;
- zéro IA, zéro fournisseur, zéro matching dans ce Story.

## Hors périmètre

Fournisseurs d'offres, JobOffer, matching/scoring, IA, multi-recherches nommées, géocodage,
multi-utilisateur, JobSearchContext (agrégat prématuré — couture documentée seulement).
