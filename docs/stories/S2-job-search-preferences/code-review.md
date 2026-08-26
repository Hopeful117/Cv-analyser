# S2 — Code review

Revue auteur sur l'intégralité du diff `origin/main..HEAD`.

| Point de vigilance | Verdict | Justification |
|---|---|---|
| Préférences qui fuient vers ProfessionalProfile | OK | aucun import croisé ; modules indépendants |
| Faits du profil copiés automatiquement en préférences | OK | le service ne dépend que du repository des préférences ; aucune lecture profil/CV/IA/CRM |
| Champs spécifiques à un fournisseur | OK | vocabulaire métier (modes, contrats français) ; rien de FT/LinkedIn/Indeed |
| Moteur de règles générique prématuré | OK | concepts explicites uniquement (ensembles acceptés, listes étiquetées, booléen de mobilité) |
| Abstraction JobSearchContext prématurée | OK | non créée ; couture documentée au rapport final |
| Inférence IA | OK | zéro appel IA dans le module |
| Normalisation dupliquée | OK | `ProfileNormalizer` réutilisé (algorithme partagé, persistance séparée) |
| Persistance JSON opaque | OK | tables relationnelles + `@ElementCollection` typées |
| Multi-utilisateur accidentel | OK | sélection centralisée dans `findActivePreferences()` |
| Recherches multiples non demandées | OK | un seul agrégat actif |
| Logique métier dans les contrôleurs | OK | contrôleurs = routing/PRG ; invariants dans le service |
| Contraintes base manquantes | OK | UK normalisées (rôles, lieux, technologies par kind), PK composées pour les ensembles d'énumérés |
| Entrées de navigation dupliquées / placeholder résiduel | OK + test-garde | `navigationHasSingleFunctionalSearchEntryAndNoPlaceholder` verrouille la régression constatée sur S1 |
| Tests miroirs de l'implémentation | acceptable | assertions orientées comportement/messages produit ; le garde navigation est volontairement structurel |

Décisions discutées et assumées :

- `ContractType` (career.domain) réimporté par le module search : vocabulaire commun assumé plutôt
  que duplication d'énuméré ;
- remplacement = suppression ORM de l'agrégat puis recréation (leçon S1), l'identifiant des
  préférences changeant à chaque édition — aucune référence externe n'existe aujourd'hui ;
- devise/période par défaut (EUR/ANNUAL) si montant fourni sans précision : explicite côté service,
  documenté côté UX (« facultatif, brut »).
