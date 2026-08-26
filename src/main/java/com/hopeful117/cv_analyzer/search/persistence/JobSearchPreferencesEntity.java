package com.hopeful117.cv_analyzer.search.persistence;

import com.hopeful117.cv_analyzer.career.domain.ContractType;
import com.hopeful117.cv_analyzer.search.domain.SalaryPeriod;
import com.hopeful117.cv_analyzer.search.domain.WorkMode;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "career_job_search_preferences")
@Getter
@Setter
@NoArgsConstructor
public class JobSearchPreferencesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Modes acceptés ; vide = ouvert à tous les modes (contrainte éliminatoire si non vide). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "career_preference_work_mode",
            joinColumns = @JoinColumn(name = "preferences_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 16)
    private Set<WorkMode> acceptedWorkModes = new LinkedHashSet<>();

    /** Contrats acceptés ; vide = aucune restriction (contrainte éliminatoire si non vide). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "career_preference_contract_type",
            joinColumns = @JoinColumn(name = "preferences_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false, length = 24)
    private Set<ContractType> contractTypes = new LinkedHashSet<>();

    /**
     * false : une offre hors zones listées est disqualifiée.
     * true : les zones listées deviennent une simple priorité de pertinence.
     */
    @Column(name = "open_to_relocation", nullable = false)
    private boolean openToRelocation;

    @Column(name = "salary_min_amount")
    private Integer salaryMinAmount;

    @Column(name = "salary_currency", length = 3)
    private String salaryCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "salary_period", length = 10)
    private SalaryPeriod salaryPeriod;

    @OrderBy("itemOrder ASC")
    @OneToMany(mappedBy = "preferences", cascade = jakarta.persistence.CascadeType.ALL,
            orphanRemoval = true)
    private List<PreferenceRoleEntity> targetRoles = new ArrayList<>();

    @OrderBy("itemOrder ASC")
    @OneToMany(mappedBy = "preferences", cascade = jakarta.persistence.CascadeType.ALL,
            orphanRemoval = true)
    private List<PreferenceLocationEntity> locations = new ArrayList<>();

    /** Technologies recherchées et exclues, discriminées par {@link TechnologyPreference}. */
    @OrderBy("itemOrder ASC")
    @OneToMany(mappedBy = "preferences", cascade = jakarta.persistence.CascadeType.ALL,
            orphanRemoval = true)
    private List<PreferenceTechnologyEntity> technologies = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void createTimestamps() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void updateTimestamp() {
        updatedAt = Instant.now();
    }

    public boolean empty() {
        return acceptedWorkModes.isEmpty() && contractTypes.isEmpty()
                && !openToRelocation && salaryMinAmount == null
                && targetRoles.isEmpty() && locations.isEmpty()
                && technologies.isEmpty();
    }

    public void addTargetRole(PreferenceRoleEntity role) {
        role.setItemOrder(targetRoles.size());
        role.setPreferences(this);
        targetRoles.add(role);
    }

    public void addLocation(PreferenceLocationEntity location) {
        location.setItemOrder(locations.size());
        location.setPreferences(this);
        locations.add(location);
    }

    public void addTechnology(PreferenceTechnologyEntity technology) {
        technology.setItemOrder(technologies.size());
        technology.setPreferences(this);
        technologies.add(technology);
    }
}
