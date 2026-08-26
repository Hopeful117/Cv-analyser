package com.hopeful117.cv_analyzer.profile.persistence;

import com.hopeful117.cv_analyzer.profile.domain.SkillOrigin;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "career_profile_skill")
@Getter
@Setter
@NoArgsConstructor
public class ProfileSkillEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfessionalProfileEntity profile;
    @Column(name = "item_order", nullable = false)
    private int itemOrder;
    @Column(nullable = false, length = 120)
    private String label;
    @Column(name = "normalized_name", nullable = false, length = 120)
    private String normalizedName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SkillOrigin origin;
}
