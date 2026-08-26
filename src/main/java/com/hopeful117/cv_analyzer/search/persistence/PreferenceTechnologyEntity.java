package com.hopeful117.cv_analyzer.search.persistence;

import com.hopeful117.cv_analyzer.search.domain.TechnologyPreference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "career_preference_technology")
@Getter
@Setter
@NoArgsConstructor
public class PreferenceTechnologyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "preferences_id", nullable = false)
    private JobSearchPreferencesEntity preferences;
    @Column(name = "item_order", nullable = false)
    private int itemOrder;
    @Enumerated(EnumType.STRING)
    @Column(name = "preference_kind", nullable = false, length = 16)
    private TechnologyPreference kind;
    @Column(nullable = false, length = 120)
    private String label;
    @Column(name = "normalized_name", nullable = false, length = 120)
    private String normalizedName;
}
