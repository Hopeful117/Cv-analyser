# S1 — Analyse du dépôt existant

Constat validé contre le HEAD courant (`8402ea6`) : toutes les conclusions de l'investigation
restent vraies. Aucune contradiction détectée. Points vérifiés : aucun modèle User/Profile
(migrations V1–V3 et `src/main/java`), mode mono-utilisateur local, CV transitoire, JPA + Flyway +
MySQL, monolithe modulaire.

## 1. Flux CV actuel (upload → parse → IA)

- `UploadValidationService.requirePdf(MultipartFile)` : PDF obligatoire, ≤ 5 Mo, extension +
  content-type ; messages d'erreur produit en français.
- `PdfParserService.extractText(MultipartFile)` : PDFBox (`Loader.loadPDF` + `PDFTextStripper`),
  texte brut. Le PDF original n'est **jamais persisté** (README) — respecter cette posture.
- `CareerWorkspaceService.analyze(...)` : orchestration transactionnelle unique
  (extraction → analyse IA → génération IA → sauvegarde opportunité + analyse + version CV).
- Le texte extrait est utilisé puis abandonné : aucune structure persistée.

Conséquence S1 : l'initialisation du profil doit repartir d'un upload de CV frais ; on ne peut pas
supposer qu'un PDF antérieur existe encore. Réutiliser `UploadValidationService` + 
`PdfParserService` plutôt que créer un second pipeline.

## 2. Contrats IA

- Adaptateurs : `AiResumeAnalyzer`, `AiResumeGenerator`, `AiCoverLetterGenerator`.
- Convention : `ChatClient.Builder` injecté et `.build()` dans le constructeur ; prompt système
  français en text block avec règles anti-invention explicites ; `.user(u -> u.text(...).param(...))`
  ; `.call().entity(ContratJava.class)` ; exceptions wrappées dans `AIAnalysisException`.
- Contrats = classes/records Java simples **non JPA** (`model.ResumeAnalysis`,
  `model.GeneratedResume`), mappés vers entités par la couche applicative.
- Traçabilité persistée : colonnes `ai_provider` / `ai_model` / `prompt_version` /
  `generated_at` (V1) + `AnalysisNature.AI_ESTIMATE`.

## 3. Persistance

- MySQL production (`jdbc:mysql://localhost:3306/cv_analyzer`), H2 MODE=MySQL pour les tests
  (`ddl-auto=none`, Flyway activé) ; production `ddl-auto=validate`.
- Migrations : `src/main/resources/db/migration/V{n}__{description}.sql`, style :
  `BIGINT AUTO_INCREMENT`, `TIMESTAMP(6)`, enums en `VARCHAR(n) NOT NULL`, collections ordonnées
  en tables filles `(parent_id, item_order)` avec `ON DELETE CASCADE`, contraintes nommées
  (`fk_*`, `uk_*`, `idx_*`), préfixe `career_`.
- Entités : `@Entity @Table @Getter @Setter @NoArgsConstructor` (Lombok), `GenerationType.IDENTITY`,
  `@PrePersist/@PreUpdate` pour timestamps, repositories Spring Data minces.
- Pas de JSON opaque dans le schéma actuel.

## 4. Frontière application

- Contrôleurs minces (`WebInterfaceController/*`) déléguant à des services applicatifs
  (`career.application.CareerWorkspaceService`, `ApplicationCrmService`).
- View models immuables (records) dans `CareerViewModels` / `CrmViewModels` — les templates ne
  manipulent jamais les entités JPA.
- Formulaires : classes `*Form` avec annotations jakarta (`@NotBlank`, `@Size`, `@Email`),
  binding `@Valid @ModelAttribute` + `BindingResult` + re-render du template en cas d'erreur,
  flash `successMessage` après redirection (PRG).
- Exceptions métier : sous-types de `CvAnalyzerException` → page `error` 400 par
  `GlobalExceptionHandler` (avec `retryUrl`) ; `EntityNotFoundException` → 404.

## 5. Thymeleaf

- Layout par fragments : `fragments/head`, `fragments/sidebar(activePage)`, `fragments/topbar`,
  `fragments/alerts`. Classes utilitaires : `page-stack`, `os-card`, `os-grid os-grid-2`,
  `os-label/os-input/os-select/os-textarea`, `field-error`, `eyebrow`, `os-badge`, boutons
  `os-button(-primary|-secondary|-danger)`.
- Navigation sidebar avec clé `activePage` par page.
- Précédent POST rendant directement une page de résultat (sans PRG intermédiaire) :
  `/analyze` → `result-analyzer.html`. Ce pattern convient à l'écran de revue de proposition.

## 6. Transactions

- `@Transactional` sur les méthodes de cas d'usage applicatifs (lecture : `readOnly = true`).
- Appels IA synchrones DANS la transaction côté `analyze()` existant. Pour S1 : extraire via l'IA
  AVANT d'ouvrir la transaction d'écriture (aucune écriture pendant un appel distant).

## 7. Tests

- Persistance/intégration : `@SpringBootTest @Transactional` sur H2 (ex.
  `CareerPersistenceIntegrationTest`, `ApplicationCrmIntegrationTest`).
- Web : MockMvc standalone avec service mocké + `GlobalExceptionHandler`
  (`CareerWorkspaceMvcTest`, `GoogleSheetsConsultationMvcTest`).
- Unitaires purs pour mappers/cas d'usage Google Sheets.
- Commande qualité : `mvn test` (README) ; build complet via wrapper `./mvnw`.

## 8. Réutilisation sûre

| Composant | Réutilisation S1 |
|---|---|
| `UploadValidationService.requirePdf` | oui, telle quelle |
| `PdfParserService.extractText` | oui, telle quelle |
| Convention ChatClient | oui, nouvel adaptateur dédié |
| `GlobalExceptionHandler` + `error.html` | oui |
| Fragments/classes CSS | oui |

Aucun service existant n'a besoin de modification.

## 9. Extraction déterministe ou IA ?

L'extraction déterministe actuelle (`JobKeyWordExtractor`) ne couvre que ~11 mots-clés techniques
côté offre, pas les compétences/expériences/formation/langues d'un CV libre. Une extraction IA
structurée est donc nécessaire ET acceptable (mission §10), à condition de :

- produire des données structurées (contrat Java typé) ;
- rester une proposition non persistée avant validation ;
- respecter les règles anti-invention maison (précédent prompt `AiResumeGenerator`).
