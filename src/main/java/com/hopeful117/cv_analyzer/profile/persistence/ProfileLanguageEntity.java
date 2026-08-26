package com.hopeful117.cv_analyzer.profile.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "career_profile_language")
@Getter
@Setter
@NoArgsConstructor
public class ProfileLanguageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfessionalProfileEntity profile;
    @Column(name = "item_order", nullable = false)
    private int itemOrder;
    @Column(nullable = false, length = 60)
    private String language;
    @Column(name = "normalized_language", nullable = false, length = 60)
    private String normalizedLanguage;
    @Column(length = 40)
    private String level;
}
