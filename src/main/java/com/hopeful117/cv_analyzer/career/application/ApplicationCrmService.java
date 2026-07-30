package com.hopeful117.cv_analyzer.career.application;

import com.hopeful117.cv_analyzer.career.domain.*;
import com.hopeful117.cv_analyzer.career.persistence.*;
import com.hopeful117.cv_analyzer.career.web.ApplicationForm;
import com.hopeful117.cv_analyzer.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

import static com.hopeful117.cv_analyzer.career.application.CrmViewModels.*;

@Service
@RequiredArgsConstructor
public class ApplicationCrmService {
    private static final Set<ApplicationStatus> CLOSED =
            EnumSet.of(ApplicationStatus.REJECTED, ApplicationStatus.SUCCESS, ApplicationStatus.ARCHIVED);
    private static final Set<ApplicationStatus> SENT =
            EnumSet.of(ApplicationStatus.APPLIED, ApplicationStatus.WAITING,
                    ApplicationStatus.INTERVIEW, ApplicationStatus.FOLLOWED_UP,
                    ApplicationStatus.REJECTED, ApplicationStatus.SUCCESS);

    private final CompanyRepository companyRepository;
    private final OpportunityRepository opportunityRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final ExternalProjectionRepository projectionRepository;
    private final ResumeVersionRepository resumeVersionRepository;
    private final CoverLetterRepository coverLetterRepository;
    private final ResumeAnalysisRecordRepository analysisRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long create(ApplicationForm form, ChangeSource source) {
        return createInternal(form, source, null);
    }

    @Transactional
    public Long createImported(ApplicationForm form, String legacyExternalId) {
        return createInternal(form, ChangeSource.IMPORT, clean(legacyExternalId));
    }

    @Transactional
    public Long createImportedFromGoogleSheet(ApplicationForm form, int rowNumber) {
        return createInternal(form, ChangeSource.IMPORT, "GOOGLE-SHEET-ROW:" + rowNumber, false);
    }

    private Long createInternal(ApplicationForm form, ChangeSource source, String legacyExternalId) {
        return createInternal(form, source, legacyExternalId, true);
    }

    private Long createInternal(ApplicationForm form, ChangeSource source, String legacyExternalId,
                                boolean publishProjection) {
        CompanyEntity company = findOrCreateCompany(form);
        OpportunityEntity opportunity = createOpportunity(form, company);
        opportunityRepository.save(opportunity);

        ApplicationEntity application = new ApplicationEntity();
        application.setOpportunity(opportunity);
        applyForm(application, form, false);
        application.setLegacyExternalId(legacyExternalId);
        applicationRepository.save(application);
        recordHistory(application, null, application.getStatus(), source, "Création de la candidature");
        if (publishProjection) {
            eventPublisher.publishEvent(new ApplicationChangedEvent(application.getId()));
        }
        return application.getId();
    }

    @Transactional(readOnly = true)
    public boolean isPotentialDuplicate(String legacyExternalId, String company, String title,
                                        LocalDate appliedAt) {
        if (hasText(legacyExternalId) &&
                applicationRepository.existsByLegacyExternalId(legacyExternalId.trim())) {
            return true;
        }
        return applicationRepository
                .existsByOpportunityCompanyNameIgnoreCaseAndOpportunityTitleIgnoreCaseAndAppliedAt(
                        company, title, appliedAt);
    }

    @Transactional
    public void update(long id, ApplicationForm form) {
        ApplicationEntity application = requireApplication(id);
        ApplicationStatus previous = application.getStatus();
        updateCompany(application.getOpportunity().getCompany(), form);
        updateOpportunity(application.getOpportunity(), form);
        applyForm(application, form, true);
        if (previous != application.getStatus()) {
            recordHistory(application, previous, application.getStatus(), ChangeSource.USER, "Modification");
        }
        eventPublisher.publishEvent(new ApplicationChangedEvent(id));
    }

    @Transactional
    public void changeStatus(long id, ApplicationStatus newStatus, String comment) {
        ApplicationEntity application = requireApplication(id);
        changeStatus(application, newStatus, ChangeSource.USER, comment);
        eventPublisher.publishEvent(new ApplicationChangedEvent(id));
    }

    @Transactional
    public void planFollowUp(long id, LocalDate plannedAt) {
        if (plannedAt == null) {
            throw new IllegalArgumentException("La date de relance est obligatoire.");
        }
        ApplicationEntity application = requireApplication(id);
        application.setFollowUpPlannedAt(plannedAt);
        eventPublisher.publishEvent(new ApplicationChangedEvent(id));
    }

    @Transactional
    public void recordFollowUp(long id, LocalDate performedAt, String comment) {
        ApplicationEntity application = requireApplication(id);
        application.setLastFollowUpAt(performedAt == null ? LocalDate.now() : performedAt);
        changeStatus(application, ApplicationStatus.FOLLOWED_UP, ChangeSource.USER, comment);
        eventPublisher.publishEvent(new ApplicationChangedEvent(id));
    }

    @Transactional(readOnly = true)
    public Page<ApplicationListItem> search(String query, ApplicationStatus status,
                                            ApplicationPriority priority, boolean followUpsDue,
                                            boolean sentOnly, int page, int size, String direction) {
        Specification<ApplicationEntity> specification =
                (root, ignored, cb) -> cb.conjunction();
        if (hasText(query)) {
            String pattern = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, ignored, cb) -> cb.or(
                    cb.like(cb.lower(root.join("opportunity").join("company").get("name")), pattern),
                    cb.like(cb.lower(root.join("opportunity").get("title")), pattern)));
        }
        if (status != null) {
            specification = specification.and((root, ignored, cb) -> cb.equal(root.get("status"), status));
        }
        if (priority != null) {
            specification = specification.and((root, ignored, cb) -> cb.equal(root.get("priority"), priority));
        }
        if (followUpsDue) {
            specification = specification.and((root, ignored, cb) -> cb.and(
                    cb.lessThanOrEqualTo(root.get("followUpPlannedAt"), LocalDate.now()),
                    root.get("status").in(CLOSED).not()));
        }
        if (sentOnly) {
            specification = specification.and(
                    (root, ignored, cb) -> root.get("status").in(SENT));
        }
        Sort sort = Sort.by("updatedAt");
        sort = "asc".equalsIgnoreCase(direction) ? sort.ascending() : sort.descending();
        return applicationRepository.findAll(specification,
                PageRequest.of(Math.max(0, page), Math.clamp(size, 1, 50), sort)).map(this::toListItem);
    }

    @Transactional(readOnly = true)
    public ApplicationDetails getDetails(long id) {
        ApplicationEntity application = requireApplication(id);
        OpportunityEntity opportunity = application.getOpportunity();
        CompanyEntity company = opportunity.getCompany();
        List<StatusHistoryItem> history = historyRepository
                .findByApplicationIdOrderByChangedAtDesc(id).stream().map(this::toHistory).toList();
        ProjectionView projection = projectionRepository
                .findFirstByResourceTypeAndResourceIdOrderByUpdatedAtDesc("APPLICATION", id)
                .map(this::toProjection).orElse(null);
        return new ApplicationDetails(application.getId(), opportunity.getId(),
                company == null ? null : company.getId(), companyName(opportunity),
                company == null ? null : company.getCity(), company == null ? null : company.getAddress(),
                company == null ? null : company.getPhone(), company == null ? null : company.getEmail(),
                company == null ? null : company.getWebsite(), opportunity.getTitle(),
                opportunity.getSourceUrl(), opportunity.getContractType(), opportunity.getContractTypeRaw(),
                opportunity.getWorkSchedule(), opportunity.getWorkScheduleRaw(), opportunity.getRemoteMode(),
                opportunity.getSource(), opportunity.getSalaryText(), opportunity.getDistanceText(),
                opportunity.getLocation(), opportunity.getNormalizedDescription(), statusOf(application),
                priorityOf(application), application.getAppliedAt(), application.getFollowUpPlannedAt(),
                application.getLastFollowUpAt(), interviewOf(application), decisionOf(application),
                application.isPortfolioSent(), application.getNotes(), application.getPrivateNotes(),
                id(application.getResumeVersion()), application.getResumeVersion() == null ? null
                        : application.getResumeVersion().getVersionNumber(),
                id(application.getCoverLetter()), id(application.getAnalysis()),
                application.getAnalysis() == null ? null : application.getAnalysis().getOverallScore(),
                application.getCreatedAt(), application.getUpdatedAt(), history, projection);
    }

    @Transactional(readOnly = true)
    public ApplicationForm getForm(long id) {
        ApplicationEntity application = requireApplication(id);
        OpportunityEntity opportunity = application.getOpportunity();
        CompanyEntity company = opportunity.getCompany();
        ApplicationForm form = new ApplicationForm();
        form.setCompanyName(companyName(opportunity));
        if (company != null) {
            form.setCity(company.getCity());
            form.setAddress(company.getAddress());
            form.setPhone(company.getPhone());
            form.setEmail(company.getEmail());
            form.setWebsite(company.getWebsite());
        }
        form.setJobTitle(opportunity.getTitle());
        form.setOfferUrl(opportunity.getSourceUrl());
        form.setContractType(opportunity.getContractType());
        form.setContractTypeRaw(opportunity.getContractTypeRaw());
        form.setWorkSchedule(opportunity.getWorkSchedule());
        form.setWorkScheduleRaw(opportunity.getWorkScheduleRaw());
        form.setRemoteMode(opportunity.getRemoteMode());
        form.setSource(opportunity.getSource());
        form.setSalaryText(opportunity.getSalaryText());
        form.setDistanceText(opportunity.getDistanceText());
        form.setLocation(opportunity.getLocation());
        form.setDescription(opportunity.getNormalizedDescription());
        form.setStatus(statusOf(application));
        form.setPriority(priorityOf(application));
        form.setAppliedAt(application.getAppliedAt());
        form.setFollowUpPlannedAt(application.getFollowUpPlannedAt());
        form.setLastFollowUpAt(application.getLastFollowUpAt());
        form.setInterviewStatus(interviewOf(application));
        form.setDecision(decisionOf(application));
        form.setPortfolioSent(application.isPortfolioSent());
        form.setNotes(application.getNotes());
        form.setPrivateNotes(application.getPrivateNotes());
        form.setResumeVersionId(id(application.getResumeVersion()));
        form.setCoverLetterId(id(application.getCoverLetter()));
        form.setAnalysisId(id(application.getAnalysis()));
        return form;
    }

    @Transactional(readOnly = true)
    public FormOptions getFormOptions() {
        List<DocumentOption> versions = resumeVersionRepository.findAll().stream()
                .sorted(Comparator.comparing(ResumeVersionEntity::getCreatedAt).reversed())
                .limit(100)
                .map(v -> new DocumentOption(v.getId(), "CV v" + v.getVersionNumber() + " · "
                        + fallback(v.getProfessionalTitle(), "Sans titre"))).toList();
        List<DocumentOption> letters = coverLetterRepository.findAll().stream()
                .sorted(Comparator.comparing(CoverLetterEntity::getUpdatedAt).reversed())
                .limit(100)
                .map(l -> new DocumentOption(l.getId(), "Lettre · "
                        + fallback(l.getOpportunity().getTitle(), "Sans titre"))).toList();
        List<DocumentOption> analyses = analysisRepository.findAll().stream()
                .sorted(Comparator.comparing(ResumeAnalysisRecordEntity::getCreatedAt).reversed())
                .limit(100)
                .map(a -> new DocumentOption(a.getId(), "Analyse " + a.getOverallScore() + "/100 · "
                        + fallback(a.getOpportunity().getTitle(), "Sans titre"))).toList();
        return new FormOptions(versions, letters, analyses);
    }

    @Transactional(readOnly = true)
    public CrmDashboard dashboard() {
        List<ApplicationListItem> recent = applicationRepository.findAllByOrderByUpdatedAtDesc(
                PageRequest.of(0, 5)).stream().map(this::toListItem).toList();
        List<ApplicationListItem> followUps = applicationRepository
                .findByFollowUpPlannedAtLessThanEqualAndStatusNotInOrderByFollowUpPlannedAtAsc(
                        LocalDate.now(), CLOSED, PageRequest.of(0, 5)).stream().map(this::toListItem).toList();
        List<ApplicationListItem> priorities = applicationRepository
                .findByPriorityAndStatusNotInOrderByUpdatedAtDesc(
                        ApplicationPriority.HIGH, CLOSED, PageRequest.of(0, 5))
                .stream().map(this::toListItem).toList();
        List<StatusHistoryItem> transitions = historyRepository.findAllByOrderByChangedAtDesc(
                PageRequest.of(0, 5)).stream().map(this::toHistory).toList();
        return new CrmDashboard(opportunityRepository.count(),
                applicationRepository.countByStatusIn(SENT),
                applicationRepository.countByStatus(ApplicationStatus.WAITING),
                applicationRepository.countByStatus(ApplicationStatus.INTERVIEW),
                applicationRepository.countByFollowUpPlannedAtLessThanEqualAndStatusNotIn(LocalDate.now(), CLOSED),
                applicationRepository.countByStatus(ApplicationStatus.REJECTED),
                applicationRepository.countByStatus(ApplicationStatus.SUCCESS),
                projectionRepository.countByStatus(ProjectionStatus.FAILED),
                recent, followUps, priorities, transitions);
    }

    @Transactional
    public void delete(long id) {
        ApplicationEntity application = requireApplication(id);
        applicationRepository.delete(application);
    }

    @Transactional(readOnly = true)
    public List<ApplicationEntity> findAllForProjection() {
        return applicationRepository.findAll(Sort.by("id"));
    }

    private CompanyEntity findOrCreateCompany(ApplicationForm form) {
        String name = form.getCompanyName().trim();
        String city = clean(form.getCity());
        return companyRepository.findExactCandidates(name, city == null ? "" : city).stream().findFirst()
                .orElseGet(() -> {
                    CompanyEntity company = new CompanyEntity();
                    updateCompany(company, form);
                    return companyRepository.save(company);
                });
    }

    private OpportunityEntity createOpportunity(ApplicationForm form, CompanyEntity company) {
        OpportunityEntity opportunity = new OpportunityEntity();
        opportunity.setCompany(company);
        opportunity.setCompanyName(company.getName());
        opportunity.setSourceType(hasText(form.getOfferUrl())
                ? OpportunitySourceType.URL : OpportunitySourceType.MANUAL);
        opportunity.setDetectedLanguage(null);
        opportunity.setStatus(OpportunityStatus.DRAFT);
        updateOpportunity(opportunity, form);
        return opportunity;
    }

    private void updateCompany(CompanyEntity company, ApplicationForm form) {
        if (company == null) {
            return;
        }
        company.setName(form.getCompanyName().trim());
        company.setCity(clean(form.getCity()));
        company.setAddress(clean(form.getAddress()));
        company.setPhone(clean(form.getPhone()));
        company.setEmail(clean(form.getEmail()));
        company.setWebsite(clean(form.getWebsite()));
    }

    private void updateOpportunity(OpportunityEntity opportunity, ApplicationForm form) {
        opportunity.setTitle(form.getJobTitle().trim());
        opportunity.setCompanyName(form.getCompanyName().trim());
        opportunity.setSourceUrl(clean(form.getOfferUrl()));
        opportunity.setContractType(form.getContractType());
        opportunity.setContractTypeRaw(clean(form.getContractTypeRaw()));
        opportunity.setWorkSchedule(form.getWorkSchedule());
        opportunity.setWorkScheduleRaw(clean(form.getWorkScheduleRaw()));
        opportunity.setRemoteMode(form.getRemoteMode() == null ? RemoteMode.UNSPECIFIED : form.getRemoteMode());
        opportunity.setSource(clean(form.getSource()));
        opportunity.setSalaryText(clean(form.getSalaryText()));
        opportunity.setDistanceText(clean(form.getDistanceText()));
        opportunity.setLocation(clean(form.getLocation()));
        String description = clean(form.getDescription());
        opportunity.setRawDescription(description == null ? "" : description);
        opportunity.setNormalizedDescription(description == null ? "" : description);
    }

    private void applyForm(ApplicationEntity application, ApplicationForm form, boolean update) {
        application.setStatus(form.getStatus() == null ? ApplicationStatus.NOT_CONTACTED : form.getStatus());
        application.setPriority(form.getPriority() == null ? ApplicationPriority.MEDIUM : form.getPriority());
        application.setAppliedAt(form.getAppliedAt());
        application.setFollowUpPlannedAt(form.getFollowUpPlannedAt());
        application.setLastFollowUpAt(form.getLastFollowUpAt());
        application.setInterviewStatus(form.getInterviewStatus() == null
                ? InterviewStatus.NONE : form.getInterviewStatus());
        application.setDecision(form.getDecision() == null ? ApplicationDecision.PENDING : form.getDecision());
        application.setPortfolioSent(form.isPortfolioSent());
        application.setNotes(clean(form.getNotes()));
        application.setPrivateNotes(clean(form.getPrivateNotes()));
        application.setResumeVersion(optional(form.getResumeVersionId(), resumeVersionRepository, "Version de CV"));
        application.setCoverLetter(optional(form.getCoverLetterId(), coverLetterRepository, "Lettre"));
        application.setAnalysis(optional(form.getAnalysisId(), analysisRepository, "Analyse"));
        if (!update && SENT.contains(application.getStatus()) && application.getAppliedAt() == null) {
            application.setAppliedAt(LocalDate.now());
        }
    }

    private void changeStatus(ApplicationEntity application, ApplicationStatus newStatus,
                              ChangeSource source, String comment) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Le statut est obligatoire.");
        }
        ApplicationStatus previous = application.getStatus();
        if (previous == newStatus) {
            return;
        }
        application.setStatus(newStatus);
        if (SENT.contains(newStatus) && application.getAppliedAt() == null) {
            application.setAppliedAt(LocalDate.now());
        }
        recordHistory(application, previous, newStatus, source, clean(comment));
    }

    private void recordHistory(ApplicationEntity application, ApplicationStatus previous,
                               ApplicationStatus next, ChangeSource source, String comment) {
        ApplicationStatusHistoryEntity history = new ApplicationStatusHistoryEntity();
        history.setApplication(application);
        history.setPreviousStatus(previous);
        history.setNewStatus(next);
        history.setChangeSource(source);
        history.setComment(comment);
        historyRepository.save(history);
    }

    private ApplicationEntity requireApplication(long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Candidature introuvable."));
    }

    private ApplicationListItem toListItem(ApplicationEntity application) {
        OpportunityEntity opportunity = application.getOpportunity();
        ProjectionStatus projection = projectionRepository
                .findFirstByResourceTypeAndResourceIdOrderByUpdatedAtDesc("APPLICATION", application.getId())
                .map(ExternalProjectionEntity::getStatus).orElse(ProjectionStatus.PENDING);
        return new ApplicationListItem(application.getId(), companyName(opportunity), opportunity.getTitle(),
                opportunity.getContractType(), opportunity.getContractTypeRaw(), application.getAppliedAt(),
                statusOf(application), priorityOf(application), application.getFollowUpPlannedAt(),
                application.getResumeVersion() != null, application.getCoverLetter() != null, projection);
    }

    private StatusHistoryItem toHistory(ApplicationStatusHistoryEntity history) {
        return new StatusHistoryItem(history.getPreviousStatus(),
                history.getNewStatus() == null ? ApplicationStatus.NOT_CONTACTED : history.getNewStatus(),
                history.getChangedAt(), history.getChangeSource(), history.getComment());
    }

    private static ApplicationStatus statusOf(ApplicationEntity application) {
        return application.getStatus() == null ? ApplicationStatus.NOT_CONTACTED
                : application.getStatus();
    }

    private static ApplicationPriority priorityOf(ApplicationEntity application) {
        return application.getPriority() == null ? ApplicationPriority.MEDIUM
                : application.getPriority();
    }

    private static InterviewStatus interviewOf(ApplicationEntity application) {
        return application.getInterviewStatus() == null ? InterviewStatus.NONE
                : application.getInterviewStatus();
    }

    private static ApplicationDecision decisionOf(ApplicationEntity application) {
        return application.getDecision() == null ? ApplicationDecision.PENDING
                : application.getDecision();
    }

    private ProjectionView toProjection(ExternalProjectionEntity projection) {
        return new ProjectionView(projection.getStatus(), projection.getLastAttemptAt(),
                projection.getLastSuccessfulSyncAt(), projection.getLastErrorCode(),
                projection.getLastErrorMessage(), projection.getRetryCount());
    }

    private static <T> T optional(Long id, org.springframework.data.jpa.repository.JpaRepository<T, Long> repository,
                                  String label) {
        return id == null ? null : repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(label + " introuvable."));
    }

    private static Long id(Object entity) {
        if (entity == null) return null;
        if (entity instanceof ResumeVersionEntity value) return value.getId();
        if (entity instanceof CoverLetterEntity value) return value.getId();
        if (entity instanceof ResumeAnalysisRecordEntity value) return value.getId();
        return null;
    }

    private static String companyName(OpportunityEntity opportunity) {
        return opportunity.getCompany() != null ? opportunity.getCompany().getName()
                : fallback(opportunity.getCompanyName(), "Entreprise non renseignée");
    }

    private static String fallback(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private static String clean(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
