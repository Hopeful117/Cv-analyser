package com.hopeful117.cv_analyzer.search.application;

import com.hopeful117.cv_analyzer.career.domain.ContractType;
import com.hopeful117.cv_analyzer.exception.InvalidSearchPreferencesException;
import com.hopeful117.cv_analyzer.profile.domain.ProfileNormalizer;
import com.hopeful117.cv_analyzer.search.domain.SalaryPeriod;
import com.hopeful117.cv_analyzer.search.domain.TechnologyPreference;
import com.hopeful117.cv_analyzer.search.domain.WorkMode;
import com.hopeful117.cv_analyzer.search.persistence.JobSearchPreferencesEntity;
import com.hopeful117.cv_analyzer.search.persistence.JobSearchPreferencesRepository;
import com.hopeful117.cv_analyzer.search.persistence.PreferenceLocationEntity;
import com.hopeful117.cv_analyzer.search.persistence.PreferenceRoleEntity;
import com.hopeful117.cv_analyzer.search.persistence.PreferenceTechnologyEntity;
import com.hopeful117.cv_analyzer.search.web.JobSearchPreferencesForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static com.hopeful117.cv_analyzer.search.application.SearchPreferencesViewModels.PreferencesView;

@Service
@RequiredArgsConstructor
public class JobSearchPreferencesService {

    private static final int MAX_ROLE_LENGTH = 200;
    private static final int MAX_LOCATION_LENGTH = 300;
    private static final int MAX_TECHNOLOGY_LENGTH = 120;
    private static final int MAX_SALARY_AMOUNT = 9_999_999;

    private final JobSearchPreferencesRepository repository;

    @Transactional(readOnly = true)
    public Optional<PreferencesView> getPreferencesView() {
        return repository.findActivePreferences().map(this::toView);
    }

    /**
     * Création ou mise à jour : l'état soumis remplace l'état actif. Toute la validation précède
     * la moindre mutation ; un échec laisse les préférences existantes strictement intactes.
     * Aucune valeur n'est déduite du profil professionnel ou d'une autre source.
     */
    @Transactional
    public Long saveFromForm(JobSearchPreferencesForm form) {
        List<PreferenceRoleEntity> roles = buildRoles(form.getTargetRolesText());
        List<PreferenceLocationEntity> locations = buildLocations(form.getLocationsText());
        List<PreferenceTechnologyEntity> technologies = buildTechnologies(
                form.getPreferredTechnologiesText(), form.getExcludedTechnologiesText());
        SalaryThreshold salary = validatedSalary(
                form.getSalaryMinAmount(), form.getSalaryCurrency(), form.getSalaryPeriod());

        if (form.empty()) {
            throw new InvalidSearchPreferencesException(
                    "Précisez au moins un critère de recherche pour enregistrer vos préférences.");
        }

        JobSearchPreferencesEntity preferences = repository.findActivePreferences()
                .orElseGet(JobSearchPreferencesEntity::new);
        if (preferences.getId() != null) {
            // Remplacement explicite : suppression ORM de l'agrégat puis recréation, dans la
            // même transaction (réinsertion dans des collections déjà flushées n'est pas fiable).
            repository.delete(preferences);
            repository.flush();
            preferences = new JobSearchPreferencesEntity();
        }
        preferences.setAcceptedWorkModes(new LinkedHashSet<>(form.getWorkModes()));
        preferences.setContractTypes(new LinkedHashSet<>(form.getContractTypes()));
        preferences.setOpenToRelocation(form.isOpenToRelocation());
        if (salary != null) {
            preferences.setSalaryMinAmount(salary.amount());
            preferences.setSalaryCurrency(salary.currency());
            preferences.setSalaryPeriod(salary.period());
        }
        for (PreferenceRoleEntity role : roles) {
            preferences.addTargetRole(role);
        }
        for (PreferenceLocationEntity location : locations) {
            preferences.addLocation(location);
        }
        for (PreferenceTechnologyEntity technology : technologies) {
            preferences.addTechnology(technology);
        }
        return repository.save(preferences).getId();
    }

    /* ---- construction validée des listes ---- */

    private List<PreferenceRoleEntity> buildRoles(String text) {
        List<PreferenceRoleEntity> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String line : lines(text)) {
            requireMaxLength(line, MAX_ROLE_LENGTH, "L’intitulé visé");
            String normalized = normalizedUnique(line, seen, "l’intitulé visé");
            PreferenceRoleEntity role = new PreferenceRoleEntity();
            role.setItemOrder(result.size());
            role.setLabel(line);
            role.setNormalizedLabel(normalized);
            result.add(role);
        }
        return result;
    }

    private List<PreferenceLocationEntity> buildLocations(String text) {
        List<PreferenceLocationEntity> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String line : lines(text)) {
            requireMaxLength(line, MAX_LOCATION_LENGTH, "Le lieu recherché");
            String normalized = normalizedUnique(line, seen, "le lieu recherché");
            PreferenceLocationEntity location = new PreferenceLocationEntity();
            location.setItemOrder(result.size());
            location.setLabel(line);
            location.setNormalizedLabel(normalized);
            result.add(location);
        }
        return result;
    }

    private List<PreferenceTechnologyEntity> buildTechnologies(String preferredText,
                                                               String excludedText) {
        List<String> preferredLines = lines(preferredText);
        List<String> excludedLines = lines(excludedText);
        if (preferredLines.isEmpty() && excludedLines.isEmpty()) {
            return List.of();
        }

        Set<String> preferredNames = new HashSet<>();
        List<PreferenceTechnologyEntity> result = new ArrayList<>();
        for (String line : preferredLines) {
            requireMaxLength(line, MAX_TECHNOLOGY_LENGTH, "La technologie recherchée");
            String normalized = normalizedUnique(line, preferredNames, "la technologie recherchée");
            result.add(technology(TechnologyPreference.PREFERRED, result.size(), line, normalized));
        }

        Set<String> excludedNames = new HashSet<>();
        for (String line : excludedLines) {
            requireMaxLength(line, MAX_TECHNOLOGY_LENGTH, "La technologie exclue");
            String normalized = normalizedUnique(line, excludedNames, "la technologie exclue");
            result.add(technology(TechnologyPreference.EXCLUDED, result.size(), line, normalized));
        }

        Set<String> conflicts = new HashSet<>(preferredNames);
        conflicts.retainAll(excludedNames);
        if (!conflicts.isEmpty()) {
            throw new InvalidSearchPreferencesException(
                    "Des technologies sont à la fois recherchées et exclues : "
                            + joinCaseInsensitive(conflicts)
                            + ". Retirez-les d’une des deux listes.");
        }
        return result;
    }

    private PreferenceTechnologyEntity technology(TechnologyPreference kind, int order,
                                                  String label, String normalized) {
        PreferenceTechnologyEntity item = new PreferenceTechnologyEntity();
        item.setKind(kind);
        item.setItemOrder(order);
        item.setLabel(label);
        item.setNormalizedName(normalized);
        return item;
    }

    /* ---- salaire ---- */

    private record SalaryThreshold(int amount, String currency, SalaryPeriod period) {
    }

    private SalaryThreshold validatedSalary(Integer amount, String currency, SalaryPeriod period) {
        if (amount == null) {
            return null;
        }
        if (amount <= 0 || amount > MAX_SALARY_AMOUNT) {
            throw new InvalidSearchPreferencesException(
                    "Le salaire minimum doit être un montant positif réaliste.");
        }
        String resolvedCurrency = (currency == null || currency.isBlank())
                ? "EUR" : currency.trim();
        if (!resolvedCurrency.matches("[A-Za-z]{3}")) {
            throw new InvalidSearchPreferencesException(
                    "La devise du salaire doit être un code de 3 lettres (ex. EUR).");
        }
        SalaryPeriod resolvedPeriod = period == null ? SalaryPeriod.ANNUAL : period;
        return new SalaryThreshold(amount, resolvedCurrency.toUpperCase(Locale.ROOT), resolvedPeriod);
    }

    /* ---- mapping ---- */

    private PreferencesView toView(JobSearchPreferencesEntity entity) {
        return new PreferencesView(
                entity.getId(),
                entity.getTargetRoles().stream().map(PreferenceRoleEntity::getLabel).toList(),
                entity.getLocations().stream().map(PreferenceLocationEntity::getLabel).toList(),
                entity.getAcceptedWorkModes(),
                entity.getContractTypes(),
                entity.getTechnologies().stream()
                        .filter(item -> item.getKind() == TechnologyPreference.PREFERRED)
                        .map(PreferenceTechnologyEntity::getLabel).toList(),
                entity.getTechnologies().stream()
                        .filter(item -> item.getKind() == TechnologyPreference.EXCLUDED)
                        .map(PreferenceTechnologyEntity::getLabel).toList(),
                entity.isOpenToRelocation(),
                entity.getSalaryMinAmount(),
                entity.getSalaryCurrency(),
                entity.getSalaryPeriod(),
                entity.getUpdatedAt()
        );
    }


    /* ---- helpers ---- */

    private List<String> lines(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return text.lines().map(String::trim).filter(line -> !line.isEmpty()).toList();
    }

    private String normalizedUnique(String line, Set<String> seen, String fieldLabel) {
        String normalized = ProfileNormalizer.normalize(line);
        requireContent(normalized, capitalize(fieldLabel) + " « " + line + " » n’est pas exploitable");
        if (!seen.add(normalized)) {
            throw new InvalidSearchPreferencesException(capitalize(fieldLabel) + " « "
                    + line + " » apparaît plusieurs fois.");
        }
        return normalized;
    }

    private String joinCaseInsensitive(Set<String> values) {
        List<String> list = new ArrayList<>(values);
        list.sort(String.CASE_INSENSITIVE_ORDER);
        return String.join(", ", list);
    }

    private static void requireContent(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidSearchPreferencesException(message + ".");
        }
    }

    private static void requireMaxLength(String value, int max, String label) {
        if (value.length() > max) {
            throw new InvalidSearchPreferencesException(
                    label + " dépasse la taille maximale autorisée.");
        }
    }

    private static String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
