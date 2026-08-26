# Engineering Story S1 — Profil Professionnel persistant avec initialisation assistée par CV

Statut : EN COURS.
Branche : `story/professional-profile` (base : `origin/main` `8402ea6`).
Précédent : [Investigation « Profil Utilisateur & Agrégation Autonome d'Offres »](../../investigations/user-profile-autonomous-job-aggregation.md).

## Problème

Career Intelligence n'existe qu'à travers des données transitoires : le texte du CV est envoyé brut
à l'IA à chaque analyse puis jeté. Rien de structuré ne représente durablement « qui est
l'utilisateur professionnellement ». Toute découverte d'offres pertinente (objectif produit du
Developer OS) exige cette fondation.

## Objectif

Le plus petit périmètre utile :

1. l'utilisateur local possède un profil professionnel **persistant** ;
2. il peut le créer/éditer **manuellement** ;
3. il peut l'**initialiser à partir d'un CV** via une extraction structurée ;
4. toute donnée issue du CV/de l'IA reste une **proposition** jusqu'à validation explicite ;
5. le profil survit au redémarrage.

Invariant produit (dur) :

```text
Donnée dérivée CV/IA ≠ ProfessionalProfile fiable, avant validation explicite de l'utilisateur.
```

## Hors périmètre (interdits dans cette Story)

JobSearchPreferences, JobOffer, JobRecommendation, France Travail / Lever / Greenhouse,
planification, polling, matching, ranking, agents de découverte, alertes, authentification,
multi-utilisateur, géocodage.

## Décisions clés (détaillées dans repository-analysis.md et implementation-plan.md)

- deux concepts séparés : le profil (faits) vs les futures préférences (objectif courant) ;
- la proposition d'extraction N'EST PAS persistée : elle vit dans un formulaire de revue
  éditable, comme les résultats d'analyse avant leur sauvegarde (précédent Phase 1 → Phase 2) ;
- traçabilité IA sur le profil (`ai_provider`, `ai_model`, `prompt_version`, date d'assistance),
  même convention que `career_resume_version` ;
- sémantique mono-utilisateur local assumée ; jointure future possible vers un workspace par
  migration sans réécriture du domaine (couture documentée) ;
- compétences avec libellé utilisateur + nom normalisé déterministe (unicité), sans ontologie.

## Livrables

- Migration Flyway V4 (tables `career_professional_profile*`) ;
- entités JPA + repositories conformes aux conventions ;
- service applicatif `ProfessionalProfileService` (frontière use-case) ;
- contrat IA `AiProfileExtractor` (extraction structurée, jamais persistée telle quelle) ;
- contrôleur `/profile` + templates Thymeleaf (état vide, création, édition, initialisation,
  revue de proposition) ;
- tests domaine/application/persistance/web/adaptateur IA ;
- mise à jour README (limites actuelles).
