package com.hopeful117.cv_analyzer.discovery.domain;

import com.hopeful117.cv_analyzer.profile.domain.ProfileNormalizer;
import com.hopeful117.cv_analyzer.profile.persistence.ProfileExperienceEntity;
import com.hopeful117.cv_analyzer.profile.persistence.ProfileSkillEntity;
import com.hopeful117.cv_analyzer.profile.persistence.ProfessionalProfileEntity;
import com.hopeful117.cv_analyzer.search.domain.TechnologyPreference;
import com.hopeful117.cv_analyzer.search.persistence.JobSearchPreferencesEntity;
import com.hopeful117.cv_analyzer.search.persistence.PreferenceRoleEntity;
import com.hopeful117.cv_analyzer.search.persistence.PreferenceTechnologyEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class JobMatchingEngine {
    // Role is the strongest relevance signal; skills prove capability; preferences add a smaller bonus.
    // Unknown dimensions are omitted from the weighted average instead of reducing the score.
    private static final int ROLE_WEIGHT = 50;
    private static final int SKILL_WEIGHT = 40;
    private static final int PREFERRED_TECHNOLOGY_WEIGHT = 10;

    private JobMatchingEngine() {
    }

    public static JobMatchResult evaluate(ProfessionalProfileEntity profile,
                                          JobSearchPreferencesEntity preferences,
                                          JobOffer offer) {
        if (offer == null) {
            return JobMatchResult.unknown("Offre indisponible pour le matching.");
        }

        List<MatchSignal> positives = new ArrayList<>();
        List<MatchSignal> gaps = new ArrayList<>();
        List<MatchSignal> unknowns = new ArrayList<>();
        List<WeightedDimension> dimensions = new ArrayList<>();

        evaluateRole(profile, preferences, offer, positives, gaps, unknowns)
                .ifPresent(dimension -> dimensions.add(new WeightedDimension(dimension, ROLE_WEIGHT)));
        evaluateSkills(profile, offer, positives, gaps, unknowns)
                .ifPresent(dimension -> dimensions.add(new WeightedDimension(dimension, SKILL_WEIGHT)));
        evaluatePreferredTechnologies(preferences, offer, positives, unknowns)
                .ifPresent(dimension -> dimensions.add(new WeightedDimension(dimension, PREFERRED_TECHNOLOGY_WEIGHT)));

        int score = dimensions.isEmpty() ? 50 : weightedScore(dimensions);
        return new JobMatchResult(score, positives, gaps, unknowns);
    }

    private static java.util.Optional<Integer> evaluateRole(ProfessionalProfileEntity profile,
                                                              JobSearchPreferencesEntity preferences,
                                                              JobOffer offer,
                                                              List<MatchSignal> positives,
                                                              List<MatchSignal> gaps,
                                                              List<MatchSignal> unknowns) {
        Set<String> offerTokens = tokens(offer.title());
        List<String> candidateTitles = new ArrayList<>();
        if (profile != null) {
            candidateTitles.add(profile.getProfessionalTitle());
            safe(profile.getExperiences()).stream().map(ProfileExperienceEntity::getTitle).forEach(candidateTitles::add);
        }
        if (preferences != null) {
            safe(preferences.getTargetRoles()).stream().map(PreferenceRoleEntity::getLabel).forEach(candidateTitles::add);
        }
        List<Set<String>> candidateTokens = candidateTitles.stream()
                .map(JobMatchingEngine::tokens)
                .filter(set -> !set.isEmpty())
                .toList();
        if (offerTokens.isEmpty() || candidateTokens.isEmpty()) {
            unknowns.add(new MatchSignal(MatchSignalType.UNKNOWN,
                    "Le rôle ne peut pas être comparé avec les données disponibles."));
            return java.util.Optional.empty();
        }

        RoleMatch bestMatch = candidateTokens.stream()
                .map(candidate -> roleMatch(offerTokens, candidate))
                .max(java.util.Comparator.comparingInt(RoleMatch::score)
                        .thenComparingInt(match -> match.overlap().size()))
                .orElseThrow();
        int score = bestMatch.score();
        if (score > 0) {
            positives.add(new MatchSignal(MatchSignalType.ROLE,
                    "Le rôle correspond à " + String.join(", ", bestMatch.overlap())
                            + " (" + score + "% des termes de l’offre)."));
        } else {
            gaps.add(new MatchSignal(MatchSignalType.ROLE,
                    "Aucun terme du rôle de l’offre ne correspond aux titres connus du profil ou recherchés."));
        }
        return java.util.Optional.of(score);
    }

    private static java.util.Optional<Integer> evaluateSkills(ProfessionalProfileEntity profile,
                                                               JobOffer offer,
                                                               List<MatchSignal> positives,
                                                               List<MatchSignal> gaps,
                                                               List<MatchSignal> unknowns) {
        List<JobOfferCompetency> competencies = uniqueCompetencies(safe(offer.competencies()));
        Set<String> profileSkills = profile == null ? Set.of() : safe(profile.getSkills()).stream()
                .map(ProfileSkillEntity::getNormalizedName)
                .map(ProfileNormalizer::normalize)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<JobOfferCompetency> comparable = competencies.stream()
                .filter(competency -> !ProfileNormalizer.normalize(competency.label()).isEmpty())
                .toList();
        if (comparable.isEmpty() || profileSkills.isEmpty()) {
            unknowns.add(new MatchSignal(MatchSignalType.UNKNOWN,
                    "Les compétences ne peuvent pas être comparées de manière complète."));
            return java.util.Optional.empty();
        }

        int totalWeight = 0;
        int matchedWeight = 0;
        for (JobOfferCompetency competency : comparable) {
            int weight = competency.isRequired() ? 2 : 1;
            totalWeight += weight;
            String label = competency.label().trim();
            if (profileSkills.contains(ProfileNormalizer.normalize(label))) {
                matchedWeight += weight;
                positives.add(new MatchSignal(MatchSignalType.SKILL, label + " est présent dans le profil."));
            } else {
                gaps.add(new MatchSignal(MatchSignalType.SKILL,
                        label + " est demandé mais n’est pas présent dans les compétences connues."));
            }
        }
        return java.util.Optional.of(percentage((double) matchedWeight / totalWeight));
    }

    private static java.util.Optional<Integer> evaluatePreferredTechnologies(
            JobSearchPreferencesEntity preferences,
            JobOffer offer,
            List<MatchSignal> positives,
            List<MatchSignal> unknowns) {
        if (preferences == null) {
            return java.util.Optional.empty();
        }
        Set<String> preferred = safe(preferences.getTechnologies()).stream()
                .filter(technology -> technology.getKind() == TechnologyPreference.PREFERRED)
                .map(PreferenceTechnologyEntity::getNormalizedName)
                .map(ProfileNormalizer::normalize)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> offered = safe(offer.competencies()).stream()
                .map(JobOfferCompetency::label)
                .map(ProfileNormalizer::normalize)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (preferred.isEmpty()) {
            return java.util.Optional.empty();
        }
        if (offered.isEmpty()) {
            unknowns.add(new MatchSignal(MatchSignalType.UNKNOWN,
                    "Les technologies préférées ne peuvent pas être comparées à cette offre."));
            return java.util.Optional.empty();
        }
        List<String> matches = preferred.stream().filter(offered::contains).toList();
        if (matches.isEmpty()) {
            unknowns.add(new MatchSignal(MatchSignalType.UNKNOWN,
                    "Aucune technologie préférée n’est identifiable dans l’offre."));
            return java.util.Optional.empty();
        }
        matches.forEach(match -> positives.add(new MatchSignal(MatchSignalType.PREFERRED_TECHNOLOGY,
                match + " fait partie des technologies préférées.")));
        return java.util.Optional.of(100);
    }

    private static int weightedScore(List<WeightedDimension> dimensions) {
        int totalWeight = dimensions.stream().mapToInt(WeightedDimension::weight).sum();
        int weighted = dimensions.stream().mapToInt(dimension -> dimension.score() * dimension.weight()).sum();
        return Math.clamp((int) Math.round((double) weighted / totalWeight), 0, 100);
    }

    private static RoleMatch roleMatch(Set<String> offerTokens, Set<String> candidateTokens) {
        Set<String> overlap = new LinkedHashSet<>(offerTokens);
        overlap.retainAll(candidateTokens);
        return new RoleMatch(percentage((double) overlap.size() / offerTokens.size()), overlap);
    }

    private static List<JobOfferCompetency> uniqueCompetencies(List<JobOfferCompetency> competencies) {
        Map<String, JobOfferCompetency> unique = new LinkedHashMap<>();
        for (JobOfferCompetency competency : competencies) {
            String normalized = ProfileNormalizer.normalize(competency.label());
            if (normalized.isEmpty()) {
                continue;
            }
            JobOfferCompetency existing = unique.get(normalized);
            if (existing == null || (!existing.isRequired() && competency.isRequired())) {
                unique.put(normalized, competency);
            }
        }
        return List.copyOf(unique.values());
    }

    private static int percentage(double value) {
        return Math.clamp((int) Math.round(value * 100), 0, 100);
    }

    private static Set<String> tokens(String value) {
        String normalized = ProfileNormalizer.normalize(value);
        if (normalized.isEmpty()) {
            return Set.of();
        }
        return java.util.Arrays.stream(normalized.split(" "))
                .filter(token -> token.length() >= 3)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static <T> List<T> safe(Collection<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    private record WeightedDimension(int score, int weight) {
    }

    private record RoleMatch(int score, Set<String> overlap) {
    }
}
