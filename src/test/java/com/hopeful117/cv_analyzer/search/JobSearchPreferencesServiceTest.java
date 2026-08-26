package com.hopeful117.cv_analyzer.search;

import com.hopeful117.cv_analyzer.career.domain.ContractType;
import com.hopeful117.cv_analyzer.exception.InvalidSearchPreferencesException;
import com.hopeful117.cv_analyzer.search.application.JobSearchPreferencesService;
import com.hopeful117.cv_analyzer.search.application.SearchPreferencesViewModels.PreferencesView;
import com.hopeful117.cv_analyzer.search.domain.SalaryPeriod;
import com.hopeful117.cv_analyzer.search.domain.WorkMode;
import com.hopeful117.cv_analyzer.search.persistence.JobSearchPreferencesEntity;
import com.hopeful117.cv_analyzer.search.persistence.JobSearchPreferencesRepository;
import com.hopeful117.cv_analyzer.search.web.JobSearchPreferencesForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JobSearchPreferencesServiceTest {

    private JobSearchPreferencesRepository repository;
    private JobSearchPreferencesService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(JobSearchPreferencesRepository.class);
        service = new JobSearchPreferencesService(repository);
        when(repository.save(any())).thenAnswer(invocation -> {
            JobSearchPreferencesEntity saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(1L);
            }
            return saved;
        });
    }

    @Test
    void getWhenAbsentReturnsEmpty() {
        when(repository.findActivePreferences()).thenReturn(Optional.empty());
        assertThat(service.getPreferencesView()).isEmpty();
    }

    @Test
    void createThenUpdateReplacesSingleActiveSet() {
        when(repository.findActivePreferences())
                .thenReturn(Optional.empty())
                .thenAnswer(invocation -> Optional.of(new JobSearchPreferencesEntity()));

        JobSearchPreferencesForm initial = minimalForm();
        service.saveFromForm(initial);

        JobSearchPreferencesForm updated = new JobSearchPreferencesForm();
        updated.setTargetRolesText("Backend Engineer");
        service.saveFromForm(updated);

        verify(repository, Mockito.times(2)).save(any());
    }

    @Test
    void fullyEmptySubmissionIsRejected() {
        assertThatThrownBy(() -> service.saveFromForm(new JobSearchPreferencesForm()))
                .isInstanceOf(InvalidSearchPreferencesException.class)
                .hasMessageContaining("au moins un critère");
        verify(repository, never()).save(any());
    }

    @Test
    void duplicateNormalizedRolesAreRejected() {
        JobSearchPreferencesForm form = new JobSearchPreferencesForm();
        form.setTargetRolesText("Développeur Java\ndéveloppeur  java");
        assertThatThrownBy(() -> service.saveFromForm(form))
                .isInstanceOf(InvalidSearchPreferencesException.class)
                .hasMessageContaining("plusieurs fois");
    }

    @Test
    void technologyPresentInPreferredAndExcludedIsRejectedDeterministically() {
        JobSearchPreferencesForm form = new JobSearchPreferencesForm();
        form.setPreferredTechnologiesText("Java\nSpring");
        form.setExcludedTechnologiesText("java");
        assertThatThrownBy(() -> service.saveFromForm(form))
                .isInstanceOf(InvalidSearchPreferencesException.class)
                .hasMessageContaining("Java")
                .hasMessageContaining("exclues");
    }

    @Test
    void salaryRequiresPositiveAmountAndDefaultsCurrencyAndPeriod() {
        JobSearchPreferencesForm invalid = minimalForm();
        invalid.setSalaryMinAmount(-5);
        assertThatThrownBy(() -> service.saveFromForm(invalid))
                .isInstanceOf(InvalidSearchPreferencesException.class)
                .hasMessageContaining("positif");

        JobSearchPreferencesForm badCurrency = minimalForm();
        badCurrency.setSalaryMinAmount(40_000);
        badCurrency.setSalaryCurrency("euros");
        assertThatThrownBy(() -> service.saveFromForm(badCurrency))
                .isInstanceOf(InvalidSearchPreferencesException.class)
                .hasMessageContaining("3 lettres");

        JobSearchPreferencesForm valid = minimalForm();
        valid.setSalaryMinAmount(40_000);
        Long id = service.saveFromForm(valid);
        assertThat(id).isNotNull();
    }

    @Test
    void excludedTechnologyAloneCountsAsAMeaningfulCriterion() {
        JobSearchPreferencesForm form = new JobSearchPreferencesForm();
        form.setExcludedTechnologiesText("COBOL");
        service.saveFromForm(form);
        verify(repository).save(any());
    }

    @Test
    void workModeSemanticsAreCarriedAsExplicitAcceptedSet() {
        JobSearchPreferencesForm form = minimalForm();
        form.getWorkModes().add(WorkMode.REMOTE);
        form.getContractTypes().add(ContractType.CDI);
        service.saveFromForm(form);

        verify(repository).save(Mockito.argThat(saved ->
                saved.getAcceptedWorkModes().equals(java.util.Set.of(WorkMode.REMOTE))
                        && saved.getContractTypes().equals(java.util.Set.of(ContractType.CDI))));
    }

    @Test
    void viewExposesSalaryDisplayForTrustedStateOnly() {
        JobSearchPreferencesForm form = minimalForm();
        form.setSalaryMinAmount(45_000);
        service.saveFromForm(form);

        JobSearchPreferencesEntity saved = new JobSearchPreferencesEntity();
        saved.setSalaryMinAmount(45_000);
        saved.setSalaryPeriod(SalaryPeriod.ANNUAL);
        when(repository.findActivePreferences()).thenReturn(Optional.of(saved));

        PreferencesView view = service.getPreferencesView().orElseThrow();
        assertThat(view.salaryDisplay()).contains("45 000").contains("EUR / an");
    }

    private JobSearchPreferencesForm minimalForm() {
        JobSearchPreferencesForm form = new JobSearchPreferencesForm();
        form.setTargetRolesText("Développeur Java");
        return form;
    }
}
