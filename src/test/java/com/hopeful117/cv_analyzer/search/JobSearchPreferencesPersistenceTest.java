package com.hopeful117.cv_analyzer.search;

import com.hopeful117.cv_analyzer.career.domain.ContractType;
import com.hopeful117.cv_analyzer.exception.InvalidSearchPreferencesException;
import com.hopeful117.cv_analyzer.search.application.JobSearchPreferencesService;
import com.hopeful117.cv_analyzer.search.application.SearchPreferencesViewModels.PreferencesView;
import com.hopeful117.cv_analyzer.search.domain.SalaryPeriod;
import com.hopeful117.cv_analyzer.search.domain.WorkMode;
import com.hopeful117.cv_analyzer.search.persistence.JobSearchPreferencesRepository;
import com.hopeful117.cv_analyzer.search.web.JobSearchPreferencesForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class JobSearchPreferencesPersistenceTest {

    @Autowired JobSearchPreferencesRepository repository;
    @Autowired JobSearchPreferencesService service;
    @Autowired JdbcTemplate jdbcTemplate;

    private JobSearchPreferencesForm fullForm() {
        JobSearchPreferencesForm form = new JobSearchPreferencesForm();
        form.setTargetRolesText("Développeur Java\nBackend Engineer");
        form.setLocationsText("Bourges\nLyon");
        form.setPreferredTechnologiesText("Spring Boot\nPostgreSQL");
        form.setExcludedTechnologiesText("COBOL");
        form.getWorkModes().add(WorkMode.REMOTE);
        form.getWorkModes().add(WorkMode.HYBRID);
        form.getContractTypes().add(ContractType.CDI);
        form.setOpenToRelocation(true);
        form.setSalaryMinAmount(45_000);
        form.setSalaryCurrency("eur");
        form.setSalaryPeriod(SalaryPeriod.ANNUAL);
        return form;
    }

    @Test
    void flywayCreatesPreferenceTables() {
        for (String table : new String[]{
                "career_job_search_preferences", "career_preference_role",
                "career_preference_location", "career_preference_technology",
                "career_preference_work_mode", "career_preference_contract_type"}) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE upper(table_name) = ?",
                    Integer.class, table.toUpperCase());
            assertThat(count != null && count > 0).as(table).isTrue();
        }
    }

    @Test
    void completeRoundTripIncludingEnumSetsAndLists() {
        Long id = service.saveFromForm(fullForm());
        repository.flush();

        PreferencesView view = service.getPreferencesView().orElseThrow();
        assertThat(view.id()).isEqualTo(id);
        assertThat(view.targetRoles()).containsExactly("Développeur Java", "Backend Engineer");
        assertThat(view.locations()).containsExactly("Bourges", "Lyon");
        assertThat(view.preferredTechnologies()).containsExactly("Spring Boot", "PostgreSQL");
        assertThat(view.excludedTechnologies()).containsExactly("COBOL");
        assertThat(view.acceptedWorkModes()).containsExactlyInAnyOrder(WorkMode.REMOTE, WorkMode.HYBRID);
        assertThat(view.contractTypes()).containsExactly(ContractType.CDI);
        assertThat(view.openToRelocation()).isTrue();
        assertThat(view.salaryMinAmount()).isEqualTo(45_000);
        assertThat(view.salaryCurrency()).isEqualTo("EUR");
        assertThat(view.salaryPeriod()).isEqualTo(SalaryPeriod.ANNUAL);
        assertThat(view.salaryDisplay()).contains("45", "EUR / an");

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM career_preference_work_mode WHERE preferences_id = ?",
                Integer.class, id)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM career_preference_technology WHERE preference_kind = 'EXCLUDED'",
                Integer.class)).isEqualTo(1);
    }

    @Test
    void normalizedRoleUniquenessIsEnforcedAtDatabaseLevel() {
        Long id = service.saveFromForm(fullForm());
        repository.flush();

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO career_preference_role (preferences_id, item_order, label, normalized_label) "
                        + "VALUES (?, 9, 'développeur java', 'developpeur java')", id))
                .isInstanceOf(org.springframework.dao.DataAccessException.class);
    }

    @Test
    void updateReplacesPreviousStateEvenWhenAnItemIsKept() {
        service.saveFromForm(fullForm());

        JobSearchPreferencesForm edited = new JobSearchPreferencesForm();
        edited.setTargetRolesText("Développeur Java");   // conservé volontairement
        edited.getContractTypes().add(ContractType.CDD);
        service.saveFromForm(edited);
        repository.flush();

        PreferencesView view = service.getPreferencesView().orElseThrow();
        assertThat(view.targetRoles()).containsExactly("Développeur Java");
        assertThat(view.locations()).isEmpty();
        assertThat(view.contractTypes()).containsExactly(ContractType.CDD);
        assertThat(view.salaryMinAmount()).isNull();

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM career_job_search_preferences", Integer.class)).isEqualTo(1);
    }

    @Test
    void failedValidationLeavesExistingPreferencesIntact() {
        service.saveFromForm(fullForm());

        JobSearchPreferencesForm broken = new JobSearchPreferencesForm();
        broken.setPreferredTechnologiesText("Java");
        broken.setExcludedTechnologiesText("JAVA");

        assertThatThrownBy(() -> service.saveFromForm(broken))
                .isInstanceOf(InvalidSearchPreferencesException.class)
                .hasMessageContaining("recherchées et exclues");

        PreferencesView untouched = service.getPreferencesView().orElseThrow();
        assertThat(untouched.targetRoles()).hasSize(2);
        assertThat(untouched.excludedTechnologies()).containsExactly("COBOL");
    }
}
