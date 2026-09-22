package com.hopeful117.cv_analyzer.discovery.web;

import com.hopeful117.cv_analyzer.career.domain.ContractType;
import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;
import com.hopeful117.cv_analyzer.discovery.domain.JobOfferCompetency;
import com.hopeful117.cv_analyzer.search.domain.WorkMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ManualJobOfferForm {
    private String providerKey = "manual";
    private String providerOfferId = "manual-" + UUID.randomUUID();

    @NotBlank(message = "Le titre est obligatoire.")
    @Size(max = 200)
    private String title;

    @Size(max = 200)
    private String company;

    @Size(max = 2048)
    @Pattern(regexp = "^\\s*$|https?://.+", message = "L'URL doit commencer par http:// ou https://.")
    private String originUrl;

    @Size(max = 300)
    private String locationLabel;

    @Size(max = 120)
    private String rawContractCode;

    @Size(max = 120)
    private String rawContractLabel;

    @Size(max = 200)
    private String rawSalaryText;

    private WorkMode workMode;

    @Size(max = 120)
    private String workDurationLabel;

    @NotBlank(message = "La description est obligatoire.")
    @Size(max = 100000)
    private String description;

    @Size(max = 2000)
    private String competencies;

    public JobOffer toJobOffer() {
        List<JobOfferCompetency> canonicalCompetencies = Arrays.stream(valueOrEmpty(competencies).split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .map(value -> new JobOfferCompetency(value, value, "S"))
                .toList();
        String contractLabel = blankToNull(rawContractLabel);
        if (contractLabel == null) {
            contractLabel = blankToNull(rawContractCode);
        }
        return new JobOffer(
                "manual", valueOrEmpty(providerOfferId).trim(), blankToNull(originUrl), null,
                title.trim(), description.trim(), blankToNull(company), null, null, null,
                blankToNull(locationLabel), null, null, null, null,
                ContractType.fromCode(rawContractCode), blankToNull(rawContractCode), contractLabel,
                canonicalCompetencies, null, workMode, blankToNull(workDurationLabel), blankToNull(rawSalaryText),
                null, null, null, null, null
        );
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
