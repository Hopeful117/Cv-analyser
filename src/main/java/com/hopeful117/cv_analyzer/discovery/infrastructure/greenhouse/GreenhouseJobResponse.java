package com.hopeful117.cv_analyzer.discovery.infrastructure.greenhouse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GreenhouseJobResponse(
        List<GreenhouseJobDto> jobs,
        Meta meta
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(Integer total) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GreenhouseJobDto(
            Long id,
            String title,
            String content,
            Location location,
            List<Office> offices,
            List<Department> departments,
            @JsonProperty("updated_at") String updatedAt,
            @JsonProperty("first_published") String firstPublished,
            @JsonProperty("absolute_url") String absoluteUrl,
            @JsonProperty("requisition_id") String requisitionId
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Location(String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Office(String name, String location) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Department(String name) {
    }
}
