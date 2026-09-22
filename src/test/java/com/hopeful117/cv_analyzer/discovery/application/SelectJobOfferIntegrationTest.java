package com.hopeful117.cv_analyzer.discovery.application;

import com.hopeful117.cv_analyzer.career.application.OpportunityCreationRequest;
import com.hopeful117.cv_analyzer.career.application.OpportunityService;
import com.hopeful117.cv_analyzer.career.domain.OpportunitySourceType;
import com.hopeful117.cv_analyzer.career.domain.OpportunityStatus;
import com.hopeful117.cv_analyzer.career.persistence.ApplicationRepository;
import com.hopeful117.cv_analyzer.career.persistence.OpportunityRepository;
import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SelectJobOfferIntegrationTest {

    @Autowired
    private SelectJobOffer selectJobOffer;

    @Autowired
    private OpportunityService opportunityService;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Test
    void selectionPersistsOnlyAnOpportunity() {
        long applicationsBefore = applicationRepository.count();
        JobOffer offer = new JobOffer(
                "france-travail", "12345", "https://example.com/12345", null,
                "Développeur Java", "provider description", "Example", null, null, null,
                "Paris", null, null, null, null, null, "CDI", "CDI", List.of(), null,
                null, null, "45k", null, null, null, null, null
        );

        Long opportunityId = selectJobOffer.select(offer);

        assertThat(opportunityRepository.findById(opportunityId)).get()
                .extracting(opportunity -> opportunity.getTitle(), opportunity -> opportunity.getCompanyName(),
                        opportunity -> opportunity.getRawDescription())
                .containsExactly("Développeur Java", "Example", "");
        assertThat(applicationRepository.count()).isEqualTo(applicationsBefore);
    }

    @Test
    void directCreationDoesNotRequireAJobOfferOrCreateAnApplication() {
        long applicationsBefore = applicationRepository.count();

        Long opportunityId = opportunityService.create(new OpportunityCreationRequest(
                null, "Développeur Kotlin", "Example", null, null, null, null, null,
                "manual", null, null, "Lyon", OpportunitySourceType.MANUAL, null,
                "Description", "Description", "fr", OpportunityStatus.DRAFT)).getId();

        assertThat(opportunityRepository.existsById(opportunityId)).isTrue();
        assertThat(applicationRepository.count()).isEqualTo(applicationsBefore);
    }
}
