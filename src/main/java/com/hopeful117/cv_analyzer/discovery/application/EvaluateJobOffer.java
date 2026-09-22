package com.hopeful117.cv_analyzer.discovery.application;

import com.hopeful117.cv_analyzer.discovery.domain.EligibilityEvaluator;
import com.hopeful117.cv_analyzer.discovery.domain.EligibilityResult;
import com.hopeful117.cv_analyzer.discovery.domain.JobMatchResult;
import com.hopeful117.cv_analyzer.discovery.domain.JobMatchingEngine;
import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;
import com.hopeful117.cv_analyzer.profile.persistence.ProfessionalProfileRepository;
import com.hopeful117.cv_analyzer.search.persistence.JobSearchPreferencesEntity;
import com.hopeful117.cv_analyzer.search.persistence.JobSearchPreferencesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EvaluateJobOffer {

    private final ProfessionalProfileRepository profileRepository;
    private final JobSearchPreferencesRepository preferencesRepository;

    @Transactional(readOnly = true)
    public String readinessError() {
        if (profileRepository.findLocalProfile().isEmpty()) {
            return "Votre profil professionnel n'est pas encore créé.";
        }
        if (preferencesRepository.findActivePreferences().isEmpty()) {
            return "Vos préférences de recherche ne sont pas encore configurées.";
        }
        return null;
    }

    @Transactional(readOnly = true)
    public EvaluationResult evaluate(JobOffer offer) {
        var profile = profileRepository.findLocalProfile();
        var preferences = preferencesRepository.findActivePreferences();
        if (profile.isEmpty() || preferences.isEmpty()) {
            return EvaluationResult.failure(readinessError());
        }
        return EvaluationResult.success(
                EligibilityEvaluator.evaluate(offer, preferences.get()),
                JobMatchingEngine.evaluate(profile.get(), preferences.get(), offer));
    }

    public record EvaluationResult(
            boolean success,
            EligibilityResult eligibility,
            JobMatchResult matching,
            String errorMessage
    ) {
        public static EvaluationResult success(EligibilityResult eligibility, JobMatchResult matching) {
            return new EvaluationResult(true, eligibility, matching, null);
        }

        public static EvaluationResult failure(String message) {
            return new EvaluationResult(false, null, null, message);
        }
    }
}
