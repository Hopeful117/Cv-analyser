# S1 — Code review

Revue effectuée par l'auteur sur le diff complet `origin/main..HEAD`, selon la checklist demandée.

| Point de vigilance | Verdict | Justification |
|---|---|---|
| Auto-enrichissement accidentel du profil | OK | aucun chemin d'écriture hors POST explicites `/profile`, `/profile/update`, `/profile/apply` ; pas de planificateur |
| Sorties IA traitées comme faits | OK | `ExtractedProfileProposal` jamais persistée ; passage exclusif par formulaire de revue ; seules cases cochées appliquées ; traçabilité posée à l'application |
| Fuite profil/préférences | OK | aucun champ de recherche ; libellé « Lieu de référence » + aide explicite (« vos souhaits de mobilité seront gérés séparément ») |
| Sur-modélisation | OK | pas de séniorité stockée (dérivable), pas de coordonnées, formation/certification = un concept typé |
| Logique métier dans les contrôleurs | corrigé | mapping formulaire↔vue déplacé vers `ProfileViewModels` (commit `e98e70c`) ; contrôleurs = routing + PRG |
| Double flux de parsing CV | OK | réutilisation stricte de `UploadValidationService` + `PdfParserService` |
| Persistance du PDF | OK | aucune écriture du fichier ; texte transitoire |
| Validation faible | renforcée | doublons normalisés rejetés, plages de dates invalides rejetées, soumission vide rejetée, cascade `@Valid` sur listes |
| Sémantique destructive | documentée | édition manuelle = remplacement complet annoncé (l'utilisateur voit l'état final) ; proposition = strictement additive |
| Hypothèses multi-utilisateurs cachées | OK | mono-utilisateur centralisé dans `findLocalProfile()` (couture unique) |
| Abstractions spéculatives | nettoyées | repositories enfants supprimés après pivot vers delete+recreate ; pas de couche inutilisée |

Points discutés et assumés :

- `saveFromForm` qualifie toutes les compétences resoumises en `MANUAL` : après relecture complète
  par l'utilisateur, c'est la vérité la plus honnête ; la provenance CV est historisée via la
  traçabilité jusqu'à la prochaine édition manuelle ;
- `repository.delete + flush + recreate` au lieu de mises à jour différentielles : déterministe,
  testé H2/MySQL, coût négligeable au volume local ; identité du profil instable entre éditions
  (documenté, sans consommateur externe aujourd'hui).
