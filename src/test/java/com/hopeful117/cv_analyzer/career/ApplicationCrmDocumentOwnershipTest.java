package com.hopeful117.cv_analyzer.career;

import com.hopeful117.cv_analyzer.career.application.ApplicationCrmService;
import com.hopeful117.cv_analyzer.career.persistence.*;
import com.hopeful117.cv_analyzer.career.web.ApplicationForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationCrmDocumentOwnershipTest {

    @Mock CompanyRepository companyRepository;
    @Mock OpportunityRepository opportunityRepository;
    @Mock com.hopeful117.cv_analyzer.career.application.OpportunityService opportunityService;
    @Mock ApplicationRepository applicationRepository;
    @Mock ApplicationStatusHistoryRepository historyRepository;
    @Mock ExternalProjectionRepository projectionRepository;
    @Mock ResumeVersionRepository resumeVersionRepository;
    @Mock CoverLetterRepository coverLetterRepository;
    @Mock ResumeAnalysisRecordRepository analysisRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ApplicationCrmService service;

    @Test
    void absentDocumentsAreAllowed() {
        ApplicationEntity application = application(10L, 20L);
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(application));

        assertThatCode(() -> service.update(10L, form())).doesNotThrowAnyException();
    }

    @Test
    void rejectsResumeVersionFromAnotherOpportunity() {
        ApplicationEntity application = application(10L, 20L);
        ResumeAnalysisRecordEntity foreignAnalysis = analysis(30L);
        ResumeDocumentEntity document = new ResumeDocumentEntity();
        document.setAnalysis(foreignAnalysis);
        ResumeVersionEntity version = new ResumeVersionEntity();
        version.setDocument(document);
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(application));
        when(resumeVersionRepository.findById(1L)).thenReturn(Optional.of(version));
        ApplicationForm form = form();
        form.setResumeVersionId(1L);

        assertThatThrownBy(() -> service.update(10L, form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("version de CV");
    }

    @Test
    void rejectsCoverLetterFromAnotherOpportunity() {
        ApplicationEntity application = application(10L, 20L);
        CoverLetterEntity letter = new CoverLetterEntity();
        letter.setOpportunity(opportunity(30L));
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(application));
        when(coverLetterRepository.findById(1L)).thenReturn(Optional.of(letter));
        ApplicationForm form = form();
        form.setCoverLetterId(1L);

        assertThatThrownBy(() -> service.update(10L, form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lettre");
    }

    @Test
    void rejectsAnalysisFromAnotherOpportunity() {
        ApplicationEntity application = application(10L, 20L);
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(application));
        when(analysisRepository.findById(1L)).thenReturn(Optional.of(analysis(30L)));
        ApplicationForm form = form();
        form.setAnalysisId(1L);

        assertThatThrownBy(() -> service.update(10L, form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("analyse");
    }

    private ApplicationEntity application(long id, long opportunityId) {
        ApplicationEntity application = new ApplicationEntity();
        application.setId(id);
        application.setOpportunity(opportunity(opportunityId));
        return application;
    }

    private ResumeAnalysisRecordEntity analysis(long opportunityId) {
        ResumeAnalysisRecordEntity analysis = new ResumeAnalysisRecordEntity();
        analysis.setOpportunity(opportunity(opportunityId));
        return analysis;
    }

    private OpportunityEntity opportunity(long id) {
        OpportunityEntity opportunity = new OpportunityEntity();
        opportunity.setId(id);
        return opportunity;
    }

    private ApplicationForm form() {
        ApplicationForm form = new ApplicationForm();
        form.setCompanyName("Example");
        form.setJobTitle("Développeur");
        return form;
    }
}
