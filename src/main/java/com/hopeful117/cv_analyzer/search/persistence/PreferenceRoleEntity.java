package com.hopeful117.cv_analyzer.search.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "career_preference_role")
@Getter
@Setter
@NoArgsConstructor
public class PreferenceRoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "preferences_id", nullable = false)
    private JobSearchPreferencesEntity preferences;
    @Column(name = "item_order", nullable = false)
    private int itemOrder;
    @Column(nullable = false, length = 200)
    private String label;
    @Column(name = "normalized_label", nullable = false, length = 200)
    private String normalizedLabel;
}
