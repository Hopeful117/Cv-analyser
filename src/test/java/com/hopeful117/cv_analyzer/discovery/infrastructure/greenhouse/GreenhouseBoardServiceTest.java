package com.hopeful117.cv_analyzer.discovery.infrastructure.greenhouse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GreenhouseBoardServiceTest {

    @Mock
    private GreenhouseApiClient apiClient;

    @Test
    void keepsSuccessfulBoardsWhenAnotherBoardFails() {
        GreenhouseProperties.Board successful = new GreenhouseProperties.Board("success", "Success Corp", true);
        GreenhouseProperties.Board failing = new GreenhouseProperties.Board("removed", "Removed Corp", true);
        GreenhouseProperties properties = new GreenhouseProperties("https://example.test", List.of(successful, failing));
        GreenhouseJobResponse response = new GreenhouseJobResponse(List.of(
                new GreenhouseJobResponse.GreenhouseJobDto(1L, "Engineer", null, null, null, null,
                        null, null, "https://example.test/jobs/1", null)), new GreenhouseJobResponse.Meta(1));
        when(apiClient.fetchJobs(successful)).thenReturn(response);
        when(apiClient.fetchJobs(failing)).thenThrow(new IllegalStateException("404"));

        GreenhouseBoardService.BoardFetchResult result = new GreenhouseBoardService(apiClient, properties).fetchAll();

        assertThat(result.configuredBoards()).isEqualTo(2);
        assertThat(result.offers()).extracting(offer -> offer.providerOfferId()).containsExactly("1");
        assertThat(result.errors()).containsExactly("Removed Corp : 404");
    }

    @Test
    void acceptsZeroConfiguredBoards() {
        GreenhouseProperties properties = new GreenhouseProperties(null, List.of());

        GreenhouseBoardService.BoardFetchResult result = new GreenhouseBoardService(apiClient, properties).fetchAll();

        assertThat(result.configuredBoards()).isZero();
        assertThat(result.offers()).isEmpty();
        assertThat(result.errors()).isEmpty();
    }
}
