# ADR — Persist Career Intelligence analyses and documents

## Statut

Accepté pour la Phase 2.

## Contexte

Les résultats d’analyse, CV améliorés et lettres étaient uniquement présents dans la réponse d’un formulaire
POST. Ils disparaissaient après navigation et n’avaient ni URL stable ni historique.

## Décision

Persister séparément les opportunités, analyses, documents CV, versions de CV et lettres dans MySQL. Gérer le
schéma avec Flyway, conserver les contrats de réponse IA hors de JPA, puis exposer les résultats par un flux
Post/Redirect/Get. Chaque sauvegarde explicite d’un CV crée une nouvelle version.

## Conséquences

- les résultats survivent au rechargement et au redémarrage ;
- le dashboard et l’historique reposent sur des données réelles ;
- la traçabilité minimale du fournisseur, modèle, type de génération et prompt logique est conservée ;
- le schéma devient une responsabilité explicite et versionnée ;
- la suppression doit respecter les relations et éviter les cascades implicites.

## Alternatives rejetées

- session HTTP : ne survit pas au redémarrage et ne fournit pas d’historique fiable ;
- sérialisation de l’intégralité d’une réponse IA dans une colonne opaque : évolution et requêtes difficiles ;
- stockage du PDF original : inutile pour les cas d’usage actuels et plus risqué pour les données personnelles ;
- utilisation directe des entités JPA comme schémas OpenAI : couplage entre fournisseur IA et persistance.

## Compatibilité Developer OS

Les agrégats possèdent leur propre identité. Une relation vers un futur workspace pourra être ajoutée par
migration sans modifier la forme des réponses IA ni les routes de consultation existantes.
