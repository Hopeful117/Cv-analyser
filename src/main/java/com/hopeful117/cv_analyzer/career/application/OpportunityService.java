package com.hopeful117.cv_analyzer.career.application;

import com.hopeful117.cv_analyzer.career.domain.OpportunitySourceType;
import com.hopeful117.cv_analyzer.career.domain.OpportunityStatus;
import com.hopeful117.cv_analyzer.career.persistence.OpportunityEntity;
import com.hopeful117.cv_analyzer.career.persistence.OpportunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OpportunityService {

    private final OpportunityRepository repository;

    @Transactional
    public OpportunityEntity create(OpportunityCreationRequest request) {
        OpportunityEntity opportunity = new OpportunityEntity();
        opportunity.setCompany(request.company());
        opportunity.setTitle(cleanLimited(request.title(), 200, "L’intitulé"));
        opportunity.setCompanyName(cleanLimited(request.companyName(), 200, "L’entreprise"));
        opportunity.setContractType(request.contractType());
        opportunity.setContractTypeRaw(cleanLimited(request.contractTypeRaw(), 120, "Le contrat"));
        opportunity.setWorkSchedule(request.workSchedule());
        opportunity.setWorkScheduleRaw(cleanLimited(request.workScheduleRaw(), 120, "Le rythme de travail"));
        opportunity.setRemoteMode(request.remoteMode());
        opportunity.setSource(cleanLimited(request.source(), 200, "La source"));
        opportunity.setSalaryText(cleanLimited(request.salaryText(), 200, "Le salaire"));
        opportunity.setDistanceText(cleanLimited(request.distanceText(), 120, "La distance"));
        opportunity.setLocation(cleanLimited(request.location(), 300, "Le lieu"));
        opportunity.setSourceType(request.sourceType() == null
                ? OpportunitySourceType.MANUAL : request.sourceType());
        opportunity.setSourceUrl(cleanLimited(request.sourceUrl(), 2048, "L’URL"));
        opportunity.setRawDescription(content(request.rawDescription()));
        opportunity.setNormalizedDescription(content(request.normalizedDescription()));
        opportunity.setDetectedLanguage(cleanLimited(request.detectedLanguage(), 16, "La langue"));
        opportunity.setStatus(request.status() == null ? OpportunityStatus.DRAFT : request.status());
        return repository.save(opportunity);
    }

    private static String content(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() > 100_000) {
            throw new IllegalArgumentException("La description dépasse la taille autorisée.");
        }
        return trimmed;
    }

    private static String cleanLimited(String value, int maxLength, String label) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String cleaned = value.trim();
        if (cleaned.length() > maxLength) {
            throw new IllegalArgumentException(label + " dépasse " + maxLength + " caractères.");
        }
        return cleaned;
    }
}
