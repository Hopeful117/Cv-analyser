package com.hopeful117.cv_analyzer.profile.application;

import com.hopeful117.cv_analyzer.career.application.UploadValidationService;
import com.hopeful117.cv_analyzer.exception.InvalidProfileException;
import com.hopeful117.cv_analyzer.profile.ai.AiProfileExtractor;
import com.hopeful117.cv_analyzer.profile.ai.ExtractedProfileProposal;
import com.hopeful117.cv_analyzer.profile.domain.EducationKind;
import com.hopeful117.cv_analyzer.profile.domain.ProfileNormalizer;
import com.hopeful117.cv_analyzer.profile.domain.SkillOrigin;
import com.hopeful117.cv_analyzer.profile.persistence.*;
import com.hopeful117.cv_analyzer.service.PdfParserService;
import com.hopeful117.cv_analyzer.profile.web.ProfileForm;
import com.hopeful117.cv_analyzer.profile.web.ProfileProposalForm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static com.hopeful117.cv_analyzer.profile.application.ProfileViewModels.*;

@Service
@RequiredArgsConstructor
public class ProfessionalProfileService {

    private static final int MAX_SKILL_LENGTH = 120;
    private static final int MAX_LANGUAGE_LENGTH = 60;
    private static final int MAX_LEVEL_LENGTH = 40;
    private static final int MAX_EDUCATION_LABEL_LENGTH = 255;
    private static final int MAX_INSTITUTION_LENGTH = 200;

    private final ProfessionalProfileRepository repository;
    private final AiProfileExtractor extractor;
    private final PdfParserService pdfParserService;
    private final UploadValidationService uploadValidationService;

    @Value("${spring.ai.openai.model:gpt-4o-mini}")
    private String aiModel;

    @Transactional(readOnly = true)
    public Optional<ProfileView> getProfileView() {
        return repository.findLocalProfile().map(this::toView);
    }

    /**
     * Création ou mise à jour manuelle : l'état soumis remplace l'état existant et l'utilisateur a
     * relu chaque valeur ; les compétences sont donc requalifiées en saisie manuelle et la
     * traçabilité d'assistance CV précédente devient obsolète. Tout est validé AVANT toute
     * modification de l'agrégat : un échec laisse le profil existant strictement intact.
     */
    @Transactional
    public Long saveFromForm(ProfileForm form) {
        String fullName = clean(form.getFullName());
        String professionalTitle = clean(form.getProfessionalTitle());
        String referenceLocation = clean(form.getReferenceLocation());

        List<ProfileSkillEntity> skills = buildManualSkills(form.getSkillsText());
        List<ProfileLanguageEntity> languages = buildManualLanguages(form.getLanguagesText());
        List<ProfileEducationEntity> educations = new java.util.ArrayList<>();
        educations.addAll(buildEducations(form.getEducationText(), EducationKind.EDUCATION));
        educations.addAll(buildEducations(form.getCertificationText(), EducationKind.CERTIFICATION));
        List<ProfileExperienceEntity> experiences =
                validExperiences(form.getExperiences(), "expérience");

        boolean nothingSubmitted = fullName == null && professionalTitle == null
                && referenceLocation == null
                && skills.isEmpty() && languages.isEmpty()
                && educations.isEmpty() && experiences.isEmpty();
        if (nothingSubmitted) {
            throw new InvalidProfileException(
                    "Renseignez au moins une donnée professionnelle pour enregistrer votre profil.");
        }

        ProfessionalProfileEntity profile = repository.findLocalProfile()
                .orElseGet(ProfessionalProfileEntity::new);
        if (profile.getId() != null) {
            // Remplacement explicite : suppression ORM de l'agrégat (cascade enfants) puis
            // recréation. Réinsérer dans les collections existantes après suppressions partielles
            // produit des ordres SQL non fiables (insert avant delete) selon le fournisseur.
            // Aucune autre table ne référence le profil : l'identité reste locale à cet agrégat.
            profile.setFullName(fullName);
            profile.setProfessionalTitle(professionalTitle);
            profile.setReferenceLocation(referenceLocation);
            repository.delete(profile);
            repository.flush();
            profile = new ProfessionalProfileEntity();
        }
        profile.setFullName(fullName);
        profile.setProfessionalTitle(professionalTitle);
        profile.setReferenceLocation(referenceLocation);

        skills.forEach(profile::addSkill);
        languages.forEach(profile::addLanguage);
        educations.forEach(profile::addEducation);
        experiences.forEach(profile::addExperience);

        return repository.save(profile).getId();
    }

    /**
     * Extrait une PROPOSITION depuis un CV fraîchement uploadé (le PDF n'est pas conservé par le
     * produit). Aucune écriture ici : l'appel IA se fait hors transaction et la proposition est
     * rendue dans un formulaire de revue.
     */
    public ProfileViewModels.ProposalReview proposeFromCv(MultipartFile cvFile) {
        uploadValidationService.requirePdf(cvFile, "Le CV");
        String resumeText;
        try {
            resumeText = pdfParserService.extractText(cvFile);
        } catch (IOException exception) {
            throw new InvalidProfileException("Le CV n’a pas pu être lu.");
        }
        requireContent(resumeText, "Le texte extrait du CV");
        ExtractedProfileProposal proposal = extractor.extract(resumeText);
        return toProposalReview(proposal);
    }

    /**
     * Applique explicitement les valeurs cochées de la proposition : ajout après déduplication pour
     * les listes, écrasement uniquement si la case correspondante est cochée pour les champs
     * simples. Le profil existant n'est jamais remplacé silencieusement.
     */
    @Transactional
    public void applyProposal(ProfileProposalForm form) {
        boolean hasExistingProfile = repository.findLocalProfile().isPresent();
        if (!hasExistingProfile && form.nothingSelected()) {
            throw new InvalidProfileException(
                    "Sélectionnez au moins une donnée à conserver dans votre profil.");
        }
        ProfessionalProfileEntity profile = repository.findLocalProfile()
                .orElseGet(ProfessionalProfileEntity::new);

        boolean applied = false;
        if (form.isApplyFullName() && hasText(form.getFullName())) {
            profile.setFullName(clean(form.getFullName()));
            applied = true;
        }
        if (form.isApplyProfessionalTitle() && hasText(form.getProfessionalTitle())) {
            profile.setProfessionalTitle(clean(form.getProfessionalTitle()));
            applied = true;
        }
        if (form.isApplyReferenceLocation() && hasText(form.getReferenceLocation())) {
            profile.setReferenceLocation(clean(form.getReferenceLocation()));
            applied = true;
        }

        Set<String> knownSkills = normalizedValues(profile.getSkills().stream()
                .map(ProfileSkillEntity::getNormalizedName).toList());
        Set<String> knownLanguages = normalizedValues(profile.getLanguages().stream()
                .map(ProfileLanguageEntity::getNormalizedLanguage).toList());

        for (ProfileProposalForm.SkillEntry entry : safe(form.getSkills())) {
            if (!entry.isApply()) {
                continue;
            }
            String label = clean(entry.getLabel());
            requireContent(label, "La compétence");
            String normalized = ProfileNormalizer.normalize(label);
            if (normalized.isEmpty()) {
                throw new InvalidProfileException("La compétence « " + label + " » n’est pas exploitable.");
            }
            if (knownSkills.add(normalized)) {
                profile.addSkill(skill(label, normalized, SkillOrigin.FROM_CV));
                applied = true;
            }
        }
        for (ProfileProposalForm.LanguageEntry entry : safe(form.getLanguages())) {
            if (!entry.isApply()) {
                continue;
            }
            String language = clean(entry.getLanguage());
            requireContent(language, "La langue");
            String level = clean(entry.getLevel());
            String normalized = ProfileNormalizer.normalize(language);
            if (!normalized.isEmpty() && knownLanguages.add(normalized)) {
                profile.addLanguage(language(language, normalized, level));
                applied = true;
            }
        }
        for (ProfileProposalForm.ExperienceEntry entry : safe(form.getExperiences())) {
            if (!entry.isApply()) {
                continue;
            }
            ProfileForm.ExperienceLine line = new ProfileForm.ExperienceLine();
            line.setTitle(entry.getTitle());
            line.setCompany(entry.getCompany());
            line.setStartDate(entry.getStartDate());
            line.setEndDate(entry.getEndDate());
            line.setDescription(entry.getDescription());
            List<ProfileExperienceEntity> validated =
                    validExperiences(List.of(line), "expérience proposée");
            if (!validated.isEmpty()) {
                profile.addExperience(validated.getFirst());
                applied = true;
            }
        }
        for (ProfileProposalForm.EducationEntry entry : safe(form.getEducations())) {
            if (!entry.isApply()) {
                continue;
            }
            ProfileEducationEntity education = validEducation(
                    kindOf(entry.getKind()), clean(entry.getLabel()),
                    clean(entry.getInstitution()), entry.getObtainedOn(), "formation proposée");
            profile.addEducation(education);
            applied = true;
        }

        if (!applied) {
            throw new InvalidProfileException(
                    "Aucune donnée proposée n’a été retenue : le profil reste inchangé.");
        }

        profile.setAiProvider(clean(form.getAiProvider()) != null ? clean(form.getAiProvider()) : "OpenAI");
        profile.setAiModel(clean(aiModel));
        profile.setPromptVersion(AiProfileExtractor.PROMPT_VERSION);
        profile.setCvAssistedAt(Instant.now());
        repository.save(profile);
    }

    /* ---- mapping et helpers privés ---- */

    private List<ProfileSkillEntity> buildManualSkills(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<ProfileSkillEntity> result = new java.util.ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String line : text.lines().map(String::trim).filter(l -> !l.isEmpty()).toList()) {
            requireMaxLength(line, MAX_SKILL_LENGTH, "La compétence");
            String normalized = ProfileNormalizer.normalize(line);
            if (!seen.add(normalized)) {
                throw new InvalidProfileException(
                        "La compétence « " + line + " » apparaît plusieurs fois.");
            }
            ProfileSkillEntity skill = new ProfileSkillEntity();
            skill.setLabel(line);
            skill.setNormalizedName(normalized);
            skill.setOrigin(SkillOrigin.MANUAL);
            result.add(skill);
        }
        return result;
    }

    private List<ProfileLanguageEntity> buildManualLanguages(String text) {
        List<ProfileLanguageEntity> parsed = parseLanguageLines(text);
        Set<String> seen = new HashSet<>();
        for (ProfileLanguageEntity language : parsed) {
            if (!seen.add(language.getNormalizedLanguage())) {
                throw new InvalidProfileException("La langue « "
                        + language.getLanguage() + " » apparaît plusieurs fois.");
            }
        }
        return parsed;
    }

    private void replaceEducations(ProfessionalProfileEntity profile,
                                   List<ProfileEducationEntity> educations,
                                   List<ProfileEducationEntity> certifications) {
        profile.getEducations().clear();
        educations.forEach(profile::addEducation);
        certifications.forEach(profile::addEducation);
    }

    private ProfileSkillEntity skill(String label, String normalized, SkillOrigin origin) {
        ProfileSkillEntity skill = new ProfileSkillEntity();
        skill.setLabel(label);
        skill.setNormalizedName(normalized);
        skill.setOrigin(origin);
        return skill;
    }

    private ProfileLanguageEntity language(String language, String normalized, String level) {
        ProfileLanguageEntity entity = new ProfileLanguageEntity();
        entity.setLanguage(language);
        entity.setNormalizedLanguage(normalized);
        entity.setLevel(level);
        return entity;
    }

    private List<ProfileLanguageEntity> parseLanguageLines(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<ProfileLanguageEntity> result = new java.util.ArrayList<>();
        text.lines().map(String::trim).filter(line -> !line.isEmpty()).forEach(line -> {
            String[] parts = line.split("\\s*[:|]\\s*", 2);
            String language = parts[0].trim();
            String level = parts.length > 1 ? parts[1].trim() : null;
            requireContent(language, "La langue");
            requireMaxLength(language, MAX_LANGUAGE_LENGTH, "La langue");
            if (level != null) {
                requireMaxLength(level, MAX_LEVEL_LENGTH, "Le niveau");
                if (level.isEmpty()) {
                    level = null;
                }
            }
            result.add(language(language, ProfileNormalizer.normalize(language), level));
        });
        return result;
    }

    private List<ProfileEducationEntity> buildEducations(String text, EducationKind kind) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<ProfileEducationEntity> result = new java.util.ArrayList<>();
        int index = 1;
        for (String raw : text.lines().map(String::trim).filter(line -> !line.isEmpty()).toList()) {
            String[] parts = raw.split("\\|", -1);
            String label = parts[0].trim();
            String institution = parts.length > 1 ? parts[1].trim() : null;
            LocalDate obtainedOn = parts.length > 2 ? ProposalDateParser.parse(parts[2]) : null;
            result.add(validEducation(kind, label,
                    institution == null || institution.isEmpty() ? null : institution,
                    obtainedOn, "ligne " + index));
            index++;
        }
        return result;
    }

    private ProfileEducationEntity validEducation(EducationKind kind, String label,
                                                  String institution, LocalDate obtainedOn,
                                                  String contextLabel) {
        requireContent(label, "L’intitulé de formation/certification (" + contextLabel + ")");
        requireMaxLength(label, MAX_EDUCATION_LABEL_LENGTH, "L’intitulé");
        if (institution != null) {
            requireMaxLength(institution, MAX_INSTITUTION_LENGTH, "L’établissement");
            if (institution.isEmpty()) {
                institution = null;
            }
        }
        ProfileEducationEntity education = new ProfileEducationEntity();
        education.setKind(kind);
        education.setLabel(label);
        education.setInstitution(institution);
        education.setObtainedOn(obtainedOn);
        return education;
    }

    private List<ProfileExperienceEntity> validExperiences(List<ProfileForm.ExperienceLine> lines,
                                                           String contextLabel) {
        List<ProfileExperienceEntity> result = new java.util.ArrayList<>();
        if (lines == null) {
            return result;
        }
        int position = 1;
        for (ProfileForm.ExperienceLine line : lines) {
            boolean allBlank = clean(line.getTitle()) == null && clean(line.getCompany()) == null
                    && line.getStartDate() == null && line.getEndDate() == null
                    && clean(line.getDescription()) == null;
            if (allBlank) {
                continue;
            }
            String title = clean(line.getTitle());
            requireContent(title, "L’intitulé du poste (" + contextLabel + " " + position + ")");
            ProposalDateParser.validateRange(line.getStartDate(), line.getEndDate(),
                    "L’expérience « " + title + " »");
            ProfileExperienceEntity experience = new ProfileExperienceEntity();
            experience.setTitle(title);
            experience.setCompany(clean(line.getCompany()));
            experience.setStartDate(line.getStartDate());
            experience.setEndDate(line.getEndDate());
            experience.setDescription(clean(line.getDescription()));
            result.add(experience);
            position++;
        }
        return result;
    }

    private EducationKind kindOf(String rawKind) {
        if (rawKind == null || rawKind.isBlank()) {
            return EducationKind.EDUCATION;
        }
        try {
            return EducationKind.valueOf(rawKind.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidProfileException("Type de formation/certification inconnu : " + rawKind);
        }
    }

    private ProfileView toView(ProfessionalProfileEntity profile) {
        return new ProfileView(
                profile.getId(),
                profile.getFullName(),
                profile.getProfessionalTitle(),
                profile.getReferenceLocation(),
                profile.getSkills().stream()
                        .map(skill -> new SkillView(skill.getLabel(), skill.getOrigin().getFrenchLabel()))
                        .toList(),
                profile.getExperiences().stream()
                        .map(experience -> new ExperienceView(experience.getTitle(),
                                experience.getCompany(), experience.getStartDate(),
                                experience.getEndDate(), experience.getDescription()))
                        .toList(),
                profile.getEducations().stream()
                        .map(education -> new EducationView(education.getKind().getFrenchLabel(),
                                education.getLabel(), education.getInstitution(),
                                education.getObtainedOn()))
                        .toList(),
                profile.getLanguages().stream()
                        .map(language -> new LanguageView(language.getLanguage(), language.getLevel()))
                        .toList(),
                profile.getCvAssistedAt() != null,
                profile.getUpdatedAt()
        );
    }

    private ProfileViewModels.ProposalReview toProposalReview(ExtractedProfileProposal proposal) {
        ProfileProposalForm form = new ProfileProposalForm();
        if (proposal.getFullName() != null && !proposal.getFullName().isBlank()) {
            form.setApplyFullName(true);
            form.setFullName(proposal.getFullName().trim());
        }
        if (proposal.getProfessionalTitle() != null && !proposal.getProfessionalTitle().isBlank()) {
            form.setApplyProfessionalTitle(true);
            form.setProfessionalTitle(proposal.getProfessionalTitle().trim());
        }
        if (proposal.getLocation() != null && !proposal.getLocation().isBlank()) {
            form.setApplyReferenceLocation(true);
            form.setReferenceLocation(proposal.getLocation().trim());
        }
        if (proposal.getSkills() != null) {
            proposal.getSkills().stream()
                    .filter(item -> item != null && hasText(item.getName()))
                    .forEach(item -> {
                        ProfileProposalForm.SkillEntry entry = new ProfileProposalForm.SkillEntry();
                        entry.setApply(true);
                        entry.setLabel(item.getName().trim());
                        form.getSkills().add(entry);
                    });
        }
        if (proposal.getExperiences() != null) {
            proposal.getExperiences().stream()
                    .filter(item -> item != null && hasText(item.getTitle()))
                    .forEach(item -> {
                        ProfileProposalForm.ExperienceEntry entry = new ProfileProposalForm.ExperienceEntry();
                        entry.setApply(true);
                        entry.setTitle(item.getTitle().trim());
                        entry.setCompany(clean(item.getCompany()));
                        entry.setStartDate(ProposalDateParser.parse(item.getStartDate()));
                        entry.setEndDate(ProposalDateParser.parse(item.getEndDate()));
                        entry.setDescription(clean(item.getSummary()));
                        form.getExperiences().add(entry);
                    });
        }
        if (proposal.getEducations() != null) {
            proposal.getEducations().stream()
                    .filter(item -> item != null && hasText(item.getLabel()))
                    .forEach(item -> {
                        ProfileProposalForm.EducationEntry entry = new ProfileProposalForm.EducationEntry();
                        entry.setApply(true);
                        entry.setKind(kindOf(item.getKind()).name());
                        entry.setLabel(item.getLabel().trim());
                        entry.setInstitution(clean(item.getInstitution()));
                        entry.setObtainedOn(ProposalDateParser.parse(item.getObtainedOn()));
                        form.getEducations().add(entry);
                    });
        }
        if (proposal.getLanguages() != null) {
            proposal.getLanguages().stream()
                    .filter(item -> item != null && hasText(item.getLanguage()))
                    .forEach(item -> {
                        ProfileProposalForm.LanguageEntry entry = new ProfileProposalForm.LanguageEntry();
                        entry.setApply(true);
                        entry.setLanguage(item.getLanguage().trim());
                        entry.setLevel(clean(item.getLevel()));
                        form.getLanguages().add(entry);
                    });
        }
        return new ProfileViewModels.ProposalReview(form, "OpenAI", aiModel);
    }

    private <T> List<T> safe(List<T> list) {
        return list == null ? List.of() : list;
    }

    private Set<String> normalizedValues(List<String> values) {
        return new HashSet<>(values);
    }

    private static void requireContent(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileException(label + " est requis.");
        }
    }

    private static void requireMaxLength(String value, int max, String label) {
        if (value.length() > max) {
            throw new InvalidProfileException(label + " dépasse la taille maximale autorisée.");
        }
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
