# Investigation — Discovered JobOffer to Opportunity

Status: investigation only. No Engineering Story, production code, migration, offer persistence,
or autonomous collection was created.

## Evidence Legend

- **OBSERVED**: verified in the current repository, Git/DevLog, or cited official provider material.
- **INFERRED**: conclusion supported by observed implementation, but not an explicit contract.
- **RECOMMENDED**: proposed boundary for a future Story; not implemented.
- **NOT VERIFIED**: evidence is insufficient and must be resolved before implementation.

## Executive Summary

`JobOffer -> Opportunity` is the correct next product boundary, but it is not currently ready to
become an Engineering Story.

The current `OpportunityEntity` is a durable employment-target context shared by CV analyses,
cover letters, and CRM applications. It is neither the transient provider offer nor the CRM
pipeline record. The pipeline record is `ApplicationEntity`, whose existing `NOT_CONTACTED`
status already means a selected lead that has not been applied to. Consequently, an action that
claims to add an offer to the existing CRM must atomically create both:

```text
OpportunityEntity(status = DRAFT)
        +
ApplicationEntity(status = NOT_CONTACTED)
```

Creating only an Opportunity would leave it absent from the current `/applications` CRM workflow.
Creating either object during search would violate the established explicit-selection boundary.

**RECOMMENDED** — the browser should submit only a bounded provider key and external offer ID. A
dedicated application use case should retrieve the current offer from a provider lookup capability,
map a minimal provider-neutral snapshot, and invoke the existing CRM creation rules in a short local
transaction. Hidden title, company, salary, URL, or description fields are user-controlled and must
not be accepted as authoritative provider data.

**RECOMMENDED** — duplicate identity is `(source_provider, source_external_id)`. Enforce it through
both an idempotent application check and a database unique constraint on nullable Opportunity
columns. Manual and legacy Opportunities retain null source identity and remain unaffected.

**RECOMMENDED** — do not persist eligibility, reasons, ProfessionalProfile, or
JobSearchPreferences. Eligibility is derived from mutable inputs. All three statuses remain
saveable; `INELIGIBLE` is warned, not blocked, because the user remains authoritative.

**BLOCKER** — repository licence evidence explicitly says the unresolved non-technical gates must
be resolved before any France Travail persistence. It is not established whether a private,
explicitly selected CRM snapshot may retain minimal fields without the full-content, daily
reconciliation, deletion/anonymization, attribution, and method-disclosure duties previously
identified. The minimal S4 below is therefore conditional on written France Travail/legal
clarification. If those duties apply to selected CRM records, this proposed S4 conflicts with the
required scope (no autonomous synchronization) and must not proceed as designed.

Final recommendation: **NEED_MORE_INVESTIGATION**, limited to the licence/retention question. No
additional architecture investigation is required before Story design once that gate is answered.

## Git And DevLog Baseline

### Git safety

- **OBSERVED** — investigation started on clean `main` at `1ef8fba`.
- **OBSERVED** — local `main`, local `origin/main`, and `origin/HEAD` all pointed to `1ef8fba`.
- **OBSERVED** — `1ef8fba` is `Merge pull request #3 ... job-discovery-vertical-slice`.
- **OBSERVED** — the repository had one worktree.
- **OBSERVED** — `git fetch` failed because the SSH agent refused the ED25519-SK signing request.
- **NOT VERIFIED** — remote state newer than the local `origin/main` could not be checked.
- **OBSERVED** — no investigation branch was needed; repository convention permits an artifact
  under `docs/investigations` and no production changes were made.

### DevLog

| Item | Observation |
|---|---|
| availability | available; server `devlog-mcp` version `0.1.0`, status `ready` |
| observed/current revision | `1ef8fba0fabaeec7f0c34f927deb1c6744133703` |
| ingested revision | `1ef8fba0fabaeec7f0c34f927deb1c6744133703` |
| baseline/context revision | `8402ea6011fb392969298c54b979148391a1b09a` |
| freshness | `PARTIALLY_FRESH`; `REFRESH_RECOMMENDED` |
| review queue | 5 pending, 0 accepted, 0 rejected |

- **OBSERVED** — S3 is discoverable in DevLog through merge `1ef8fba`, feature commit `fa30950`,
  final fixes, `JobOffer`, `DiscoverJobOffers`, the France Travail adapter, and all S3 artifacts.
- **OBSERVED** — DevLog found the original Opportunity source concept at commit `15afc39`, but its
  current engineering context did not provide source bodies or a complete CRM model.
- **OBSERVED** — broad multi-keyword searches returned no matches; individual searches for
  `Opportunity`, `JobOffer`, `France Travail`, `S3`, `ProfessionalProfile`, and
  `JobSearchPreferences` were useful for commit discovery.
- **OBSERVED** — repository fallback was required for every domain, persistence, lifecycle,
  security, and licence conclusion in this artifact.

## Current S3 Discovery Flow

```text
POST /job-discovery/search
        -> persisted ProfessionalProfile existence check
        -> persisted active JobSearchPreferences
        -> JobOfferProvider.search(targetRole, maxResults = 50)
        -> France Travail /offres/search
        -> FranceTravailOfferDto
        -> canonical in-memory JobOffer
        -> EligibilityEvaluator
        -> request-scoped view models
        -> Thymeleaf HTML
        -> discard
```

Evidence:

- `DiscoverJobOffers` checks provider availability, profile existence, and active preferences,
  searches, evaluates, and returns immutable records (`DiscoverJobOffers.java:27-60`).
- The controller renders the POST directly; it does not redirect to a durable result.
- `job-discovery-results.html:33-72` only renders result cards and external links. There is no save
  form or CRM action.
- No discovery entity, repository, migration, or cache exists.

The ProfessionalProfile is currently only an existence gate. Its title, skills, experience,
education, languages, and location are not consumed by S3. Preferences drive the role selection
and deterministic evaluator, with several known S3 gaps (notably location and work-mode mapping).

## Current Opportunity Domain

### Domain map

```text
CompanyEntity
    ^ optional many-to-one
OpportunityEntity
    +-- many ResumeAnalysisRecordEntity
    |       +-- one ResumeDocumentEntity
    |               +-- many ResumeVersionEntity
    +-- many CoverLetterEntity
    +-- many ApplicationEntity
            +-- many ApplicationStatusHistoryEntity
            +-- optional ResumeVersionEntity
            +-- optional CoverLetterEntity
            +-- optional ResumeAnalysisRecordEntity
```

`ExternalProjectionEntity` tracks Google Sheets projection using textual resource identity and has
no foreign key to `ApplicationEntity`.

### Opportunity fields

`OpportunityEntity` (`career/persistence/OpportunityEntity.java:17-68`) contains:

- identity: database ID;
- target: title, denormalized company name, optional `CompanyEntity`;
- conditions: canonical/raw contract, canonical/raw schedule, remote mode, salary, distance,
  location;
- origin: free-text `source`, `sourceType` (`MANUAL` or `URL`), source URL;
- content: raw description, normalized description, detected language;
- lifecycle: `DRAFT`, `ANALYZED`, or `ARCHIVED`;
- created/updated timestamps.

There is no provider key, provider offer ID, provider date, fetched date, eligibility, preference
snapshot, profile snapshot, or provider-identity uniqueness.

### Services and creation paths

- Manual CRM creation (`ApplicationCrmService.create`) creates/reuses a company, creates a new
  Opportunity, creates an Application and initial history, and publishes a projection event in one
  transaction (`ApplicationCrmService.java:39-73`).
- CV analysis creates a separate Opportunity with status `ANALYZED`.
- Cover-letter generation creates a separate Opportunity with status `DRAFT`.
- These paths do not currently reuse an existing Opportunity.
- Manual CRM creation always creates a new Opportunity even if title/company/URL repeat.

### Editing and deletion

- The application edit form can modify company, Opportunity, Application, document links, and
  notes (`ApplicationCrmService.java:88-99`).
- Provider-originated CRM fields remain editable after conversion; the user owns the durable
  working record. Future source refresh must not silently overwrite those edits.
- **OBSERVED** — current editing mutates the shared `CompanyEntity`, and CRM views prefer that
  entity's name over the denormalized Opportunity company name. Editing one CRM item can therefore
  change the company displayed by another item. S4 should document and test this inherited behavior;
  fixing Company master-data/edit isolation is a separate CRM concern.
- Deleting an Application deletes its status history through the database cascade but leaves the
  Opportunity and Company. There is no automatic source-identity cleanup.
- Opportunity deletion is separately guarded against analyses/letters and can fail on an existing
  Application foreign key. `ARCHIVED` Opportunity status has no active production flow.

### CRM and dashboard integration

- `/applications` and `/applications/{id}` are the actual CRM list/detail entry points.
- Application mutations publish Google Sheets projection after commit; MySQL remains the source of
  truth.
- Dashboard Opportunity counts include all Opportunities, including analysis/letter-created and
  orphaned records. Automatic discovery persistence would pollute these metrics.

## Opportunity Semantics

The best answer is **D: a generic employment opportunity**, with an important qualification.

`OpportunityEntity` is a durable working context for one targeted employment position. It can exist
for analysis, cover-letter generation, or application tracking. It resembles a job offer snapshot,
but is not restricted to provider offers and is not itself the CRM lifecycle record.

`ApplicationEntity` is the actual CRM pipeline item. Despite its name, it begins at
`NOT_CONTACTED` (`ApplicationStatus.java:3-11`), so it also represents a selected lead before an
application has been sent. The current UI labels that status “Non démarché”.

Therefore:

- Opportunity is not A alone: manual text and analysis contexts need no external job offer.
- Opportunity is not B: application dates/status/history live on `ApplicationEntity`.
- Opportunity is not C: Company is separate and an Opportunity requires a target position context.
- Opportunity is D: a user-selected generic employment target/context.

## Current JobOffer Meaning

`JobOffer` is an immutable, provider-neutral-in-intent, non-JPA canonical representation of one
external employment offer returned during discovery. It is not user-owned and has no durable
lifecycle.

### Canonical fields

The final merged record (`discovery/domain/JobOffer.java:12-40`) contains:

- source identity: `providerKey`, `providerOfferId`, `originUrl`, `fetchedAt`;
- identity/content: `title`, `description`, `company`;
- occupation: `romeCode`, `romeLabel`, `appellationLabel`;
- location: label, commune code, postal code, latitude, longitude;
- contract: canonical type, raw code, raw label;
- competencies: code, label, raw requirement code;
- conditions: experience label, work mode, work duration label;
- salary: raw text, min/max amounts, period;
- provider dates: created and updated timestamps.

### France Travail-specific data

France Travail DTOs additionally receive fields not retained by the canonical record, including
contract nature, explicit experience requirement, converted work duration, alternance, provider
origin label, accessibility flags, and salary complements. The final DTO does not model all fields
in the official offer schema (for example full company/logo, contact, agency, and other disclosure
data). ROME is France-specific even though it currently appears in the canonical record.

The mapper currently sets work mode to null. Salary parsing only supports a narrow observed French
annual-Euro format. These limitations do not block the transition because those fields are not
required durable identity.

## Transient Nature

Successful offer data survives only:

- as Java objects during provider/use-case execution;
- as Spring MVC model attributes for the result POST response;
- as escaped rendered browser HTML and potentially browser history/cache.

It does not survive in:

- the database;
- the HTTP session;
- hidden form fields;
- a server-side offer cache;
- a durable result URL.

Only the OAuth access token is cached in singleton memory across requests. A blank-role error uses a
one-use MVC flash attribute; no offer enters that flash state.

## JobOffer vs Opportunity Boundary

The transition crosses four meanings:

```text
provider-owned external data
        -> transient canonical data
        -> explicit human acceptance
        -> user-owned durable CRM working record
```

The explicit click is a domain command, not a persistence side effect of discovery. Search must
remain read-only.

Because the current CRM manages Applications rather than bare Opportunities, the use case should
return the existing or newly created Application ID and redirect to `/applications/{id}`. Internally
it creates/reuses the Opportunity and ensures one CRM item. The button label can remain “Ajouter à
mes opportunités”; the destination is the existing CRM detail.

## Current Persistence Model

- Flyway owns schema changes; Hibernate runs `ddl-auto=validate`.
- Production configuration uses MySQL; tests use H2 in MySQL mode.
- Current migrations are V1-V5 and use additive SQL, named constraints, and nullable columns for
  backward-compatible Opportunity evolution.
- The deployed MySQL server version is not pinned in the repository.
- Existing Opportunities have no reliable source identity that can be backfilled. URLs must not be
  guessed into provider identities.

## Transition Trust Boundary

### Rejected request strategies

| Strategy | Decision | Reason |
|---|---|---|
| all fields in hidden inputs | reject | every value is user-controlled; large, duplicated mapping and tampering surface |
| temporary session/cache | reject for V1 | stateful expiry, memory and multi-tab behavior for no product benefit |
| signed serialized snapshot | reject for V1 | integrity is possible, but payload/key/version/expiry complexity remains and stale/deleted offers bypass provider truth |
| persist discoveries first | reject | violates S3 boundary and creates a catalogue/synchronization lifecycle |
| provider key + ID, then refetch | recommend | minimal request, provider-authenticated source, current data, simple tamper rejection |

The command input should contain only:

- known provider key;
- bounded external offer ID.

The server must validate both before lookup. No title, company, salary, source URL, description,
eligibility, or source date sent by the browser is authoritative.

After refetch, the use case must enforce the destination column limits rather than relying on
`ApplicationForm` validation, which this path bypasses: title/company 200, location 300, raw contract
120, salary 200, display source 200, and URL 2048 characters. Minimal V1 should reject an oversized
trusted offer with a manual-entry fallback rather than silently truncate provider content.

Current inbound authentication, authorization, and Spring Security CSRF protection are absent. The
application is documented as local mono-user. S4 should not invent a new authentication architecture,
but an Internet-accessible deployment requires a separate security prerequisite. A POST endpoint
alone does not solve cross-site request forgery under the current stack.

## Application Service Boundary

**RECOMMENDED** — a dedicated application use case, named only when the Story is written, owns this
orchestration:

```text
validate provider key + external ID
        -> retrieve trusted current JobOffer outside write transaction
        -> validate/map minimal CRM seed
        -> short transactional CRM operation
                -> find existing source identity
                -> create/reuse Company
                -> create/reuse Opportunity(DRAFT)
                -> create/reuse Application(NOT_CONTACTED)
                -> record initial history
                -> rely on unique constraint
        -> return application ID + created/already-present outcome
        -> existing projection listener runs after commit
```

The controller must not copy fields. The use case should use an explicit command and mapper, not the
web `ApplicationForm`. The smallest clean implementation is explicit mapping inside the application
use case or a package-private factory method. A general mapper framework or MapStruct is not
justified.

The CRM creation internals should be reused/extracted so manual and discovery creation share
company resolution, Opportunity/Application defaults, history, and projection behavior. A parallel
discovery-only aggregate creation path would drift.

## Provider Refetch Strategy

### Capability

The official France Travail OpenAPI 2.01, rechecked on 2026-08-27, defines:

```text
GET /v2/offres/{id}
200: offer retrieved
204: offer does not exist
```

Source: `https://francetravail.io/api-peio/v2/api/84/openapi`.

The current application does not implement this endpoint. `JobOfferProvider` only has `search(...)`
and `isAvailable()` (`JobOfferProvider.java:3-6`), and `FranceTravailApiClient` only calls
`/offres/search` (`FranceTravailApiClient.java:30-60`).

### Port evolution

**RECOMMENDED** — use a small separate lookup capability rather than forcing every future search
provider to support direct retrieval. Conceptually it needs provider identity and one operation that
returns found/not-found or a canonical `JobOffer`. France Travail's adapter can implement search and
lookup. Do not add pagination, synchronization, or a provider registry until a second provider
requires them.

For V1, the use case can inject the single configured lookup capability and reject a command whose
provider key does not match it. The database identity remains provider-neutral.

### Refetch decision

**YES** — refetch on save.

Benefits:

- prevents hidden-field tampering from becoming durable provider-attributed data;
- gets the current provider record and full detail response;
- detects disappearance between search and save;
- avoids server-side result storage and signed payloads.

Costs are one API call, latency, token/quota consumption, and dependency on provider availability.
Those costs are proportionate to an explicit low-volume user action.

### Disappearance or outage

- `204/not found`: do not create a provider-attributed Opportunity. Explain that the offer is no
  longer available and offer the existing manual CRM creation route.
- provider timeout/auth/5xx: do not create; preserve current CRM data and invite retry.
- do not fall back to browser fields, because failure must not weaken the trust boundary.
- V1 should not add a signed temporary snapshot fallback. That can be reconsidered only if measured
  failures make it necessary and licence treatment is known.

## JobOffer Data Classification

All provider-originated content remains subject to the unresolved licence gate. This table answers
the product/data-minimization question assuming selected-record persistence is confirmed as allowed.

| Candidate | Classification | Persistence decision and reason |
|---|---|---|
| provider | REQUIRED | stable provenance and half of duplicate identity |
| externalOfferId | REQUIRED | provider lookup and strongest duplicate identity |
| sourceUrl | USEFUL | original-offer navigation; persist only validated HTTP(S) |
| title | REQUIRED | minimum durable human context |
| company | REQUIRED | minimum durable human context and current CRM creation contract; missing company falls back to manual entry in V1 |
| location | USEFUL | concise offline context; maps to existing CRM field |
| contract | USEFUL | canonical value plus one raw display label; maps to existing fields |
| salary | USEFUL | raw display text is more faithful than derived numeric parsing |
| description | LEGAL_UNCERTAINTY | valuable but large/provider-owned and may contain personal/contact data; do not persist in minimal V1 |
| workMode | USEFUL | seed existing remote mode only when canonical data is known; current FT mapper supplies none |
| competencies | NOT_NEEDED | no CRM field and unnecessary for minimum historical context |
| ROME | NOT_NEEDED | discovery/evaluation metadata, France-specific, not required by current CRM |
| provider publication date | REQUIRED | provider provenance and required by existing licence evidence if data is retained |
| provider update date | REQUIRED | provider provenance and required by existing licence evidence if data is retained |
| fetchedAt | REQUIRED | says when the trusted snapshot was obtained; do not call it provider update time |
| discoveredAt | NOT_NEEDED | not trustworthy after stateless results and not required to manage the CRM item |
| savedAt | REQUIRED | existing Opportunity/Application `createdAt` already records this |
| eligibility verdict | DERIVED_ONLY | depends on mutable preferences/profile/rules; do not persist |
| eligibility reasons | DERIVED_ONLY | same staleness problem; do not persist |
| ProfessionalProfile | NOT_NEEDED | no Opportunity ownership/provenance requirement and S3 only checks existence |
| JobSearchPreferences | NOT_NEEDED | discovery input, not CRM state |

## Snapshot vs Reference

### Options

| Option | Assessment |
|---|---|
| provider ID + URL only | insufficient: CRM loses title/company when the offer disappears and becomes unusable offline |
| full canonical/provider snapshot | excessive for CRM, current canonical is not licence-complete, and it increases stale/personal-data burden |
| minimal useful snapshot + source identity | recommended, conditional on licence clarification |
| persistent JobOffer catalogue linked to Opportunity | rejects explicit minimal scope and introduces synchronization/autonomy |

### Exact recommended minimal snapshot

Seed existing user-editable CRM fields with:

- title;
- company name and normalized Company relationship;
- location label;
- canonical contract type plus raw contract label;
- raw salary text;
- canonical remote mode only when known;
- safe source URL;
- empty raw/normalized description in V1 rather than copying provider description.

Persist immutable/machine provenance with:

- source provider key;
- source external ID;
- provider publication timestamp;
- provider update timestamp;
- source fetch timestamp.

Existing Opportunity/Application `createdAt` is `savedAt`. Do not create a misleading
`discoveredAt`: the stateless save request cannot prove the original search-render time without
adding signed/session state.

This snapshot remains understandable if the provider listing disappears: “Backend Java Developer
at Company X, location/contract/salary as saved.” It is intentionally not another job-board copy.

After creation, title/company/location/contract/salary are user-maintained CRM fields. Future source
checks may report changes but must never overwrite them automatically. Provider key, external ID,
and provider timestamps are provenance and should not be editable through the normal CRM form.

## Description / External Content Safety

S3 truncates and strips provider HTML, then renders with escaped `th:text`; the escaping is the
actual XSS boundary (`JobDiscoveryViewModels.java:70-101`, `job-discovery-results.html:60-62`).

**RECOMMENDED V1** — do not automatically persist France Travail description content while licence
and personal-data retention remain unresolved. Store no provider HTML; existing non-null description
columns can contain empty strings, as manual creation already does when description is blank
(`ApplicationCrmService.java:333-335`).

If later licence clarification explicitly permits/needs description retention, convert it to plain
text with a robust parser before persistence and always render through escaped text. Never persist or
render unsafe raw provider HTML, and never use `th:utext` for it.

## Source Provenance

### Existing concepts

- `sourceType` only distinguishes `MANUAL` and `URL`.
- `source` is free text for display/import.
- `sourceUrl` is the offer URL.

These are not sufficient for machine identity. Do not add `FRANCE_TRAVAIL` to
`OpportunitySourceType`; that enum describes input shape, not provider identity.

### Recommended provider-neutral fields

On `career_opportunity`:

- nullable `source_provider`;
- nullable `source_external_id`;
- nullable `source_published_at`;
- nullable `source_updated_at`;
- nullable `source_fetched_at`.

Reuse existing `source_url` and set display `source` to “France Travail” through provider metadata.
Future providers populate the same columns. Manual and legacy Opportunities leave all provider
identity/date columns null.

The source URL must be accepted only when URI parsing succeeds and scheme is `http` or `https`.
Render external links with escaped attributes and `rel="noopener noreferrer"`; add
`target="_blank"` only if desired. The current CRM link already uses `rel`; discovery validates the
scheme but omits `rel`.

## Duplicate Identity

`provider + externalOfferId` is the correct identity because:

- the canonical JobOffer exposes both;
- the official detail endpoint looks up by ID;
- title/company can repeat and can change;
- URLs can change or carry tracking parameters;
- the provider namespace prevents cross-provider collisions.

Repository evidence proves that the ID is the provider lookup identity for an active offer. It does
not prove indefinite non-reuse after deletion. Provider namespacing and retained provenance are the
best available V1 boundary.

Manual Opportunities have no provider identity and must not participate in this uniqueness rule.
Do not deduplicate manual records by title/company.

## Duplicate Protection

Use both layers:

1. Application check for a friendly idempotent result.
2. Database unique constraint on `(source_provider, source_external_id)` for double-click, two tabs,
   and concurrent requests.

The application result should be “already added” and return the existing CRM Application ID, not a
500 error. The transactional create operation must flush/commit independently; an outer
orchestrator catches a source-identity conflict after that transaction rolls back, then re-reads the
winner in a fresh read transaction. Do not attempt recovery inside the rollback-only transaction.

MySQL unique indexes permit multiple rows with nullable components in commonly deployed versions,
but the deployed server version is not pinned. The future Story must verify exact behavior against
production MySQL, not only H2 MySQL mode. Enforce the all-or-none provider identity invariant in the
application and, if supported by the production MySQL version, a check constraint.

Existing deletion semantics require an explicit idempotency rule:

- existing Opportunity with an active or archived Application: return that Application;
- existing source Opportunity whose Application was deleted: attach one new `NOT_CONTACTED`
  Application to the existing Opportunity rather than creating a duplicate Opportunity;
- never create a second Opportunity for the same source identity.

The orphan-repair path must pessimistically lock the existing Opportunity row while checking for and
creating its replacement Application. The Opportunity unique constraint alone does not prevent two
requests from attaching two Applications to the same existing orphan, and imposing global
one-Application-per-Opportunity uniqueness could break current generic domain semantics or existing
data without prior validation.

The race matters even for a mono-user application because a browser can submit twice. A database
constraint is cheaper and safer than distributed locking.

## Opportunity Initial Status

- Opportunity: existing `DRAFT`.
- CRM Application: existing `NOT_CONTACTED`.
- Priority: existing `MEDIUM` default.
- Interview: existing `NONE` default.
- Decision: existing `PENDING` default.
- `appliedAt`: null.

No new status is required. `APPLIED` would falsely state that an application was sent. The initial
history should use a user-originated source/comment indicating explicit creation from a discovered
offer; it should not claim an automated import.

## Eligibility Persistence

Do not persist the verdict or reasons on Opportunity or Application.

Eligibility depends on mutable JobSearchPreferences, partially on future ProfessionalProfile use,
and on evaluator/mapping versions. A stored verdict would become stale and would require input/rule
versioning to remain meaningful. It is not part of the CRM aggregate's lifecycle.

For save-time UX, the server must recompute eligibility from the refetched offer and current
preferences so a changed offer receives the current warning/result. It must not block creation and
must not be stored. If preferences disappeared between rendering and save, allow the explicit action
with an “eligibility not re-evaluated” warning. If the product later needs “why this looked relevant
when saved,” that is an audit/event feature requiring explicit versioned context, not an Opportunity
field in S4.

No ProfessionalProfile or JobSearchPreferences data should be copied. The selected role is already
represented by the offer title; search context is not required provenance for the CRM record.

## Human Authority

All statuses are saveable:

- `ELIGIBLE`: allow.
- `REVIEW_REQUIRED`: allow with the existing visible uncertainty reasons.
- `INELIGIBLE`: allow with a clear warning that the current preferences produced a deterministic
  rejection and that the user is choosing to continue.

The evaluator assists prioritization; it is not authorization. Blocking `INELIGIBLE` would make
mutable/incomplete preferences override human judgment and contradict the product principle.

The warning can be on the result card and repeated as a success flash after save. A second blocking
confirmation dialog is optional and not required for V1.

## France Travail Licence Considerations

### Technically possible

The application can technically store every current canonical field, including description,
company, title, salary, competencies, URL, provider ID, and dates. Technical ability is not licence
permission.

### Supported by current repository evidence

The prior official-source investigation records these obligations as observed
(`docs/investigations/job-discovery-vertical-slice.md:301-322`):

- accessible France Travail attribution, last-update date, and licence link;
- at least daily API reconciliation for matching reuse, reflecting creation/update/deletion;
- retention of first publication/update dates;
- complete supplied content on a disseminated offer;
- disclosure of method/transformations;
- deletion/anonymization and personal-data duties for retained deleted offers.

It also explicitly states that the non-technical gates do not block live non-persistent S3 but must
be resolved before any persistence (`:526-538`).

### Current uncertainty

**NOT VERIFIED** — whether a local/private, explicitly user-selected CRM working record is treated
like a persisted/disseminated offer or matching database under those clauses.

**NOT VERIFIED** — whether minimal title/company/context retention is permitted without complete
content and daily reconciliation.

**NOT VERIFIED** — permitted retention duration and required behavior when a selected source offer
is deleted.

The current official product documentation says API use is subject to the specific reuse licence,
but Context7 did not surface the detailed clauses and the official licence page is client-rendered.
The repository's 2026-08-26 official-source findings remain the strongest retained evidence.

### Decision

Classification: **BLOCKING_UNCERTAINTY**.

The safest current persistence strategy is no France Travail persistence until France Travail or
qualified legal review answers the selected-private-CRM case. Reference-only persistence is not a
safe workaround: it fails the product's offline/history requirement and is not proven exempt from
reuse conditions.

If the obligations apply unchanged, a compliant feature would require full-content modelling,
attribution, daily reconciliation, disappearance/anonymization policy, and monitoring. That is not
the bounded, non-autonomous S4 requested here, so another Story cannot simply hide those duties.

## Failure Semantics

| Failure | Recommended user-visible behavior |
|---|---|
| already saved | redirect to existing CRM detail; flash “Déjà ajoutée aux opportunités” |
| provider unavailable/timeout/auth/5xx | remain safe, create nothing, invite retry later |
| offer disappeared (`204`) | explain it is no longer available; link to manual CRM creation |
| unknown provider | reject as invalid request; create nothing |
| malformed/oversized external ID | validation error; create nothing |
| invalid source URL from provider | omit the URL but keep trusted identity; do not render an unsafe link |
| missing title | fail conversion and offer manual creation |
| missing company required by current CRM | fail conversion and offer manual creation in minimal V1 |
| missing/invalid provider publication or update date | fail provider-attributed conversion while retained licence evidence requires those dates |
| provider field exceeds a destination bound | fail conversion without truncating; offer validated manual creation |
| persistence/constraint failure | roll back Opportunity/Application/history; show generic error; do not expose SQL/provider data |

Provider unavailability must not silently downgrade to hidden-form data. Manual creation is an
explicit user-owned fallback and should not claim trusted provider provenance.

## Transaction Boundary

Do not hold a database write transaction open during OAuth or provider HTTP calls.

```text
provider refetch (no write transaction)
        -> map and validate
        -> transactional local create/find
        -> commit
        -> existing after-commit projection
```

Current CRM convention already puts aggregate mutations in application services and Google calls
after commit. The future use case should preserve that convention. The database unique constraint,
not a long transaction or lock around the external request, closes concurrent source-Opportunity
creation. A short pessimistic lock on an existing source Opportunity closes the orphan-Application
repair race.

## UX

Minimal result-card change after the licence gate:

```text
[Ajouter à mes opportunités]
```

- Render it for all eligibility statuses.
- POST only provider key + provider offer ID.
- Disable the clicked button client-side as convenience, not correctness.
- On creation, redirect to the existing Application detail/edit page with a success flash.
- On duplicate, redirect to the same existing detail with an “already added” flash.
- The destination allows immediate edits, notes, documents, status changes, and follow-up planning.
- Do not redesign discovery or turn CRM detail into a job-board page.

Minimal CRM additions:

- “Source: France Travail”;
- “Voir l'offre originale” when the stored URL is safe;
- no ROME, competency, eligibility, or provider-metadata dashboard redesign.

The action must remain absent from the search use case itself. Search tests should explicitly prove
that rendering results creates no Opportunity/Application.

## Existing CRM Integration

Discovery-created records should reuse:

- Company find/create behavior;
- Opportunity status `DRAFT`;
- Application defaults and `NOT_CONTACTED` status;
- initial status history;
- existing detail/edit/list/dashboard views;
- after-commit Google Sheets projection.

It should not pass a fabricated `ApplicationForm` through the web layer. Extract/reuse a domain
creation primitive with a validated command so both manual and discovered creation enforce the same
aggregate invariants.

The current Opportunity source fields and application view models need small additive display
support, not a new CRM module.

## Future Provider Compatibility

The design remains provider-neutral because:

- durable identity uses string provider namespace + external ID;
- mapping consumes canonical `JobOffer`, not a France Travail DTO;
- lookup is a small capability separate from search;
- provider-specific authentication/endpoints remain in infrastructure;
- no `FranceTravailOpportunity` or provider-specific Opportunity enum is introduced.

A future provider without direct lookup cannot use the same trusted-refetch save path. It can supply
another trustworthy capability later (for example a signed provider payload), but search support
alone should not falsely imply save support.

Source identity leaves a seam for future manual “check changed/expired” behavior. No synchronization,
last-seen state, or overwrite policy belongs in S4.

## Future Company Prospect Compatibility

The current Opportunity is position-centric: it has title, job description/source, analyses,
letters, and applications. Manual CRM creation requires company and job title. A company with no
visible role is not naturally this aggregate.

Likely direction: a future `CompanyCandidate` selected for spontaneous outreach should become a
separate Prospect/lead concept, possibly related to `CompanyEntity`, until a target role or
application is created. It should not become a fake Opportunity with an invented title. This is a
directional boundary only; Company Intelligence is not designed here.

## Production / Migration Safety

### Conditional schema evolution

S4 requires schema evolution if the licence gate clears:

- add nullable source provider/external-ID/publication/update/fetch columns to
  `career_opportunity`;
- add a named unique constraint/index on `(source_provider, source_external_id)`;
- optionally add an all-or-none check constraint after production MySQL version verification;
- add a repository lookup by provider identity.

No new JobOffer table is required.

The columns stay nullable for legacy/manual compatibility, but the discovered-offer command must
require both provider identity values and valid publication/update/fetch timestamps while the
retained licence evidence requires those dates.

### Backward compatibility

- Existing and manual Opportunities keep all new columns null.
- Do not backfill from `source` or `source_url`; neither proves provider identity.
- Multiple null manual rows must remain valid.
- Existing `sourceType` values and current manual/analysis/letter flows remain unchanged.
- Deploy migration before code that writes the columns; old code ignores additive nullable fields.
- Rollback is code-first: stop new writes/read dependencies, then drop the unique constraint and
  columns only if data loss is accepted. A normal rollback should leave columns in place.
- Verify migration and nullable unique behavior against the actual production MySQL version.

Production risk is **MEDIUM**: the additive migration is mechanically low risk, but uniqueness,
existing orphan semantics, provider calls, and unresolved licence obligations materially affect real
CRM data.

## Security

- Provider-controlled strings must be treated as untrusted content even after authenticated fetch.
- Use plain text and escaped rendering; no raw provider HTML.
- Validate source URL scheme as HTTP(S); invalid URL must not block identity persistence or become a
  link.
- Validate provider key and external ID length/characters before using them in lookup/logging.
- Do not trust browser-submitted provider content or eligibility.
- Database uniqueness protects duplicate identity; it is not authorization.
- Current app has no user ownership, authentication, authorization, or Spring Security CSRF. Keep
  S4 local-only under the current product assumption or require a separate security Story before
  network exposure.
- Do not log descriptions, profile data, preferences, tokens, or provider response bodies.

## Observability

Minimal structured logs, without full content:

- opportunity/application created from provider: provider key, external ID, local IDs;
- duplicate save prevented/resolved: provider key, external ID, existing local IDs;
- provider offer disappeared before save: provider key, external ID;
- provider lookup failed: provider key, normalized error category, no token/body.

No audit subsystem is needed for V1. Existing Application status history records the created CRM
state; source provenance records origin.

## Product Metrics

Future metrics that would evaluate the feature, without implementation now:

- discovery result cards viewed;
- offers explicitly saved;
- save conversion rate by provider;
- eligibility status at click/save time as transient metric, not CRM state;
- duplicate-save attempts;
- provider lookup disappearance/failure rate;
- time from Opportunity saved to `APPLIED`.

Do not add telemetry infrastructure in S4 solely for these metrics.

## Test Strategy

Future Story tests should include:

### Mapping and provenance

- canonical JobOffer maps exact minimal fields;
- provider key/external ID and provider/fetch dates persist;
- missing/malformed required provider dates reject provider-attributed creation;
- oversized provider fields reject without silent truncation;
- description, competencies, ROME, eligibility, profile, and preferences do not persist;
- manual Opportunity creation remains unchanged with null provider identity;
- source URL accepts HTTP/HTTPS and omits unsafe/malformed schemes;
- provider description/strings render as escaped text.

### Trust and lookup

- save command submits/accepts only provider key + external ID;
- selected offer is refetched by ID;
- browser-supplied extra fields cannot influence persistence;
- provider unavailable creates nothing;
- `204` disappeared offer creates nothing and returns manual fallback;
- invalid/unknown provider and invalid ID create nothing.

### CRM semantics

- new Opportunity starts `DRAFT`;
- new Application starts `NOT_CONTACTED`, medium priority, no applied date;
- creation records history and returns Application detail ID;
- existing Company resolution and after-commit projection remain active;
- saved fields remain user-editable and no later provider read overwrites them.

### Eligibility and human authority

- `ELIGIBLE` saves;
- `REVIEW_REQUIRED` saves;
- `INELIGIBLE` saves and warns;
- eligibility is recomputed after refetch, and missing preferences produce a non-blocking warning;
- no eligibility status/reasons persist;
- profile/preference changes do not mutate saved Opportunity.

### Duplicates and concurrency

- repeated click returns existing CRM item;
- two concurrent requests create one source Opportunity;
- database unique violation rolls back its create transaction and is translated by a fresh
  transaction into an idempotent existing result;
- manual rows with null provider identity can repeat;
- same external ID under two providers is allowed;
- concurrent saves against a deleted-Application orphan lock/reuse its Opportunity and create only
  one new CRM Application;
- archived existing Application is returned rather than duplicated.

### Integration and non-regression

- controller/use-case/provider/CRM integration with mocked lookup;
- search rendering alone persists no Opportunity/Application;
- no provider HTTP call occurs inside the local write transaction;
- migration works with existing rows and production-compatible MySQL;
- application starts with France Travail disabled/no credentials.

## Risks

| Risk | Severity | Control |
|---|---|---|
| licence scope applies to selected CRM snapshots | blocking | written clarification before Story |
| hidden-field tampering | high | ID-only command + server refetch |
| duplicate double-click/race | medium | application idempotency + DB uniqueness |
| orphan Opportunity receives duplicate Applications | medium | lock source Opportunity during repair |
| provider disappears between search/save | expected | no save; clear manual fallback |
| provider outage prevents save | medium | retry later; no untrusted fallback |
| unsafe URL/HTML | high | HTTP(S) allowlist, plain text, escaped rendering |
| stale eligibility | medium | do not persist; advisory only |
| existing CRM naming conflates lead/application | low for V1 | use existing `NOT_CONTACTED`; document semantics |
| missing provider company | medium | fail to manual creation in bounded V1 |
| source sync overwrites user edits | high future | no sync/overwrite in S4; immutable provenance only |
| shared Company edit changes another CRM item | medium, existing | document/test inherited behavior; fix separately |
| production migration behavior differs from H2 | medium | production-MySQL migration test/version check |
| unauthenticated/CSRF-exposed deployment | high if networked | preserve local-only assumption or separate security prerequisite |

## Open Questions

Only these questions block Story creation:

1. Does the France Travail reuse licence permit a private, explicitly user-selected CRM record to
   retain a minimal title/company/context snapshot without complete-content dissemination and
   automatic <=24-hour reconciliation?
2. What deletion, anonymization, attribution, method-disclosure, and retention duties apply to that
   selected CRM record?
3. Is automatic omission of provider description/contact/competencies permitted for that use, or
   does persistence require the full provider disclosure model?

Non-blocking implementation clarifications for the Story:

- exact maximum lengths for provider key/external ID based on provider contracts;
- actual production MySQL version and check-constraint support;
- whether missing-company offers are excluded in V1 or routed through a review form;
- exact French flash/error copy.

## Challenge Answers

1. **Is JobOffer -> Opportunity the right next Story?** Yes, technically and product-wise. It closes
   the explicit human-selection loop without autonomous collection. It must also create the existing
   `NOT_CONTACTED` Application to be CRM-managed.
2. **Snapshot or reference?** Minimal useful snapshot plus provider-neutral source identity.
3. **Refetch on save?** Yes. It is the smallest trustworthy server-side strategy.
4. **If refetch fails?** Create nothing; explain retry or disappeared offer and offer manual entry.
5. **Duplicates?** `(source_provider, source_external_id)`, application idempotency plus DB unique
   constraint.
6. **Can INELIGIBLE save?** Yes, with warning; human authority wins.
7. **Persist eligibility?** No. Recompute after refetch for the save-time warning; never store it in
   CRM.
8. **Schema evolution?** Yes: nullable provider identity and source timestamps plus uniqueness.
9. **Are licence constraints blocking?** Yes, the selected-private-CRM interpretation is unresolved
   and the repository explicitly gates persistence on clarification.
10. **Provider-neutral?** Yes. Canonical mapping and string source namespace avoid a
    France-Travel-specific Opportunity.

## Recommended S4 Scope

**Conditional ONE bounded Story: Explicitly save a live discovered offer into the existing CRM.**

After the licence gate clears, include only:

- one result-card POST action for all statuses;
- provider key + external ID request;
- France Travail detail lookup-by-ID and canonical mapping;
- dedicated application orchestration use case;
- minimal snapshot mapping, with no provider description;
- provider-neutral Opportunity provenance columns;
- application and database duplicate protection;
- atomic Opportunity `DRAFT` + Application `NOT_CONTACTED` creation using existing CRM behavior;
- redirect/idempotent redirect to existing Application detail;
- source label/original-link display;
- tests listed above that are directly in scope.

Explicitly exclude:

- persisted JobOffer catalogue;
- background refresh/synchronization;
- autonomous collection;
- multi-provider aggregation;
- ranking or evaluator changes;
- eligibility/profile/preferences snapshots;
- full provider-content storage;
- Company Intelligence/Prospect design;
- analytics infrastructure;
- new authentication architecture.

If licence clarification says daily reconciliation/full-content/deletion duties apply, this bounded
S4 is **NO-GO** and must be replaced by a separately scoped compliant persistence capability or a
different provider/data strategy. Do not smuggle synchronization into this Story.

## Go / No-Go

**NEED_MORE_INVESTIGATION**

Reason: architecture, trust, mapping, duplicate, UX, status, transaction, schema, and test boundaries
are sufficiently determined. The remaining France Travail licence/retention question is external,
material, and explicitly blocks persistence under the repository's retained evidence. An ADR is not
needed before that fact is known; an ADR may record the accepted policy afterward.
