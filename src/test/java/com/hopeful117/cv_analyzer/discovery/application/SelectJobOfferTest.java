package com.hopeful117.cv_analyzer.discovery.application;

import com.hopeful117.cv_analyzer.career.application.OpportunityCreationRequest;
import com.hopeful117.cv_analyzer.career.application.OpportunityService;
import com.hopeful117.cv_analyzer.career.persistence.OpportunityEntity;
import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SelectJobOfferTest {

    @Mock
    private OpportunityService opportunityService;

    @InjectMocks
    private SelectJobOffer selectJobOffer;

    @Test
    void mapsTransientOfferToOpportunityCreationRequest() {
        JobOffer offer = new JobOffer(
                "france-travail", "12345", "https://example.com/12345", null,
                "Développeur Java", "ignored provider description", "Example", null, null, null,
                "Paris", null, null, null, null, null, "CDI", "CDI", List.of(), null,
                null, null, "45k", null, null, null, null, null
        );
        OpportunityEntity persisted = new OpportunityEntity();
        persisted.setId(1L);
        org.mockito.Mockito.when(opportunityService.create(any())).thenReturn(persisted);

        selectJobOffer.select(offer);

        ArgumentCaptor<OpportunityCreationRequest> captor = ArgumentCaptor.forClass(OpportunityCreationRequest.class);
        verify(opportunityService).create(captor.capture());
        OpportunityCreationRequest request = captor.getValue();
        assertThat(request.title()).isEqualTo("Développeur Java");
        assertThat(request.companyName()).isEqualTo("Example");
        assertThat(request.sourceUrl()).isEqualTo("https://example.com/12345");
        assertThat(request.rawDescription()).isEmpty();
        assertThat(request.normalizedDescription()).isEmpty();
    }
}
