package com.hopeful117.cv_analyzer.profile.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "career_professional_profile")
@Getter
@Setter
@NoArgsConstructor
public class ProfessionalProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "full_name", length = 200)
    private String fullName;
    @Column(name = "professional_title", length = 200)
    private String professionalTitle;
    @Column(name = "reference_location", length = 300)
    private String referenceLocation;
    @Column(name = "ai_provider", length = 80)
    private String aiProvider;
    @Column(name = "ai_model", length = 120)
    private String aiModel;
    @Column(name = "prompt_version", length = 80)
    private String promptVersion;
    @Column(name = "cv_assisted_at")
    private Instant cvAssistedAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("itemOrder ASC")
    private List<ProfileSkillEntity> skills = new ArrayList<>();
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("itemOrder ASC")
    private List<ProfileExperienceEntity> experiences = new ArrayList<>();
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("itemOrder ASC")
    private List<ProfileEducationEntity> educations = new ArrayList<>();
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("itemOrder ASC")
    private List<ProfileLanguageEntity> languages = new ArrayList<>();

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

    public void addSkill(ProfileSkillEntity skill) {
        skill.setItemOrder(skills.size());
        skill.setProfile(this);
        skills.add(skill);
    }

    public void addExperience(ProfileExperienceEntity experience) {
        experience.setItemOrder(experiences.size());
        experience.setProfile(this);
        experiences.add(experience);
    }

    public void addEducation(ProfileEducationEntity education) {
        education.setItemOrder(educations.size());
        education.setProfile(this);
        educations.add(education);
    }

    public void addLanguage(ProfileLanguageEntity language) {
        language.setItemOrder(languages.size());
        language.setProfile(this);
        languages.add(language);
    }
}
