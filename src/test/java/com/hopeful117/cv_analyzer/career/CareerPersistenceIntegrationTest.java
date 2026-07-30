package com.hopeful117.cv_analyzer.career;

import com.hopeful117.cv_analyzer.career.domain.*;
import com.hopeful117.cv_analyzer.career.persistence.*;
import com.hopeful117.cv_analyzer.model.ResumePdfStyle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CareerPersistenceIntegrationTest {
    @Autowired OpportunityRepository opportunityRepository;
    @Autowired ResumeAnalysisRecordRepository analysisRepository;
    @Autowired ResumeDocumentRepository documentRepository;
    @PersistenceContext EntityManager entityManager;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void flywayEnsuresAllInterviewTablesExist() {
        assertThat(tableExists("interview_session")).isTrue();
        assertThat(tableExists("interview_question")).isTrue();
        assertThat(tableExists("interview_question_result")).isTrue();
    }

    @Test
    void persistsOpportunityAnalysisCollectionsAndResumeVersions() {
        OpportunityEntity opportunity = new OpportunityEntity();
        opportunity.setTitle("Développeur Java");
        opportunity.setSourceType(OpportunitySourceType.MANUAL);
        opportunity.setRawDescription("Description source");
        opportunity.setNormalizedDescription("Description source");
        opportunity.setDetectedLanguage("fr");
        opportunity.setStatus(OpportunityStatus.ANALYZED);
        opportunityRepository.save(opportunity);

        ResumeAnalysisRecordEntity analysis = new ResumeAnalysisRecordEntity();
        analysis.setOpportunity(opportunity);
        analysis.setOverallScore(78);
        analysis.setQualityScore(80);
        analysis.setAtsScore(75);
        analysis.setMatchScore(79);
        analysis.setAnalysisNature(AnalysisNature.AI_ESTIMATE);
        analysis.setRisks(List.of("Structure à vérifier"));
        analysis.setRecommendations(List.of("Clarifier le titre"));
        analysis.setMissingKeywords(List.of("Flyway"));
        analysisRepository.save(analysis);

        ResumeDocumentEntity document = new ResumeDocumentEntity();
        document.setAnalysis(analysis);
        document.setStatus(DocumentStatus.ACTIVE);
        document.addVersion(version(1, ResumeVersionOrigin.AI_GENERATED, "Version IA"));
        document.addVersion(version(2, ResumeVersionOrigin.USER_EDITED, "Version utilisateur"));
        documentRepository.saveAndFlush(document);
        entityManager.clear();

        ResumeDocumentEntity reloaded = documentRepository.findOneById(document.getId()).orElseThrow();
        assertThat(reloaded.getVersions()).extracting(ResumeVersionEntity::getVersionNumber)
                .containsExactly(2, 1);
        assertThat(analysisRepository.findOneById(analysis.getId()).orElseThrow().getMissingKeywords())
                .containsExactly("Flyway");
        assertThat(analysisRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10)))
                .hasSize(1);
    }

    private ResumeVersionEntity version(int number, ResumeVersionOrigin origin, String content) {
        ResumeVersionEntity version = new ResumeVersionEntity();
        version.setVersionNumber(number);
        version.setOrigin(origin);
        version.setContent(content);
        version.setPdfStyle(ResumePdfStyle.PROFESSIONAL);
        return version;
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.tables
                WHERE LOWER(table_schema) = LOWER(SCHEMA())
                  AND LOWER(table_name) = LOWER(?)
                """, Integer.class, tableName);
        return count != null && count == 1;
    }
}
