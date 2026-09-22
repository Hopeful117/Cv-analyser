# Investigation — France Travail Selected Offer Retention

## Status

Targeted documentary investigation only. No production code, S3 change, migration, synchronization
design, or Engineering Story was created.

Decision: **NEED_OFFICIAL_CLARIFICATION**.

The governing licence is fully retrievable and contains concrete reuse, completeness,
synchronization, deletion/anonymization, attribution, and personal-data clauses. It contains no
private/personal-use exemption and no user-selected-record exemption. However, it does not define
whether a single-user CRM record used after an offer has been selected and an application is being
tracked remains a reuse “ayant pour objet le rapprochement de l’offre et de la demande d’emploi.”
That classification determines whether the proposed minimal S4 directly conflicts with Articles
5.2 and 5.3.

Until France Travail answers that scope question, the proposed minimal automatic snapshot is
**AMBIGUOUS** and must not become an Engineering Story.

This artifact resolves the licence gate raised by
`docs/investigations/discovered-job-offer-to-opportunity.md` as far as published authoritative
evidence permits. It does not alter that investigation's technical architecture.

## Question

Under the France Travail terms applicable to the Offres d'emploi API, may an individual user
explicitly select one offer found through a live API search and retain a minimal snapshot in a
private personal CRM solely to track their own job-search/application activity?

This is not:

- a public job board;
- public redistribution;
- catalogue mirroring;
- bulk or autonomous collection;
- resale of offers;
- a commercial employer/contact prospect database.

The legal-certainty classifications used here are:

- `CLEARLY_PERMITTED`;
- `PERMITTED_WITH_OBLIGATIONS`;
- `CLEARLY_PROHIBITED`;
- `AMBIGUOUS`;
- `NOT_VERIFIED`.

Absence of an express prohibition is not treated as permission.

Separate evidence findings such as `NO_DISTINCTION_FOUND`, `NO_EVIDENCE`, and
`LIKELY_BUT_NOT_PROVEN`, and product gates such as `NEED_OFFICIAL_CLARIFICATION`, are not licence
status classifications.

## Intended S4 Use Case

```text
live France Travail search
        -> transient canonical JobOffer
        -> explicit selection by one user
        -> server refetch by provider ID
        -> minimal durable CRM snapshot
        -> Opportunity(DRAFT) + Application(NOT_CONTACTED)
```

Automatically retained provider fields would be limited to provider identity, source URL, title,
company, location, contract, salary display text, provider publication/update timestamps, and fetch
timestamp. Description, contact details, competencies, ROME, HTML, eligibility, profile, and search
preferences would not be retained.

The durable record would be visible only in that user's private application-tracking CRM. It would
not expose an offer feed, catalogue, download, or reusable provider dataset.

## Governing Documents

### Specific offers licence

| Item | Evidence |
|---|---|
| exact name | **Licence de réutilisation de la base de données des offres d’emploi de France Travail** |
| portal page title | `3.1. Licence Offres d'emploi` |
| public page | https://francetravail.io/produits-partages/documentation/conditions-dutilisation-api/licence-offres-emploi |
| official content endpoint used for this investigation | https://francetravail.io/api-peio/v2/pages/page?slug=produits-partages%2Fdocumentation%2Fconditions-dutilisation-api%2Flicence-offres-emploi |
| publication status | official page JSON: `statut: publiee` |
| version/date | no licence version identifier or document date is published in the retrieved document; `3.1` is page numbering, not proven version metadata |
| retrieved | 2026-08-27 |

Separate technical metadata must not be mistaken for the licence version: product technical sheet
version `2`, embedded documentation version `24.0` (modified 2026-03-20), and OpenAPI API version
`2.01`.

The governing licence was fully retrieved from France Travail's own page-content API. Therefore
`GOVERNING_LICENCE_NOT_FULLY_RETRIEVABLE` does not apply.

### Applicability to API 84

| Source | URL | Document/page | Section | Exact relevant wording | Interpretation | Confidence |
|---|---|---|---|---|---|---|
| France Travail API portal | https://francetravail.io/produits-partages/catalogue/offres-emploi | Offres d'emploi, API page ID 84 | Conditions d'utilisation | « licence de réutilisation de la base de données des offres d’emploi de France Travail » | The product page explicitly links this specific licence to the Offres d'emploi API. Its metadata says `conditionUtilisation: licence_specifique`, `contractualisation: false`. | High |
| France Travail CGU | https://francetravail.io/cgu | Conditions générales d'utilisation, version du 1er janvier 2025 | 5.2(b), API et widget « offres d’emploi » | « L’accès à l’API est soumis au respect et à l’acceptation de la licence offre d’emploi » | The general portal terms incorporate the specific offers licence for this API. | High |
| France Travail API portal | https://francetravail.io/produits-partages/documentation/conditions-dutilisation-api | Conditions d’utilisation des API | API en accès libre | « L’API Offres d’emploi peut également être consommée directement […] en respectant la Licence de réutilisation […] » | The API is freely accessible after account creation, but use remains subject to the specific licence. | High |

`contractualisation: false` means the API product does not require a separate bespoke contract in
the portal workflow. It does not remove the specific licence or CGU.

### General account/application terms

| Source | URL | Document/page | Section | Exact relevant wording | Interpretation | Confidence |
|---|---|---|---|---|---|---|
| France Travail CGU | https://francetravail.io/cgu | Conditions générales d'utilisation, version du 1er janvier 2025 | 2. Objet | « Elles s’imposent à tout Utilisateur. Leur acceptation est nécessaire pour créer un compte » | The CGU apply in addition to the specific licence. | High |
| France Travail CGU | https://francetravail.io/cgu | Same | 5.1 Demande d'accès | « l'Utilisateur sélectionne une ou des API et indique le service numérique pour lequel l’accès est sollicité » | API access is tied administratively to a declared digital service. | High |
| France Travail account documentation | https://francetravail.io/produits-partages/documentation/gestion-compte-applications | Gérer mon compte et mes applications | Déclarer une application | « vous devez déclarer une application »; name « identifie votre cas d’usage »; description « présente le contexte dans lequel sont utilisées les API » | CV Analyzer's declared application/context should accurately describe its actual use. | High |
| France Travail account documentation | same URL | Same | Credentials | « Un identifiant client et une clé secrète vous sont délivrés par application. » | Credentials are tied to the declared application, not a reusable account-wide entitlement. | High |
| France Travail CGU | https://francetravail.io/cgu | Same | 4. Accès | « informe France Travail de toute modification, notamment sur les caractéristiques du Service numérique » | A material change from transient search to persistence may require the declared application's characteristics to be updated. The terms do not specify a storage-specific approval. | Medium-High |

No authoritative account page found a separate pre-approval form for API 84, a storage declaration,
or a storage-specific permission. API metadata says public/free access, no contractualisation, and no
published access form. That absence is not permission to disregard the licence.

### Official API contract

| Source | URL | Document/page | Section | Exact relevant wording | Interpretation | Confidence |
|---|---|---|---|---|---|---|
| France Travail OpenAPI | https://francetravail.io/api-peio/v2/api/84/openapi | Offres d'emploi, OpenAPI 3.0.1, API version 2.01 | `GET /v2/offres/{id}` | « Consulter un détail d'offre »; « récupérer le détail d'une offre à partir de son identifiant » | The official direct refetch operation exists. | High |
| France Travail OpenAPI | same URL | Same | Responses | `200`: « L'offre a été récupérée avec succès »; `204`: « L'offre n'existe pas » | The API explicitly distinguishes current detail from disappearance. | High |
| France Travail OpenAPI | same URL | Same | Security | OAuth scopes `o2dsoffre`, `api_offresdemploiv2` | The detail endpoint uses the same authenticated API product. No endpoint-specific retention term appears in OpenAPI. | High |

## Evidence Method

- Conclusions use only current France Travail portal pages, their official content API, France
  Travail CGU/account documentation, and the official OpenAPI.
- The portal's human licence page is client-rendered. Its own JavaScript resolves the page through
  `GET /api-peio/v2/pages/page?slug=...`; that official JSON contains the complete 14-article text.
- Context7 was used only to locate/confirm official France Travail documentation and did not supply
  the detailed licence clauses.
- No blog, forum, social post, GitHub discussion, search snippet, or third-party interpretation
  supports a conclusion in this artifact.
- Short quotations preserve operative qualifiers. Interpretations are engineering classifications,
  not legal advice or replacements for France Travail's clarification.

## Definitions

| Source | URL | Document | Section | Exact wording | Interpretation | Confidence |
|---|---|---|---|---|---|---|
| Specific offers licence | licence URL above | Licence de réutilisation… | Definitions, Base de données | « base de données des offres d’emploi en cours diffusées sur le site internet www.francetravail.fr » | The licensed database is defined around current offers, not a historical offer archive. | High |
| Specific offers licence | same | Same | Definitions, Contenu | « informations intégrées dans la Base de données » | Individual fields can be Content; the licence is not limited to complete records. | High |
| Specific offers licence | same | Same | Definitions, Création | « œuvre de l’esprit intégrant tout ou partie de la Base de données […] par exemple […] un logiciel, une Base de données dérivée, un site internet » | Software integrating even part of the database can be a Creation. Public availability is not part of the definition. | High |
| Specific offers licence | same | Same | Definitions, Base de données dérivée | « base de données créée à partir de la Base de données, y compris toute traduction, adaptation ou modification […] ou d’une partie substantielle » | A transformed/substantially extracted database remains governed as a derived database. | High |
| Specific offers licence | same | Same | Definitions, Réutilisateur | « souscripteur […] ayant créé un compte […] en vue d’accéder à la Base de données » | The account-holding API consumer is the reuser. | High |
| Specific offers licence | same | Same | Definitions, Utilisateur | « utilisateur de la Création » | The term does not require a member of the public; it can include a user of private software. | Medium-High |
| Specific offers licence | same | Same | Article 5 heading | « réutilisations ayant pour objet le rapprochement de l’offre et de la demande d’emploi » | Articles 5.1-5.3 are purpose-scoped. Whether post-selection personal application tracking remains this purpose is not defined. | High |

The licence uses neither `usage privé`, `usage interne`, `usage personnel`, `CRM`, `sélection par
l’utilisateur`, nor an equivalent exemption. “Application privée” in separate OAuth documentation
only describes a confidential client capable of protecting credentials; it is not a licence category.

## Private vs Public Reuse

Classification: **NO_DISTINCTION_FOUND** as a general exemption.

| Source | URL | Document | Section | Exact wording | Interpretation | Confidence |
|---|---|---|---|---|---|---|
| Specific offers licence | licence URL above | Licence de réutilisation… | Article 1.1 | « représenter, distribuer, diffuser tout ou partie de la Base de données […] auprès de tout public » | Public dissemination is expressly contemplated and conditionally licensed. | High |
| Specific offers licence | same | Same | Definition, Création | « un logiciel, une Base de données dérivée, un site internet » | Internal/private software is not excluded merely because it is not public. | High |
| Specific offers licence | same | Same | Article 3 | « pas autorisé à la mettre à la disposition de tiers […] en les sous-licenciant »; users must not « extraire et/ou exploiter le Contenu » | Public display and transfer of reusable rights are different; sublicensing/downstream extraction is prohibited. | High |
| Specific offers licence | same | Same | Article 4 trigger | « Toute mise à disposition de la Base de données ou d’une Création » | Attribution is tied to making the database/Creation available, not expressly to public publication. Whether single-person self-use is a “mise à disposition” is not defined. | High on wording; Medium on self-use application |

The licence distinguishes purpose, dissemination, making available, sublicensing, and user
extractability. It does not say that private, internal, single-user, or non-commercial reuse is exempt
from reuse obligations.

## User-Selected vs Systematic Collection

Classification: **NO_EVIDENCE** of different rules.

| Source | URL | Document | Section | Exact wording | Interpretation | Confidence |
|---|---|---|---|---|---|---|
| Specific offers licence | licence URL above | Licence de réutilisation… | Article 1.1 | « extraire et réutiliser la totalité ou une partie substantielle »; reproduce/edit « en tout ou partie » | The licence contemplates full, substantial, and partial reuse. It does not create a low-volume exemption. | High |
| Specific offers licence | same | Same | Definition, Création | « intégrant tout ou partie de la Base de données » | One selected subset can still be inside the Creation definition. | High |
| Specific offers licence | same | Same | Article 5.3 | « sur chaque offre d’emploi la totalité du Contenu […] pour cette offre d’emploi » | Selection can occur at offer level, but if Article 5 applies, completeness is per selected/displayed offer. | High |

No clause distinguishes explicit human selection, automated selection, systematic collection, bulk
volume, or a numeric threshold. A click does not create an evidenced exemption.

## Storage and Retention

### General storage right and limits

| Source | URL | Document | Section | Exact wording | Interpretation | Confidence |
|---|---|---|---|---|---|---|
| Specific offers licence | licence URL above | Licence de réutilisation… | Article 1.1 | « le droit de fixer, numériser, reproduire » | Storage/reproduction is licensed in principle, subject to the remaining conditions. | High |
| Specific offers licence | same | Same | Article 7 | « stocker, utiliser ou diffuser tout ou partie du Contenu dès lors que celui-ci a été supprimé […] qu’à la condition d’anonymiser » | Unchanged provider content has no general historical-retention right after source deletion. | High |
| Specific offers licence | same | Same | Article 8 | « limiter la durée de conservation des données à ce qui est nécessaire à l’accomplissement de la finalité » | Personal-data retention is purpose-limited. No fixed duration is supplied. | High |
| Specific offers licence | same | Same | Article 8 | « traiter et stocker les données sur le territoire de l’Union européenne ou dans un pays assurant un niveau de sécurité équivalent » | Storage location is contractually constrained to the EU or a country with equivalent security. | High |

### Proposed fields

Article 1.1 generally authorizes reproduction in whole or in part, so no separate permission is
needed for each active-offer field. That general right remains subject to Article 5.3 when the reuse
has an employment-matching purpose, Article 7 after source deletion, Article 8 for personal data,
and Article 4 when the database or Creation is made available.

| Field group | Authoritative treatment | Engineering conclusion |
|---|---|---|
| locally assigned provider key | generated by CV Analyzer, not supplied offer Content | local provenance label; association with source Content does not create a private-use exemption |
| provider offer ID | API Content covered by the general partial-reproduction grant; no reference-only safe harbour | active retention is generally licensed, subject to purpose-specific duties; indefinite post-deletion/private treatment remains ambiguous |
| title | ordinary offer Content covered by Article 1.1 | partial reproduction is generally licensed; retaining title alone conflicts with Article 5.3 if Article 5 applies |
| company name | Content; Article 7 expressly requires removal of company name after source deletion when retaining deleted Content | minimal historical snapshot conflicts with Article 7 if company came from provider Content |
| location | Content; Article 7 expressly removes postal code, INSEE code, and municipality label after deletion | broad location label treatment is not otherwise distinguished |
| contract | Content covered by Article 1.1 | partial reproduction is generally licensed; retaining it alone conflicts with Article 5.3 if Article 5 applies |
| salary | Content covered by Article 1.1 | partial reproduction is generally licensed; retaining it alone conflicts with Article 5.3 if Article 5 applies |
| `origineOffre.urlOrigine` | API Content; it may point to France Travail or a partner site; no reference-only safe harbour | Article 7 expressly names employer-offer/contact URLs, not every possible France Travail URL; post-deletion treatment of this exact field is ambiguous |
| publication/update timestamps | Article 5.2 requires retaining first-publication or update date for each covered offer | required if Article 5 applies |
| locally generated fetch timestamp | generated by CV Analyzer, not supplied offer Content | useful local provenance; it does not replace the provider dates required by Article 5.2 |
| description | Content; Article 5.3 completeness applies if Article 5 applies; company description must be removed after deletion | omission is not supported under Article 5; post-deletion retention is constrained |
| recruiter/contact data | expressly recognized as personal data in Article 8 and removed by Article 7 after deletion | exclude from minimal S4; any full-content design needs lawful basis, minimization, security, retention and deletion controls |

Company name is not automatically personal data. Its Article 7 removal is a contractual
anonymization requirement independent of whether the company is a natural person.

## Minimal Snapshot

Classification: **AMBIGUOUS**.

The licence grants partial reproduction rights in Article 1.1. However, Article 5.3 overrides the
general cutting/deletion right for reuse whose purpose is employment matching:

> « Afin de préserver l’intégrité et la qualité des Données, le Réutilisateur est tenu de faire
> figurer sur chaque offre d’emploi la totalité du Contenu mis à disposition dans l’API pour cette
> offre d’emploi. »

> « Cette obligation s’applique également au logo figurant sur l’offre d’emploi. »

If the selected CRM record remains an offer/Creation used for employment matching, the proposed
minimal subset is incompatible with this clause. Explicit selection, low volume, single-user access,
and private deployment create no stated exception.

The unresolved issue is whether the purpose changes after selection from employment matching to
personal administrative tracking, and whether the durable CRM record is still “chaque offre
d’emploi” for Article 5.3. The licence does not define the boundary. The result cannot be upgraded to
`CLEARLY_PERMITTED`.

## Complete Content Requirement

Classification: **CONDITIONAL**.

| Source | URL | Document | Section | Exact wording | Interpretation | Confidence |
|---|---|---|---|---|---|---|
| Specific offers licence | licence URL above | Licence de réutilisation… | Article 1.1 | adaptation/cutting/deletion rights are granted « sous réserve des dispositions de l’article 5.3 » | Article 5.3 is an express exception to general field removal rights. | High |
| Specific offers licence | same | Same | Article 5 heading | « Dispositions applicables aux réutilisations ayant pour objet le rapprochement […] » | Completeness is not textually generalized to every possible reuse. | High |
| Specific offers licence | same | Same | Article 5.3, Intégrité du Contenu | « sur chaque offre d’emploi la totalité du Contenu […] pour cette offre d’emploi » | Complete API content is mandatory per offer when Article 5 applies. | High |
| Specific offers licence | same | Same | Article 5.3 | « également au logo figurant sur l’offre d’emploi » | A present logo is also mandatory under the same scope. | High |

Scope conclusions:

- public display for job search/matching: required;
- recommendation/matching: required;
- private storage unrelated to matching: Article 5.3 is not textually proven to apply;
- private post-selection application tracking: ambiguous;
- any reuse whatsoever: not supported; the clause is purpose-scoped.

## Synchronization Requirement

Classification for proposed S4: **CONDITIONAL** on Article 5 scope.

| Source | URL | Document | Section | Exact wording | Interpretation | Confidence |
|---|---|---|---|---|---|---|
| Specific offers licence | licence URL above | Licence de réutilisation… | Article 5.2, Datation… | « garantir que seules sont diffusées des offres d’emploi disponibles » | The stated goal is current, available offers in an employment-matching Creation. | High |
| Specific offers licence | same | Same | Article 5.2 | « conserve la date de première publication ou […] de mise à jour indiquée sur chaque offre » | Source dates must survive for covered offers. | High |
| Specific offers licence | same | Same | Article 5.2 | « assure une mise à jour constante du Contenu » | A frozen covered snapshot is incompatible. | High |
| Specific offers licence | same | Same | Article 5.2 | « sollicite l’API au minimum une fois toutes les 24 heures » | The covered reuser must call the API at least once in each 24-hour period. | High |
| Specific offers licence | same | Same | Article 5.2 | « le Contenu créé, supprimé ou modifié […] est respectivement créé, supprimé ou modifié de la Création » | Relevant source changes and deletions must propagate to the covered Creation. | High |

The clause does not expressly say how creation propagation works for a deliberately selected subset.
The most coherent narrow reading is that content in the reused scope must remain synchronized, not
that every new France Travail offer must be inserted into a personal CRM. This remains an
interpretation, not a defined selected-record rule.

For the proposed S4:

- if application tracking remains Article 5 employment matching, daily API use and propagation are
  required, making bounded static S4 a `NO_GO_FOR_MINIMAL_S4`;
- if France Travail confirms post-selection tracking is outside Article 5, this specific daily rule
  is not proven to apply, but Articles 4, 7 and 8 still require separate answers.

## Deleted / Expired Offers

### Provider Content

| Source | URL | Document | Section | Exact wording | Interpretation | Confidence |
|---|---|---|---|---|---|---|
| Specific offers licence | licence URL above | Licence de réutilisation… | Article 5.2 | deleted Content is « supprimé […] de la Création » | A covered active matching Creation must propagate deletion. | High |
| Specific offers licence | same | Same | Article 7, Anonymisation… | deleted Content may be stored/used/diffused only « à la condition d’anonymiser la Base de données dérivée » | Historical retention of unchanged source content is not permitted by this clause. | High |
| Specific offers licence | same | Same | Article 7 removal list | « nom, description et URL de l’entreprise »; contact identity/details/comments; employer/contact URLs; « code postal, code INSEE et libellé de la commune » | A retained deleted-provider record must remove these exact categories. Company name cannot remain merely because it is useful history. | High |

The licence does not provide an “applied to this offer” historical exception.

### User-Owned Application History

Application date, personal notes, reminders, interview history, status, and outcome created by the
user are not information supplied by the API and are conceptually outside the licensed Content.
The licence does not prohibit retaining those independent facts.

The boundary is not defined for factual labels copied from or manually retyped from the API, such as
title and company. A user click or confirmation does not prove independent origin. If “Backend Java
Developer — Company X” came from provider Content, Article 7 expressly puts the company name in the
post-deletion removal list. Whether the same words may be retained as an independently user-created
fact about a completed application requires official clarification; the licence supplies no
conversion rule.

## Attribution

Classification for a private single-user CRM: **CONDITIONAL / NOT VERIFIED** because the trigger
“mise à disposition” is not defined for self-use. No private exemption exists.

| Source | URL | Document | Section | Exact wording | Interpretation | Confidence |
|---|---|---|---|---|---|---|
| Specific offers licence | licence URL above | Licence de réutilisation… | Article 4, Mentions obligatoires… | « Toute mise à disposition de la Base de données ou d’une Création doit mentionner » | Attribution applies whenever the database or Creation is made available; public internet publication is not stated as a condition. | High |
| Specific offers licence | same | Same | Article 4 | « la source du Contenu (France Travail) et la date de sa dernière mise à jour » | Source and last-update date are required. | High |
| Specific offers licence | same | Same | Article 4 | « le fait que la réutilisation […] est soumise à la présente Licence, ainsi qu’un lien hypertexte » | A licence statement and accessible hyperlink are required. | High |
| Specific offers licence | same | Same | Article 4 | « aisément accessibles par les Utilisateurs » | The mentions cannot be hidden. | High |

If Article 4 applies to the private CRM screen, `Source: France Travail` plus `Voir l'offre
originale` is insufficient. It lacks the Content's last-update date, the statement that reuse is
subject to the licence, and a hyperlink to the licence. The licence does not prescribe exact visual
placement.

## Transformation Disclosure

| Source | URL | Document | Section | Exact wording | Interpretation | Confidence |
|---|---|---|---|---|---|---|
| Specific offers licence | licence URL above | Licence de réutilisation… | Article 4 | « Un fichier détaillant l’ensemble des modifications […] ou la méthode appliquée […] (par exemple un algorithme) » | Modifications or their method must be documented for users. | High |
| Specific offers licence | same | Same | Article 4 | file/method is « mis à la disposition des Utilisateurs, gratuitement » | Disclosure must be available without a fee, subject only to physical distribution cost wording. | High |

If a covered Creation strips HTML, omits fields, normalizes contract values, parses salary, or uses
an algorithm to modify provider data, those operations may constitute modifications requiring the
Article 4 file/method disclosure. The licence does not enumerate these examples, so France Travail
should clarify the expected disclosure scope.
Eligibility is separately calculated rather than a modification to the provider offer, but its
method should be clearly separated to avoid implying that France Travail supplied the verdict.

As with attribution, the exact self-use trigger is ambiguous; public/internal multi-user making
available is more clearly covered. Article 5.3 may independently forbid field omission for matching
reuse rather than merely require disclosure of the omission.

## Personal Data

### Offer payload evidence

The official OpenAPI `Offre.contact` schema can contain:

- « Nom du recruteur »;
- recruiter address fields;
- « N° de téléphone du recruteur »;
- contact comments;
- recruiter/application URLs.

The OpenAPI documents that recruiter email itself is no longer displayed and that the field now
provides a link to the France Travail offer. The proposed minimal S4 excludes these contact fields and therefore
substantially reduces, but does not by itself resolve, personal-data risk.

### Contractual and GDPR boundary

| Source | URL | Document | Section | Exact wording | Interpretation | Confidence |
|---|---|---|---|---|---|---|
| Specific offers licence | licence URL above | Licence de réutilisation… | Article 8, Protection des données… | « La Base de données contient des données à caractère personnel » including contact name and details | The licence expressly acknowledges personal data in offers. | High |
| Specific offers licence | same | Same | Article 8 | purpose must not be incompatible with « le rapprochement entre l’offre et la demande d’emploi » | Personal-data use is purpose-limited. Personal application tracking is likely compatible, but the licence does not expressly name it. | Medium-High |
| Specific offers licence | same | Same | Article 8 | « traitements à des fins commerciales, notamment la constitution d’un fichier clients, sont incompatibles » | Employer/contact prospecting or customer-list creation is expressly incompatible. | High |
| Specific offers licence | same | Same | Article 8 | data must be « adéquates, pertinentes et limitées à ce qui est nécessaire » | Data minimization applies independently of Article 5 completeness. | High |
| France Travail CGU | https://francetravail.io/cgu | CGU | 8.2 | Partner is responsible for its processing, legal basis, information, security and retention | France Travail's contract does not replace the reuser's GDPR obligations. | High |

Separate layers:

- API contractual/reuse licence: governs access/reuse, complete content, synchronization,
  anonymization, attribution and contractual data-protection duties;
- copyright/database rights: Article 1 grants the necessary exploitation rights subject to licence;
- GDPR/French data-protection law: independently governs any personal-data processing;
- general French employment law: incorporated for placement/diffusion constraints, but it does not
  answer the private CRM storage classification in the published licence.

Company name is not assumed to be personal data. Contact names/details may be. The full-content
obligation and minimization duty coexist in the licence; the document does not explain their
interaction for a private CRM.

## Source URL / Deep Link

Classification: **NOT_VERIFIED as a safe harbour**.

The licence describes redirection to France Travail/partner sites when direct application details are
not supplied, but it does not create a `provider ID + URL only` reuse category. It contains no
`simple lien`, metadata-only, deep-link, or reference-only exemption.

S3 retains the API field `origineOffre.urlOrigine`; observed values may be France Travail or partner
URLs. This is distinct from a France Travail detail URL locally constructed from the provider ID.
Storing no copied substantive content would materially reduce the amount of licensed Content and
personal data, but provider ID and `urlOrigine` are still API Content. Article 7 specifically names
employer/contact URLs for removal after deletion; it does not specifically classify every France
Travail detail URL. Therefore neither API-supplied nor locally constructed linking can be declared a
safe harbour without clarification.

## Reference-Only Alternative

Classification: **LIKELY_BUT_NOT_PROVEN** to reduce obligations; licence status **AMBIGUOUS**.

Architecture:

```text
sourceProvider + sourceExternalId + sourceUrl
        + independently user-entered application status/dates/notes
        + no automatically copied offer title/company/location/contract/salary/content
```

This separates provider reference from user activity and avoids automatically retaining substantive
offer fields. It does not eliminate uncertainty because:

- no link/reference-only safe harbour exists;
- the licence does not say when a reference ceases to be reuse of Content;
- Article 4 may still apply if provider Content is made available;
- Article 5 may still apply if the record represents an offer for matching;
- Article 7 does not expressly permit indefinite retention of provider references after deletion.

This is not an approved workaround. It requires the same official clarification.

## User-Confirmed Manual Alternative

Classification: **AMBIGUOUS**.

No licence clause recognizes human confirmation, manual retyping, user review, or editing as changing
provider-derived Content into independently owned data. A confirm button is not a legal provenance
boundary.

Independently user-created facts about the user's activity are separable: application date, status,
interview/follow-up events, personal notes and outcome. Automatically prefilled, copied, paraphrased,
or retyped title/company/location/salary/description remain potentially provider-derived. The
licence gives no test for when such facts become independent.

Therefore a user-reviewed prefilled form does not establish permission for minimal provider-content
retention.

## Personal vs Commercial Use

### Current personal use

The licence does not create a personal, hobby, non-commercial, local, or single-user exemption. Its
general rights and obligations apply to the Reuser/Creation according to purpose and processing.
Current personal use therefore does not itself permit minimal persistence.

### Commercial constraints

| Source | URL | Document | Section | Exact wording | Interpretation | Confidence |
|---|---|---|---|---|---|---|
| Specific offers licence | licence URL above | Licence de réutilisation… | Article 5.1 | « aucune rétribution, directe ou indirecte, ne peut être exigée des personnes à la recherche d’un emploi » for placement services | Jobseekers cannot be charged for placement services. | High |
| Specific offers licence | same | Same | Article 5.1 | « interdit […] de vendre des offres d’emploi » | Offers themselves may not be sold. | High |
| Specific offers licence | same | Same | Article 8 | commercial personal-data processing, including « constitution d’un fichier clients », is incompatible | Employer/recruiter prospecting CRM use is incompatible. | High |

`FUTURE_PRODUCT_CONSTRAINT`: a public SaaS would more clearly make a Creation available to Users,
triggering Article 4 attribution/method disclosure, Article 3 downstream-extraction protections, and
Article 5 display/completeness/synchronization if its purpose is job matching. The licence does not
ban every commercial software model, but selling offers, charging jobseekers for placement, and
commercial contact/customer-list processing are constrained or prohibited. Future SaaS requires a
fresh review of business model and declared application.

## Decision Matrix

| Architecture | Licence status | Key retention-relevant obligations | Retention rule | Sync requirement | Deletion requirement | Attribution requirement | Confidence |
|---|---|---|---|---|---|---|---|
| A. Transient live search (current S3 concept) | `PERMITTED_WITH_OBLIGATIONS` | Article 5 likely applies: complete content/logo per displayed offer, source dates, personal-data compliance, no downstream extraction | no durable retention needed; transient cache only as necessary | live calls naturally use current data; Article 5 still says API at least once/24h while Creation operates | expire transient data; Article 7 if deleted Content is retained | Article 4 source, last update, licence statement/link, method disclosure when made available | High that job search is Article 5; Medium-High on wholly stateless 24h operation |
| B. Minimal automatic snapshot after explicit selection | `AMBIGUOUS` | Article 1.1 generally permits partial reproduction, but no selection/private exemption exists; if Article 5 applies, minimal fields conflict with Article 5.3 and require dates/sync | personal-data necessity; Article 7 after deletion | required at least every 24h if post-selection tracking remains Article 5 | propagate deletion if Article 5; historical retention of deleted Content requires Article 7's exact anonymization | likely Article 4 if CRM is made available to its user; self-use trigger undefined | High that no exemption exists; Medium on Article 5 classification |
| C. Full provider offer snapshot | `AMBIGUOUS`; a synchronized active matching Creation is `PERMITTED_WITH_OBLIGATIONS`, but a frozen snapshot is not supported | if Article 5 applies: full API content/logo, dates, Article 4, Article 8 and access controls; if outside Article 5, logo use invokes Article 9 permission | only while necessary/compatible; unchanged deleted snapshot unsupported | at least every 24h and propagate changes if Article 5 | remove from active Article 5 Creation; historical retention requires removal of Article 7's enumerated fields | required when made available; self-use trigger undefined | High on conditional clauses; Medium on post-selection purpose |
| D. Reference-only provider ID + URL | `AMBIGUOUS` | no safe harbour; keep no substantive provider content; independent user facts only | indefinite provider-reference retention not verified | conditional if treated as Article 5 Content/Creation | provider reference behavior after deletion not defined; do not assume it can remain | conditional if reference is Content made available | Medium-Low |
| E. User-confirmed/manual CRM record derived from transient offer | `AMBIGUOUS` | confirmation does not cleanse provider origin; separate demonstrably user-created activity facts | user activity facts may persist outside licence; copied provider facts remain unresolved | conditional for provider-derived facts if Article 5 applies | Article 5.2 removes source-deleted Content from a covered active Creation; Article 7 historical retention removes its enumerated fields; no confirmation exception | conditional while provider-derived Content remains | High that no confirmation exception exists; Medium-Low on independent-fact boundary |

The current S3 implementation is outside this mission, but the licence matrix establishes that live,
non-persistent search is not obligation-free. Article 5.3's complete-content/logo requirement and
Article 4 attribution requirements should be assessed separately for current rendering. No S3 code
was changed here.

## Remaining Uncertainty

The document is not missing. The ambiguity comes from undefined scope terms in an otherwise
retrievable governing licence.

Exact unanswered questions:

1. Does a single-user private record used only after explicit selection to track an application
   remain a reuse « ayant pour objet le rapprochement de l’offre et de la demande d’emploi »?
2. Is that CRM record still « chaque offre d’emploi » for Article 5.3, even when it is presented as
   user-owned application history rather than an available offer?
3. If Article 5 applies, how must Article 5.2 creation/update/deletion propagation operate for an
   explicitly selected subset, including whether newly created offers outside that subset must enter
   the Creation?
4. Does `sourceProvider + sourceExternalId + sourceUrl` alone constitute Content/reuse requiring
   Articles 4, 5, and 7?
5. After source deletion, may independently user-confirmed application facts retain title/company,
   or must copied provider title/company be removed under Article 7 regardless of application
   history?
6. Is a one-person local screen a « mise à disposition […] d’une Création » under Article 4?

The clauses causing uncertainty are Article 5's purpose heading, Articles 5.2/5.3's Creation/offer
requirements, Article 4's undefined “mise à disposition,” and Article 7's treatment of deleted
Content.

France Travail API/legal support, as licensor and portal operator, should answer these questions in
writing. A qualified legal review can interpret the contract but cannot replace France Travail's
clarification of its intended product/licence scope.

## Recommended S4 Licence Boundary

Do not write or implement the minimal-snapshot S4 Story yet.

Possible outcomes after official response:

- If France Travail confirms post-selection private application tracking is outside Article 5 and
  permits the stated minimal fields, proceed with the architecture already investigated, adding only
  any Article 4/7/8 requirements they specify.
- If France Travail confirms Article 5 applies, the proposed bounded minimal S4 is
  `NO_GO_FOR_MINIMAL_S4`: complete content, <=24-hour synchronization, modification/deletion
  propagation and Article 7 behavior are major capabilities explicitly excluded from S4.
- If France Travail permits reference-only storage but not copied provider fields, reassess that
  product compromise; do not silently relabel it as the original S4.

Do not solve the ambiguity through hidden fields, user confirmation, manual retyping, or a technical
claim that private storage is not reuse.

## France Travail Clarification Request

Draft only; do not send automatically.

**Objet : Clarification licence Offres d’emploi — conservation d’une offre sélectionnée dans un CRM personnel privé**

> Bonjour,
>
> Je développe un outil personnel de suivi de recherche d’emploi utilisant l’API Offres d’emploi.
> La recherche est effectuée en direct et les résultats ne constituent ni un catalogue, ni une
> rediffusion publique, ni une collecte en masse.
>
> Lorsqu’un utilisateur individuel clique explicitement sur « Ajouter à mes opportunités », le
> serveur relirait l’offre par son identifiant puis conserverait uniquement dans son CRM privé :
> l’identifiant fournisseur, l’URL source, l’intitulé, l’entreprise, le lieu, le contrat, le salaire
> affiché et les dates de publication/actualisation. La description, le logo, les compétences, les
> coordonnées de contact et le HTML ne seraient pas conservés. Le but exclusif est de suivre sa
> propre candidature (statut, date, relances et notes), sans rendre l’offre accessible au public.
>
> Pouvez-vous confirmer si cette conservation post-sélection constitue une réutilisation « ayant
> pour objet le rapprochement de l’offre et de la demande d’emploi » au sens de l’article 5 de la
> Licence de réutilisation de la base de données des offres d’emploi de France Travail ?
>
> Dans ce cas d’usage privé et individuel, devons-nous notamment :
>
> - conserver et afficher la totalité du contenu et le logo au titre de l’article 5.3 ;
> - solliciter l’API au moins toutes les 24 heures et répercuter créations, modifications et
>   suppressions au titre de l’article 5.2 ; cette obligation est-elle limitée aux offres
>   explicitement sélectionnées ?
> - supprimer l’offre de la Création active au titre de l’article 5.2 puis, en cas de conservation
>   historique, supprimer les champs énumérés à l’article 7 ?
> - rendre aisément accessibles les mentions, la date, le lien de licence et le fichier détaillant
>   les modifications ou leur méthode prévus à l’article 4 ?
> - conserver les champs de contact/données personnelles si l’article 5.3 impose le contenu complet,
>   et comment concilier cette obligation avec la minimisation, la durée, la sécurité, la licéité et
>   la localisation du stockage prévues à l’article 8 ?
>
> À défaut, la conservation du seul identifiant fournisseur et de l’URL source, associée uniquement
> à des informations de candidature saisies par l’utilisateur, bénéficie-t-elle d’un traitement
> différent ?
>
> Merci de préciser également si la description de l’application déclarée sur francetravail.io doit
> être mise à jour ou faire l’objet d’une validation particulière pour cet usage.
>
> Cordialement,

Support route: https://francetravail.io/contact

## Go / No-Go

**NEED_OFFICIAL_CLARIFICATION**

Rationale:

- governing documents are authoritative and fully retrievable;
- no private/personal or selected-record exemption exists;
- Article 5 would make minimal S4 incompatible through complete-content and synchronization duties;
- the licence does not define whether post-selection private application tracking remains Article 5
  employment matching;
- reference-only and human-confirmed alternatives have no express safe harbour;
- guessing that the purpose changed would not meet the required legal-certainty standard.

Confidence: **HIGH** in the extracted clauses and absence of explicit exemptions; **MEDIUM** in the
scope analysis because the decisive post-selection CRM classification is not defined by the licence.
