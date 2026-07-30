# ADR — Career Intelligence owns application data and publishes a Google Sheets projection

## Statut

Accepté pour la Phase 3.

## Contexte

Le suivi historique vivait dans un tableur partagé. Career Intelligence a désormais besoin de règles
métier, de relations vers les CV et analyses, d’un historique de statut et d’un fonctionnement fiable même
si Google est indisponible. Une synchronisation bidirectionnelle créerait des conflits sans propriétaire
clair.

## Décision

MySQL est l’unique source de vérité des entreprises, opportunités et candidatures. Google Sheets reçoit une
projection unidirectionnelle destinée à la consultation des accompagnateurs.

- une écriture métier est validée et commitée avant tout appel Google ;
- un `@TransactionalEventListener(AFTER_COMMIT)` déclenche la projection ;
- l’état technique est conservé séparément dans `career_external_projection` ;
- `Career Intelligence ID` est l’identité stable d’upsert ;
- une lecture Google est autorisée uniquement pour valider, résoudre les en-têtes, retrouver un identifiant,
  détecter une divergence ou préparer une reconstruction ;
- une divergence ne modifie jamais automatiquement MySQL ;
- les erreurs Google sont visibles, réessayables et n’annulent pas la transaction métier ;
- la reconstruction met à jour ou ajoute, sans supprimer les lignes inconnues ;
- aucune transaction distribuée et aucun broker ne sont introduits.

## Sécurité

L’adaptateur utilise Application Default Credentials et le scope Sheets seulement. Le chemin et le contenu
de la clé ne sont pas persistés ou journalisés. Les notes privées, documents complets, prompts et secrets ne
font pas partie du DTO de projection. Les valeurs textuelles sont protégées contre l’injection de formule.

## Conséquences

Career Intelligence continue de fonctionner hors ligne vis-à-vis de Google. La projection devient
éventuellement cohérente et peut momentanément être en `PENDING` ou `FAILED`. L’exécution dans le processus
ne garantit pas une reprise après arrêt brutal entre commit et traitement ; une file durable pourra être
introduite ultérieurement sans changer le domaine ni le port.

## Alternatives rejetées

- Google Sheets comme base principale : relations, historique et transactions insuffisants.
- Synchronisation bidirectionnelle : conflits et propriété métier ambiguë.
- Appel Google dans la transaction JPA : latence et rollback métier sur panne externe.
- OAuth utilisateur : disproportionné pour le mode personnel actuel.
- Broker de messages : complexité prématurée pour cette phase.

## Évolutions possibles

Outbox transactionnelle, traitements planifiés, API d’administration authentifiée, multi-workspace et
journal détaillé de divergence, sans remettre en cause la propriété MySQL.
