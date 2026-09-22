package com.hopeful117.cv_analyzer.discovery.web;

import com.hopeful117.cv_analyzer.discovery.application.DiscoverJobOffers;
import com.hopeful117.cv_analyzer.discovery.application.SelectJobOffer;
import com.hopeful117.cv_analyzer.discovery.domain.EligibilityResult;
import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;
import com.hopeful117.cv_analyzer.search.persistence.JobSearchPreferencesRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class JobDiscoveryControllerMvcTest {

    @MockitoBean
    private DiscoverJobOffers discoverJobOffers;

    @MockitoBean
    private SelectJobOffer selectJobOffer;

    @MockitoBean
    private JobSearchPreferencesRepository preferencesRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void successfulSearchDisplaysReturnedOffers() throws Exception {
        JobOffer offer = new JobOffer(
                "france-travail", "12345", "https://example.com/offres/12345",
                null, "Développeur Java", "Développement backend", "Example",
                null, null, null, "Paris", null, null, null, null,
                null, null, "CDI", List.of(), null, null, null,
                null, null, null, null, null, null
        );
        var result = DiscoverJobOffers.DiscoveryResult.success(
                List.of(new DiscoverJobOffers.EligibleOffer(offer, EligibilityResult.eligible())),
                1, 1, "Développeur Java", "france-travail"
        );
        when(discoverJobOffers.discover("Développeur Java")).thenReturn(result);

        mockMvc.perform(post("/job-discovery/search")
                        .param("targetRole", "Développeur Java"))
                .andExpect(status().isOk())
                .andExpect(view().name("job-discovery-results"))
                .andExpect(model().attribute("selectedRole", "Développeur Java"))
                .andExpect(model().attribute("results", JobDiscoveryViewModels.toResults(result)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Développeur Java")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Example")));

        verify(discoverJobOffers).discover("Développeur Java");
    }

    @Test
    void selectingAnOfferDelegatesToSelectionUseCaseWithoutApplicationData() throws Exception {
        mockMvc.perform(post("/job-discovery/select")
                        .param("providerKey", "france-travail")
                        .param("providerOfferId", "12345")
                        .param("title", "Développeur Java")
                        .param("company", "Example")
                        .param("originUrl", "https://example.com/offres/12345"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/"));

        verify(selectJobOffer).select(argThat(offer ->
                offer.providerKey().equals("france-travail")
                        && offer.providerOfferId().equals("12345")
                        && offer.title().equals("Développeur Java")));
    }
}
