package com.hopeful117.cv_analyzer.career.application;

import com.hopeful117.cv_analyzer.career.persistence.*;
import com.hopeful117.cv_analyzer.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.hopeful117.cv_analyzer.career.application.OpportunityWorkspaceViewModels.*;

@Service
@RequiredArgsConstructor
public class OpportunityWorkspaceService {
    private final OpportunityRepository opportunityRepository;
    private final ResumeAnalysisRecordRepository analysisRepository;
    private final ResumeDocumentRepository resumeDocumentRepository;
    private final CoverLetterRepository coverLetterRepository;
    private final ApplicationCrmService applicationCrmService;

    @Transactional(readOnly = true)
    public List<OpportunityListItem> list() {
        return opportunityRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(opportunity -> new OpportunityListItem(
                        opportunity.getId(), opportunity.getTitle(), opportunity.getCompanyName(),
                        opportunity.getStatus(), opportunity.getCreatedAt(),
                        (int) analysisRepository.countByOpportunityId(opportunity.getId()),
                        (int) resumeDocumentRepository.countByAnalysisOpportunityId(opportunity.getId()),
                        (int) coverLetterRepository.countByOpportunityId(opportunity.getId()),
                        applicationCrmService.findForOpportunity(opportunity.getId()).size()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Workspace get(long id) {
        OpportunityEntity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Opportunité introuvable."));
        OpportunityDetails details = new OpportunityDetails(
                opportunity.getId(), opportunity.getTitle(), opportunity.getCompanyName(),
                opportunity.getLocation(), opportunity.getContractType(), opportunity.getContractTypeRaw(),
                opportunity.getRemoteMode(), opportunity.getSource(), opportunity.getSourceUrl(),
                opportunity.getNormalizedDescription(), opportunity.getStatus(), opportunity.getCreatedAt());

        List<AnalysisItem> analyses = analysisRepository
                .findAllByOpportunityIdOrderByCreatedAtDesc(id).stream()
                .map(analysis -> new AnalysisItem(analysis.getId(), analysis.getCreatedAt(),
                        analysis.getOverallScore(), analysis.getMatchScore()))
                .toList();
        List<ResumeItem> resumes = resumeDocumentRepository
                .findAllByAnalysisOpportunityIdOrderByUpdatedAtDesc(id).stream()
                .map(document -> new ResumeItem(document.getId(), document.getAnalysis().getId(),
                        document.getVersions().isEmpty() ? null
                                : document.getVersions().getFirst().getProfessionalTitle(),
                        document.getVersions().isEmpty() ? 0
                                : document.getVersions().getFirst().getVersionNumber(),
                        document.getUpdatedAt()))
                .toList();
        List<LetterItem> letters = coverLetterRepository.findAllByOpportunityIdOrderByUpdatedAtDesc(id).stream()
                .map(letter -> new LetterItem(letter.getId(), letter.getOrigin(), letter.getUpdatedAt()))
                .toList();
        return new Workspace(details, analyses, resumes, letters,
                applicationCrmService.findForOpportunity(id));
    }

    @Transactional(readOnly = true)
    public List<OpportunityListItem> recent(int limit) {
        return list().stream().limit(Math.max(0, limit)).toList();
    }
}
