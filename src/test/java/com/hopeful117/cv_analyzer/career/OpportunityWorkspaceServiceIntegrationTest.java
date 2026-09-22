package com.hopeful117.cv_analyzer.career;

import com.hopeful117.cv_analyzer.career.application.ApplicationCrmService;
import com.hopeful117.cv_analyzer.career.application.OpportunityWorkspaceService;
import com.hopeful117.cv_analyzer.career.application.OpportunityWorkspaceViewModels.Workspace;
import com.hopeful117.cv_analyzer.career.domain.*;
import com.hopeful117.cv_analyzer.career.persistence.*;
import com.hopeful117.cv_analyzer.career.web.ApplicationForm;
import com.hopeful117.cv_analyzer.model.ResumePdfStyle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OpportunityWorkspaceServiceIntegrationTest {
    @Autowired OpportunityWorkspaceService workspaceService;
    @Autowired OpportunityRepository opportunityRepository;
    @Autowired ResumeAnalysisRecordRepository analysisRepository;
    @Autowired ResumeDocumentRepository documentRepository;
    @Autowired CoverLetterRepository coverLetterRepository;
    @Autowired ApplicationCrmService applicationCrmService;

    @Test
    void aggregatesOnlyArtifactsBelongingToTheRequestedOpportunity() {
        OpportunityEntity opportunity = opportunity("Workspace Java");
        OpportunityEntity other = opportunity("Other opportunity");

        ResumeAnalysisRecordEntity analysis = new ResumeAnalysisRecordEntity();
        analysis.setOpportunity(opportunity);
        analysis.setOverallScore(82);
        analysis.setQualityScore(80);
        analysis.setAtsScore(84);
        analysis.setMatchScore(79);
        analysis.setAnalysisNature(AnalysisNature.AI_ESTIMATE);
        analysisRepository.save(analysis);

        ResumeDocumentEntity document = new ResumeDocumentEntity();
        document.setAnalysis(analysis);
        document.setStatus(DocumentStatus.ACTIVE);
        ResumeVersionEntity version = new ResumeVersionEntity();
        version.setVersionNumber(2);
        version.setOrigin(ResumeVersionOrigin.USER_EDITED);
        version.setContent("CV");
        version.setProfessionalTitle("Backend Java");
        version.setPdfStyle(ResumePdfStyle.PROFESSIONAL);
        document.addVersion(version);
        documentRepository.save(document);

        CoverLetterEntity letter = new CoverLetterEntity();
        letter.setOpportunity(opportunity);
        letter.setContent("Lettre");
        letter.setStatus(DocumentStatus.ACTIVE);
        letter.setOrigin(CoverLetterOrigin.USER_EDITED);
        coverLetterRepository.save(letter);

        ApplicationForm form = new ApplicationForm();
        form.setOpportunityId(opportunity.getId());
        form.setCompanyName("ACME");
        form.setJobTitle("Workspace Java");
        form.setStatus(ApplicationStatus.APPLIED);
        form.setPriority(ApplicationPriority.MEDIUM);
        form.setRemoteMode(RemoteMode.UNSPECIFIED);
        form.setInterviewStatus(InterviewStatus.NONE);
        form.setDecision(ApplicationDecision.PENDING);
        applicationCrmService.create(form, ChangeSource.USER);

        Workspace workspace = workspaceService.get(opportunity.getId());
        Workspace otherWorkspace = workspaceService.get(other.getId());

        assertThat(workspace.analyses()).hasSize(1);
        assertThat(workspace.resumes()).hasSize(1)
                .first().extracting(item -> item.latestVersion(), item -> item.professionalTitle())
                .containsExactly(2, "Backend Java");
        assertThat(workspace.letters()).hasSize(1);
        assertThat(workspace.applications()).hasSize(1);
        assertThat(otherWorkspace.analyses()).isEmpty();
        assertThat(otherWorkspace.resumes()).isEmpty();
        assertThat(otherWorkspace.letters()).isEmpty();
        assertThat(otherWorkspace.applications()).isEmpty();
    }

    @Test
    void workspaceKeepsEmptyArtifactCollectionsEmptyAndDoesNotCreateApplication() {
        OpportunityEntity opportunity = opportunity("Empty workspace");

        Workspace workspace = workspaceService.get(opportunity.getId());

        assertThat(workspace.analyses()).isEmpty();
        assertThat(workspace.resumes()).isEmpty();
        assertThat(workspace.letters()).isEmpty();
        assertThat(workspace.applications()).isEmpty();
    }

    private OpportunityEntity opportunity(String title) {
        OpportunityEntity opportunity = new OpportunityEntity();
        opportunity.setTitle(title);
        opportunity.setCompanyName("ACME");
        opportunity.setSourceType(OpportunitySourceType.MANUAL);
        opportunity.setRawDescription("Description");
        opportunity.setNormalizedDescription("Description");
        opportunity.setDetectedLanguage("fr");
        opportunity.setStatus(OpportunityStatus.DRAFT);
        return opportunityRepository.saveAndFlush(opportunity);
    }
}
