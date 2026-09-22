package com.hopeful117.cv_analyzer.discovery.infrastructure.greenhouse;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "greenhouse")
public record GreenhouseProperties(
        String baseUrl,
        List<Board> boards
) {
    public GreenhouseProperties {
        baseUrl = baseUrl == null || baseUrl.isBlank()
                ? "https://boards-api.greenhouse.io/v1"
                : baseUrl;
        boards = boards == null ? List.of() : List.copyOf(boards);
    }

    public List<Board> enabledBoards() {
        return boards.stream()
                .filter(Board::enabled)
                .filter(board -> board.boardToken() != null && !board.boardToken().isBlank())
                .toList();
    }

    public record Board(String boardToken, String companyName, boolean enabled) {
    }
}
