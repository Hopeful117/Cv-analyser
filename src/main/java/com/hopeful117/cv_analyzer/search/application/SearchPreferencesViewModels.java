package com.hopeful117.cv_analyzer.search.application;

import com.hopeful117.cv_analyzer.career.domain.ContractType;
import com.hopeful117.cv_analyzer.search.domain.SalaryPeriod;
import com.hopeful117.cv_analyzer.search.domain.WorkMode;
import com.hopeful117.cv_analyzer.search.web.JobSearchPreferencesForm;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class SearchPreferencesViewModels {

    private SearchPreferencesViewModels() {
    }

    public record PreferencesView(Long id, List<String> targetRoles, List<String> locations,
                                  Set<WorkMode> acceptedWorkModes, Set<ContractType> contractTypes,
                                  List<String> preferredTechnologies,
                                  List<String> excludedTechnologies,
                                  boolean openToRelocation, Integer salaryMinAmount,
                                  String salaryCurrency, SalaryPeriod salaryPeriod,
                                  Instant updatedAt) {

        /** Présentation : « 45 000 EUR / an ». Null si aucun minimum exprimé. */
        public String salaryDisplay() {
            if (salaryMinAmount == null) {
                return null;
            }
            String periodLabel = salaryPeriod == SalaryPeriod.MONTHLY ? "mois" : "an";
            return java.text.NumberFormat.getIntegerInstance(java.util.Locale.FRANCE)
                    .format(salaryMinAmount) + " " + currency() + " / " + periodLabel;
        }

        public String salaryCurrencyDisplay() {
            return salaryCurrency == null ? null : currency();
        }

        private String currency() {
            return salaryCurrency == null ? "EUR" : salaryCurrency;
        }

        public boolean empty() {
            return targetRoles.isEmpty() && locations.isEmpty()
                    && acceptedWorkModes.isEmpty() && contractTypes.isEmpty()
                    && preferredTechnologies.isEmpty() && excludedTechnologies.isEmpty()
                    && !openToRelocation && salaryMinAmount == null;
        }
    }

    /** Reconstruit le formulaire d'édition depuis l'état actif (mapping présentation). */
    public static JobSearchPreferencesForm toForm(PreferencesView view) {
        JobSearchPreferencesForm form = new JobSearchPreferencesForm();
        form.setTargetRolesText(joined(view.targetRoles()));
        form.setLocationsText(joined(view.locations()));
        form.setPreferredTechnologiesText(joined(view.preferredTechnologies()));
        form.setExcludedTechnologiesText(joined(view.excludedTechnologies()));
        form.setWorkModes(new java.util.LinkedHashSet<>(
                new TreeSet<>(view.acceptedWorkModes())));
        form.setContractTypes(new java.util.LinkedHashSet<>(
                new TreeSet<>(view.contractTypes())));
        form.setOpenToRelocation(view.openToRelocation());
        form.setSalaryMinAmount(view.salaryMinAmount());
        form.setSalaryCurrency(view.salaryCurrency());
        form.setSalaryPeriod(view.salaryPeriod());
        return form;
    }

    private static String joined(List<String> values) {
        if (values.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        values.forEach(value -> builder.append(value).append('\n'));
        return builder.toString();
    }
}
