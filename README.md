# Career Intelligence

Career Intelligence est le module carrière personnel du Developer OS. Il analyse un CV face à une offre,
conserve l’opportunité et le diagnostic, produit un CV amélioré versionnable, sauvegarde les lettres de
motivation et permet de retrouver ces contenus depuis un dashboard.

L’application fonctionne actuellement en mode local mono-utilisateur. Elle n’envoie aucune candidature.
Les scores et recommandations sont des estimations produites avec l’aide d’une IA : ils doivent être vérifiés
par l’utilisateur et ne constituent pas une certification ATS.

## Fonctionnalités disponibles

- analyse d’un CV PDF par rapport à une offre collée ou accessible par URL ;
- détection de la langue de l’offre et génération du CV dans cette langue ;
- persistance des opportunités, analyses, recommandations et mots-clés ;
- historique paginé et pages de résultat accessibles par URL stable ;
- CV améliorés avec versions IA et éditions utilisateur ;
- export PDF monopage depuis une version sauvegardée ;
- génération, sauvegarde et édition d’une lettre de motivation ;
- dashboard alimenté par les données réelles ;
- CRM personnel : entreprises, opportunités, candidatures, statuts, priorités et relances ;
- historique des changements de statut et association aux CV, lettres et analyses ;
- import contrôlé d’un historique `.xlsx` avec prévisualisation ;
- projection unidirectionnelle et optionnelle des candidatures vers Google Sheets ;
- simulation d’entretien expérimentale.

## Stack

- Java 21, Spring Boot MVC 4, Thymeleaf ;
- Spring Data JPA, MySQL ;
- Flyway pour les migrations ;
- Spring AI et OpenAI (`gpt-4o-mini` par défaut) ;
- PDFBox et Jsoup ;
- Apache POI pour l’import `.xlsx` ;
- API Google Sheets avec Application Default Credentials pour la projection ;
- Maven.

## Configuration locale

Créer une base vide :

```sql
CREATE DATABASE cv_analyzer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Définir les variables d’environnement, sans les enregistrer dans Git :

```bash
export OPENAI_API_KEY="..."
export MYSQL_PASSWORD="..."
export MYSQL_USER="root" # facultatif
export CAREER_GOOGLE_SHEETS_ENABLED="false"
```

La connexion par défaut est `jdbc:mysql://localhost:3306/cv_analyzer`. Flyway applique automatiquement les
migrations de `src/main/resources/db/migration`, puis Hibernate valide le schéma (`ddl-auto=validate`).
Une base héritée contenant uniquement les tables d’entretien est adoptée par Flyway à la version `0`, puis
les migrations Career Intelligence sont appliquées. Sauvegardez toujours la base avant la première adoption.
La migration V3 rétablit de façon idempotente les tables d’entretien manquantes sur les installations
partiellement baselinées ; elle ne supprime ni ne recrée une table déjà présente.

Si une ancienne base a été créée avec `create-drop`, sauvegardez les données utiles puis repartez d’une base
vide : l’ancien schéma n’était pas versionné et ne contient pas de données Career Workspace compatibles.

Le lancement par `CvAnalyzerApplication` charge aussi un fichier `.env` local s’il existe. Ce fichier est
ignoré par Git. Pour activer la projection :

```env
CAREER_GOOGLE_SHEETS_ENABLED=true
CAREER_GOOGLE_SHEETS_SPREADSHEET_ID=identifiant-du-tableur
CAREER_GOOGLE_SHEETS_APPLICATIONS_SHEET=Candidatures
CAREER_GOOGLE_SHEETS_DASHBOARD_SHEET="Tableau de bord"
GOOGLE_APPLICATION_CREDENTIALS=/chemin/hors-repository/compte-service.json
```

La bibliothèque Google utilise Application Default Credentials et le seul scope
`https://www.googleapis.com/auth/spreadsheets`. Le JSON n’est ni lu manuellement par l’application, ni
stocké en base, ni journalisé. L’application démarre sans credentials lorsque l’intégration est désactivée.
Même si l’intégration est activée par erreur sans ADC disponibles, leur résolution est différée jusqu’au
premier test ou à la première synchronisation : le démarrage reste possible et l’échec est exposé comme
une erreur fonctionnelle réessayable.
En développement local, `dotenv-java` expose les valeurs du `.env` comme propriétés Java. Le client tente
d’abord les ADC standards, puis utilise explicitement la propriété `GOOGLE_APPLICATION_CREDENTIALS` comme
repli local. Le chemin reste configurable et le contenu du fichier n’est jamais journalisé.

## Démarrage

```bash
mvn test
mvn spring-boot:run
```

Routes principales :

- `/` : dashboard ;
- `/analyze` : nouvelle analyse ;
- `/analyses` : historique ;
- `/applications` : CRM et historique des candidatures ;
- `/applications/import` : prévisualisation/import Excel ;
- `/settings/google-sheets` : test, nouvelle tentative et reconstruction de la projection ;
- `/generator` : nouvelle lettre ;
- `/interview/start` : entretien expérimental.

## Données et confidentialité

Les textes extraits des CV et offres, les résultats IA et les documents édités sont sauvegardés dans la base
locale. Les contenus nécessaires à la génération sont transmis au fournisseur IA configuré. Le PDF original
n’est pas conservé. Les suppressions individuelles nécessitent une confirmation dans l’interface.

## Projection Google Sheets

MySQL est l’unique source de vérité. Après le commit d’une candidature, un événement tente une projection
Google Sheets. Un échec laisse la sauvegarde métier intacte et crée un état `FAILED` consultable et
réessayable. `Career Intelligence ID` fournit l’identité stable : un second envoi met à jour la même ligne.
Les colonnes sont résolues par leur en-tête normalisé, jamais par une lettre fixe. Les notes privées, le
contenu des CV/lettres et les détails IA ne sont jamais projetés.

Depuis `/settings/google-sheets` :

1. tester la connexion et les colonnes obligatoires ;
2. relancer les projections en erreur ;
3. reconstruire par mise à jour ou ajout, sans supprimer les lignes inconnues.

La reconstruction ne touche jamais l’onglet Tableau de bord. `Jours` reste une formule générée à partir de
la colonne Date candidature. Les textes commençant par `=`, `+`, `-` ou `@` sont échappés.

L’import Excel est volontairement manuel : sélection `.xlsx`, prévisualisation, puis confirmation. Les
formules et la colonne `Jours` ne deviennent pas des données métier ; les doublons ambigus sont ignorés et
signalés.

Pour tester la connexion localement, chargez les variables sans afficher leur valeur, démarrez
l’application puis utilisez le bouton « Tester la connexion ». Pour une clé compromise : désactivez-la
dans Google Cloud, créez une nouvelle clé pour le même compte de service, remplacez uniquement le fichier
hors dépôt, puis redémarrez. Ne commitez jamais une clé.

## Limites actuelles

- pas d’authentification ni de séparation multi-utilisateur ;
- export CV limité à une page et service PDF encore monolithique ;
- lettres non versionnées (l’édition conserve le document, mais pas chaque révision) ;
- scraping générique, sans prise en charge spécialisée des plateformes ;
- entretien expérimental et modèle de persistance encore fragile ;
- import prévisualisé conservé en mémoire pendant une heure (pas de reprise après redémarrage) ;
- projection exécutée dans le processus applicatif, sans file durable ;
- pas de profil professionnel, d’authentification ou de CRM multi-utilisateur.

Voir [l’architecture du Career Workspace](docs/architecture/career-workspace.md) et
[la décision de persistance](docs/adr/persist-career-intelligence-analyses-and-documents.md).
La projection CRM est documentée dans
[l’ADR Google Sheets](docs/adr/career-intelligence-owns-application-data-and-publishes-google-sheets-projection.md).
