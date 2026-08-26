package com.hopeful117.cv_analyzer.profile.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "career_profile_experience")
@Getter
@Setter
@NoArgsConstructor
public class ProfileExperienceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfessionalProfileEntity profile;
    @Column(name = "item_order", nullable = false)
    private int itemOrder;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(length = 200)
    private String company;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @Column(length = 2000)
    private String description;
}
