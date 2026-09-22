package com.hopeful117.cv_analyzer.discovery.infrastructure.greenhouse;

import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GreenhouseBoardService {

    private final GreenhouseApiClient apiClient;
    private final GreenhouseProperties properties;

    public GreenhouseBoardService(GreenhouseApiClient apiClient, GreenhouseProperties properties) {
        this.apiClient = apiClient;
        this.properties = properties;
    }

    public List<GreenhouseProperties.Board> configuredBoards() {
        return properties.enabledBoards();
    }

    public BoardFetchResult fetchAll() {
        List<JobOffer> offers = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int configuredBoards = 0;

        for (GreenhouseProperties.Board board : properties.enabledBoards()) {
            configuredBoards++;
            try {
                GreenhouseJobResponse response = apiClient.fetchJobs(board);
                if (response.jobs() != null) {
                    response.jobs().stream()
                            .map(job -> GreenhouseOfferMapper.toDomain(job, board))
                            .forEach(offers::add);
                }
            } catch (Exception exception) {
                String label = board.companyName() == null || board.companyName().isBlank()
                        ? board.boardToken() : board.companyName();
                log.warn("Greenhouse board '{}' could not be fetched: {}", label, exception.getMessage());
                errors.add(label + " : " + (exception.getMessage() == null ? "erreur inconnue" : exception.getMessage()));
            }
        }

        return new BoardFetchResult(List.copyOf(offers), List.copyOf(errors), configuredBoards);
    }

    public record BoardFetchResult(List<JobOffer> offers, List<String> errors, int configuredBoards) {
    }
}
