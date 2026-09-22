# ADR — Job Offer Normalization and Provenance

## Statut

Accepted

## Contexte

ES-017 doit rendre les offres issues de France Travail, Adzuna, Greenhouse et Manual Intake comparables sans
effacer l'incertitude des sources. `JobOffer` est actuellement transient et les adapters construisent déjà le
modèle canonique. Il ne faut donc ni introduire une persistence, ni une déduplication implicite, ni une
enrichissement sémantique prématuré.

## Decision

### Normalisation déterministe

Le vocabulaire partagé des contrats est normalisé par `ContractType.fromCode`. Les alias dont la sémantique est
explicite sont acceptés :

| Valeurs source | Canonique |
|---|---|
| `CDI`, `permanent`, `permanent employment` | `CDI` |
| `CDD`, `fixed-term`, `fixed-term employment` | `CDD` |
| `alternance`, `apprenticeship` | `ALTERNANCE` |
| `stage`, `internship` | `STAGE` |
| `freelance` | `FREELANCE` |
| `interim`, `temporary` | `INTERIM` |

Les valeurs inconnues ou ambiguës restent `null` et leur valeur brute est conservée dans `rawContractCode` ou
`rawContractLabel`. En particulier, `contract` n'est pas assimilé automatiquement à `CDD`.

Les autres dimensions restent représentées par les champs existants lorsqu'une donnée explicite est disponible :

- salaire : montants et `SalaryPeriod` connu uniquement lorsque la période est explicite ;
- mode de travail : `WorkMode` uniquement lorsqu'il est fourni explicitement ;
- lieu : libellé, codes et coordonnées provider lorsqu'ils existent ;
- compétences : compétences explicitement fournies par le provider ou la saisie manuelle.

### Provenance et identité

`providerKey` représente la source d'acquisition qui a fourni l'enregistrement. `providerOfferId` reste l'identité
locale dans cet espace de noms. Le couple est exposé par `JobOffer.providerIdentity()` via `JobOfferIdentity` afin
de rendre l'invariant explicite sans ajouter de champs persistants ou redondants.

`originUrl` représente l'URL de l'annonce acquise lorsqu'elle est connue. Elle ne prouve pas l'éditeur original,
un éventuel intermédiaire ou une URL de candidature distincte. Pour Manual Intake, la provenance originale reste
inconnue sauf indication explicite de l'utilisateur.

## Conséquences

- Les adapters partagent la même conversion déterministe des contrats.
- Les valeurs inconnues restent observables et peuvent être réévaluées ultérieurement.
- L'identité provider-local est prête pour ES-018 sans créer de déduplication cross-source.
- Aucun champ `originalSource`, devise, pays canonique, remote scope ou URL de candidature n'est ajouté sans
  contrat source suffisamment fiable.

## Hors périmètre

- déduplication ou fusion d'offres ;
- extraction NLP ou IA ;
- inférence de pays, remote, séniorité, type de contrat ou provenance ;
- persistence, migration ou changement de schéma ;
- enrichissement des compétences ;
- modèle complet d'URL source/candidature.
