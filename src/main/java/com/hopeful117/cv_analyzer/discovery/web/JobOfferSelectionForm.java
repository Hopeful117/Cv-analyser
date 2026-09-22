package com.hopeful117.cv_analyzer.discovery.web;

import com.hopeful117.cv_analyzer.career.domain.ContractType;
import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JobOfferSelectionForm {
    @NotBlank
    @Size(max = 80)
    private String providerKey;
    @NotBlank
    @Size(max = 120)
    private String providerOfferId;
    @NotBlank
    @Size(max = 200)
    private String title;
    @Size(max = 200)
    private String company;
    @Size(max = 2048)
    private String originUrl;
    @Size(max = 300)
    private String locationLabel;
    @Size(max = 120)
    private String rawContractCode;
    @Size(max = 120)
    private String rawContractLabel;
    @Size(max = 200)
    private String rawSalaryText;
    @Size(max = 100_000)
    private String description;

    public JobOffer toJobOffer() {
        return new JobOffer(
                providerKey.trim(), providerOfferId.trim(), originUrl, null, title.trim(), description, company,
                null, null, null, locationLabel, null, null, null, null,
                ContractType.fromCode(rawContractCode), rawContractCode, rawContractLabel, List.of(), null,
                null, null, rawSalaryText, null, null, null, null, null
        );
    }
}
