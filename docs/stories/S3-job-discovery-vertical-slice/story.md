# Story S3 — First Real Job Discovery Vertical Slice

## Objectif

France Travail → recherche authentifiée réelle → adaptateur fournisseur → JobOffer canonique → éligibilité déterministe → résultats visibles par l'utilisateur.

## Question produit

Comment l'utilisateur peut-il rechercher de vraies offres d'emploi France Travail, voir leur éligibilité déterministe par rapport à son profil et ses préférences, sans persistance ni autonomie ?

## Portée

### IN SCOPE

- Configuration France Travail
- Acquisition/cache token OAuth
- Recherche live bornée
- DTOs fournisseur
- Mapping fournisseur
- JobOffer canonique
- Éligibilité déterministe
- Résultat tri-state
- Page résultats simple
- États prérequis/vide/erreur/chargement/résultats
- Attribution source
- Tests
- Validation manuelle API réelle

### OUT OF SCOPE

- Table JobOffer persistante
- Historique offres
- Recherche planifiée/arrière-plan
- Recherche autonome
- Multi-fournisseur
- Pagination au-delà du premier lot
- Moteur géographique avancé
- Classement offres
- Pourcentage correspondance
- Embeddings
- Qualification LLM
- Notifications
- Alertes email
- Création automatique Opportunity
- Création candidature
- Agent recommandation
- Intégration ROME/ROMEO supplémentaire
- Moteur de règles générique
- Framework agrégateur générique

## Résultat attendu

1. L'écran exige un profil et des préférences existants
2. L'utilisateur choisit UN rôle parmi `targetRoles`
3. L'utilisateur clique « Rechercher maintenant »
4. Une seule requête France Travail récupère les 50 offres les plus récentes
5. Chaque offre est évaluée déterministement
6. Les résultats sont affichés avec statut + raisons
7. Rien n'est sauvegardé

## Critères d'acceptation

- [ ] Authentification OAuth2 fonctionne avec les credentials existants
- [ ] Token caché en mémoire avec expiration
- [ ] Recherche bornée à 50 offres
- [ ] JobOffer canonique sans dépendance France Travail
- [ ] Évaluation tri-state (ELIGIBLE/INELIGIBLE/REVIEW_REQUIRED)
- [ ] Raisons typées et explicatives
- [ ] Page résultats avec états prérequis/erreur/vide/résultats
- [ ] Aucune persistance
- [ ] Aucun Opportunity créé
- [ ] Application démarre sans credentials
- [ ] Tests unitaires et d'intégration

## Risques

| Risque | Protection |
|--------|------------|
| Secret/token exposé | Config env, logs redactés, adapter désactivé sans credentials |
| Appel bloquant | Timeouts connexion/lecture, taille réponse bornée |
| Quota inconnu | Un appel manuel borné, pas de retry aveugle |
| Token expiré | Cache mémoire fondé sur expires_in |
| Mapping contrat inconnu | Unknown → REVIEW_REQUIRED |
| Faux rejet remote/salaire | Tri-state, parse strict, unknown conservé |
| Confusion Opportunity | Aucune écriture CRM |

## Gates

### Techniques — À SATISFAIRE

1. Credentials disponibles et fonctionnels
2. Authentification OAuth2 réussie
3. Recherche temps réel exécutée
4. Payload réel inspecté
5. Pagination Content-Range fonctionnelle

### Non techniques — NON BLOQUANTS

1. RGPD/géographie (avant persistance)
2. Article 5.3 juridique (avant persistance)
3. Monétisation (avant monétisation)
