package com.hopeful117.cv_analyzer.profile.persistence;

import com.hopeful117.cv_analyzer.profile.domain.EducationKind;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "career_profile_education")
@Getter
@Setter
@NoArgsConstructor
public class ProfileEducationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfessionalProfileEntity profile;
    @Column(name = "item_order", nullable = false)
    private int itemOrder;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EducationKind kind;
    @Column(nullable = false)
    private String label;
    @Column(length = 200)
    private String institution;
    @Column(name = "obtained_on")
    private LocalDate obtainedOn;
}
