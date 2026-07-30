package com.hopeful117.cv_analyzer.career.application;

import com.hopeful117.cv_analyzer.career.domain.*;
import com.hopeful117.cv_analyzer.career.persistence.*;
import com.hopeful117.cv_analyzer.exception.EntityNotFoundException;
import com.hopeful117.cv_analyzer.exception.InvalidJobOfferException;
import com.hopeful117.cv_analyzer.helper.ResolveOffer;
import com.hopeful117.cv_analyzer.model.GeneratedResume;
import com.hopeful117.cv_analyzer.model.ResumeAnalysis;
import com.hopeful117.cv_analyzer.model.ResumePdfStyle;
import com.hopeful117.cv_analyzer.service.CoverLetterService;
import com.hopeful117.cv_analyzer.service.PdfParserService;
import com.hopeful117.cv_analyzer.service.ResumeAnalysisService;
import com.hopeful117.cv_analyzer.service.ResumeGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

import static com.hopeful117.cv_analyzer.career.application.CareerViewModels.*;

@Service
@RequiredArgsConstructor
public class CareerWorkspaceService {
    private final PdfParserService pdfParserService;
    private final ResolveOffer resolveOffer;
    private final ResumeAnalysisService resumeAnalysisService;
    private final ResumeGenerationService resumeGenerationService;
    private final CoverLetterService coverLetterService;
    private final UploadValidationService uploadValidationService;
    private final OpportunityRepository opportunityRepository;
    private final ResumeAnalysisRecordRepository analysisRepository;
    private final ResumeDocumentRepository resumeDocumentRepository;
    private final ResumeVersionRepository resumeVersionRepository;
    private final CoverLetterRepository coverLetterRepository;

    @Value("${spring.ai.openai.model:gpt-4o-mini}")
    private String aiModel;

    @Transactional
    public Long analyze(MultipartFile file, String jobOffer, String jobOfferUrl,
                        String title, String companyName) {
        uploadValidationService.requirePdf(file, "Le CV");
        String resumeText = extractPdf(file);
        String offerText = resolveOffer(jobOffer, jobOfferUrl);
        requireContent(resumeText, "Le texte extrait du CV");
        requireContent(offerText, "Le contenu de l’offre");
        ResumeAnalysis aiAnalysis = resumeAnalysisService.analyzeResume(resumeText, offerText);
        GeneratedResume generated = resumeGenerationService.generateCorrectedResume(
                resumeText, offerText, aiAnalysis);

        OpportunityEntity opportunity = new OpportunityEntity();
        opportunity.setTitle(cleanLimited(title, 200, "L’intitulé"));
        opportunity.setCompanyName(cleanLimited(companyName, 200, "L’entreprise"));
        opportunity.setSourceType(hasText(jobOfferUrl) ? OpportunitySourceType.URL : OpportunitySourceType.MANUAL);
        opportunity.setSourceUrl(cleanLimited(jobOfferUrl, 2048, "L’URL"));
        opportunity.setRawDescription(hasText(jobOffer) ? jobOffer.trim() : offerText);
        opportunity.setNormalizedDescription(offerText.trim());
        opportunity.setDetectedLanguage(clean(aiAnalysis.getJobOfferLanguage()));
        opportunity.setStatus(OpportunityStatus.ANALYZED);
        opportunityRepository.save(opportunity);

        Instant generatedAt = Instant.now();
        ResumeAnalysisRecordEntity analysis = new ResumeAnalysisRecordEntity();
        analysis.setOpportunity(opportunity);
        analysis.setOverallScore(aiAnalysis.getOverallScore());
        analysis.setQualityScore(aiAnalysis.getCvQualityScore());
        analysis.setAtsScore(aiAnalysis.getAtsScore());
        analysis.setMatchScore(aiAnalysis.getJobMatchScore());
        analysis.setJobOfferLanguage(clean(aiAnalysis.getJobOfferLanguage()));
        analysis.setAnalysisNature(AnalysisNature.AI_ESTIMATE);
        analysis.setRisks(copy(aiAnalysis.getAtsRisks()));
        analysis.setRecommendations(copy(aiAnalysis.getRecommendations()));
        analysis.setMissingKeywords(copy(aiAnalysis.getMissingKeywords()));
        analysis.setAiProvider("OpenAI");
        analysis.setAiModel(aiModel);
        analysis.setPromptVersion("resume-analysis-v1");
        analysis.setGenerationType(GenerationType.RESUME_ANALYSIS);
        analysis.setGeneratedAt(generatedAt);
        analysisRepository.save(analysis);

        ResumeDocumentEntity document = new ResumeDocumentEntity();
        document.setAnalysis(analysis);
        document.setStatus(DocumentStatus.ACTIVE);
        ResumeVersionEntity initialVersion = new ResumeVersionEntity();
        initialVersion.setVersionNumber(1);
        initialVersion.setCandidateName(cleanLimited(generated.getCandidateName(), 200, "Le nom généré"));
        initialVersion.setProfessionalTitle(cleanLimited(
                generated.getProfessionalTitle(), 200, "Le titre professionnel généré"));
        initialVersion.setContent(requireContent(generated.getContent(), "Le CV généré"));
        initialVersion.setLanguage(clean(aiAnalysis.getJobOfferLanguage()));
        initialVersion.setOrigin(ResumeVersionOrigin.AI_GENERATED);
        initialVersion.setPdfStyle(ResumePdfStyle.PROFESSIONAL);
        initialVersion.setPlaceholders(copy(generated.getPlaceholders()));
        initialVersion.setCorrections(copy(generated.getAppliedCorrections()));
        initialVersion.setAiProvider("OpenAI");
        initialVersion.setAiModel(aiModel);
        initialVersion.setPromptVersion("resume-generation-v1");
        initialVersion.setGenerationType(GenerationType.RESUME_GENERATION);
        initialVersion.setGeneratedAt(generatedAt);
        document.addVersion(initialVersion);
        resumeDocumentRepository.save(document);
        return analysis.getId();
    }

    @Transactional(readOnly = true)
    public AnalysisDetails getAnalysis(long id) {
        ResumeAnalysisRecordEntity entity = analysisRepository.findOneById(id)
                .orElseThrow(() -> new EntityNotFoundException("Analyse introuvable."));
        ResumeDocumentEntity document = resumeDocumentRepository.findByAnalysisId(id)
                .orElseThrow(() -> new EntityNotFoundException("CV associé introuvable."));
        List<ResumeVersionView> versions = document.getVersions().stream().map(this::toVersionView).toList();
        ResumeVersionView active = versions.getFirst();
        ResumeAnalysis analysis = new ResumeAnalysis(
                entity.getOverallScore(), entity.getQualityScore(), entity.getAtsScore(),
                entity.getMatchScore(), entity.getJobOfferLanguage(),
                List.copyOf(entity.getRisks()), List.copyOf(entity.getRecommendations()),
                List.copyOf(entity.getMissingKeywords()));
        GeneratedResume generated = new GeneratedResume(active.content(), active.candidateName(),
                active.professionalTitle(), active.placeholders(), active.corrections());
        return new AnalysisDetails(entity.getId(), toOpportunityView(entity.getOpportunity()), analysis,
                document.getId(), generated, active, versions, entity.getCreatedAt(),
                entity.getAiProvider(), entity.getAiModel(), entity.getPromptVersion());
    }

    @Transactional(readOnly = true)
    public Page<AnalysisListItem> getAnalyses(int page, int size) {
        return analysisRepository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50))).map(this::toAnalysisItem);
    }

    @Transactional
    public Long saveResumeVersion(long documentId, String content, String candidateName,
                                  String professionalTitle, ResumePdfStyle style) {
        ResumeDocumentEntity document = resumeDocumentRepository.findOneById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("CV introuvable."));
        String validContent = requireContent(content, "Le contenu du CV");
        int nextNumber = document.getVersions().stream()
                .mapToInt(ResumeVersionEntity::getVersionNumber).max().orElse(0) + 1;
        ResumeVersionEntity version = new ResumeVersionEntity();
        version.setVersionNumber(nextNumber);
        version.setCandidateName(cleanLimited(candidateName, 200, "Le nom"));
        version.setProfessionalTitle(cleanLimited(professionalTitle, 200, "Le titre professionnel"));
        version.setContent(validContent);
        version.setLanguage(document.getAnalysis().getJobOfferLanguage());
        version.setOrigin(ResumeVersionOrigin.USER_EDITED);
        version.setPdfStyle(style == null ? ResumePdfStyle.PROFESSIONAL : style);
        document.addVersion(version);
        resumeDocumentRepository.save(document);
        return document.getAnalysis().getId();
    }

    @Transactional(readOnly = true)
    public ResumeVersionView getResumeVersion(long versionId) {
        return resumeVersionRepository.findOneById(versionId)
                .map(this::toVersionView)
                .orElseThrow(() -> new EntityNotFoundException("Version de CV introuvable."));
    }

    @Transactional
    public Long generateCoverLetter(MultipartFile letterFile, MultipartFile cvFile,
                                    String jobOffer, String jobOfferUrl,
                                    String title, String companyName) {
        uploadValidationService.requirePdf(cvFile, "Le CV");
        uploadValidationService.validateOptionalPdf(letterFile, "La lettre existante");
        String offerText = resolveOffer(jobOffer, jobOfferUrl);
        String cvText = extractPdf(cvFile);
        String existingLetter = letterFile == null || letterFile.isEmpty() ? "" : extractPdf(letterFile);
        requireContent(cvText, "Le texte extrait du CV");
        requireContent(offerText, "Le contenu de l’offre");
        String content = coverLetterService.generateFromTexts(existingLetter, cvText, offerText);
        OpportunityEntity opportunity = new OpportunityEntity();
        opportunity.setTitle(cleanLimited(title, 200, "L’intitulé"));
        opportunity.setCompanyName(cleanLimited(companyName, 200, "L’entreprise"));
        opportunity.setSourceType(hasText(jobOfferUrl) ? OpportunitySourceType.URL : OpportunitySourceType.MANUAL);
        opportunity.setSourceUrl(cleanLimited(jobOfferUrl, 2048, "L’URL"));
        opportunity.setRawDescription(hasText(jobOffer) ? jobOffer.trim() : offerText);
        opportunity.setNormalizedDescription(offerText.trim());
        opportunity.setStatus(OpportunityStatus.DRAFT);
        opportunityRepository.save(opportunity);

        CoverLetterEntity letter = new CoverLetterEntity();
        letter.setOpportunity(opportunity);
        letter.setContent(requireContent(content, "La lettre générée"));
        letter.setStatus(DocumentStatus.ACTIVE);
        letter.setOrigin(letterFile == null || letterFile.isEmpty()
                ? CoverLetterOrigin.AI_GENERATED : CoverLetterOrigin.AI_IMPROVED);
        letter.setAiProvider("OpenAI");
        letter.setAiModel(aiModel);
        letter.setPromptVersion("cover-letter-v1");
        letter.setGenerationType(GenerationType.COVER_LETTER_GENERATION);
        letter.setGeneratedAt(Instant.now());
        return coverLetterRepository.save(letter).getId();
    }

    @Transactional(readOnly = true)
    public CoverLetterView getCoverLetter(long id) {
        return coverLetterRepository.findOneById(id).map(this::toCoverLetterView)
                .orElseThrow(() -> new EntityNotFoundException("Lettre introuvable."));
    }

    @Transactional
    public void updateCoverLetter(long id, String content) {
        CoverLetterEntity letter = coverLetterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lettre introuvable."));
        letter.setContent(requireContent(content, "Le contenu de la lettre"));
        letter.setOrigin(CoverLetterOrigin.USER_EDITED);
    }

    @Transactional(readOnly = true)
    public Dashboard getDashboard() {
        List<AnalysisListItem> analyses = analysisRepository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(0, 5)).map(this::toAnalysisItem).getContent();
        List<ResumeSummary> resumes = resumeDocumentRepository.findAllByOrderByUpdatedAtDesc(
                PageRequest.of(0, 5)).stream().map(this::toResumeSummary).toList();
        List<CoverLetterView> letters = coverLetterRepository.findAllByOrderByUpdatedAtDesc(
                PageRequest.of(0, 5)).stream().map(this::toCoverLetterView).toList();
        return new Dashboard(opportunityRepository.count(), analysisRepository.count(),
                resumeDocumentRepository.count(), coverLetterRepository.count(), analyses, resumes, letters);
    }

    @Transactional
    public void deleteAnalysis(long id) {
        ResumeAnalysisRecordEntity analysis = analysisRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Analyse introuvable."));
        resumeDocumentRepository.findByAnalysisId(id).ifPresent(resumeDocumentRepository::delete);
        analysisRepository.delete(analysis);
    }

    @Transactional
    public void deleteResume(long id) {
        ResumeDocumentEntity document = resumeDocumentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CV introuvable."));
        resumeDocumentRepository.delete(document);
    }

    @Transactional
    public void deleteCoverLetter(long id) {
        if (!coverLetterRepository.existsById(id)) {
            throw new EntityNotFoundException("Lettre introuvable.");
        }
        coverLetterRepository.deleteById(id);
    }

    @Transactional
    public void deleteOpportunity(long id) {
        OpportunityEntity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Opportunité introuvable."));
        if (analysisRepository.countByOpportunityId(id) > 0 || coverLetterRepository.countByOpportunityId(id) > 0) {
            throw new InvalidJobOfferException(
                    "Cette opportunité contient des analyses ou lettres. Supprimez-les explicitement d’abord.");
        }
        opportunityRepository.delete(opportunity);
    }

    private AnalysisListItem toAnalysisItem(ResumeAnalysisRecordEntity entity) {
        return new AnalysisListItem(entity.getId(), toOpportunityView(entity.getOpportunity()),
                entity.getCreatedAt(), entity.getOverallScore(), entity.getAtsScore(),
                entity.getMatchScore(), entity.getAnalysisNature());
    }

    private OpportunityView toOpportunityView(OpportunityEntity entity) {
        return new OpportunityView(entity.getId(), entity.getTitle(), entity.getCompanyName(),
                entity.getSourceUrl(), entity.getNormalizedDescription(), entity.getDetectedLanguage(),
                entity.getStatus(), entity.getCreatedAt());
    }

    private ResumeVersionView toVersionView(ResumeVersionEntity entity) {
        return new ResumeVersionView(entity.getId(), entity.getVersionNumber(), entity.getCandidateName(),
                entity.getProfessionalTitle(), entity.getContent(), entity.getLanguage(), entity.getOrigin(),
                entity.getPdfStyle(), entity.getCreatedAt(), List.copyOf(entity.getPlaceholders()),
                List.copyOf(entity.getCorrections()));
    }

    private ResumeSummary toResumeSummary(ResumeDocumentEntity entity) {
        ResumeVersionEntity latest = entity.getVersions().getFirst();
        return new ResumeSummary(entity.getId(), entity.getAnalysis().getId(),
                latest.getProfessionalTitle(), entity.getAnalysis().getOpportunity().getTitle(),
                latest.getVersionNumber(), entity.getUpdatedAt());
    }

    private CoverLetterView toCoverLetterView(CoverLetterEntity entity) {
        return new CoverLetterView(entity.getId(), toOpportunityView(entity.getOpportunity()),
                entity.getContent(), entity.getLanguage(), entity.getStatus(), entity.getOrigin(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String clean(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private static String cleanLimited(String value, int maxLength, String label) {
        String cleaned = clean(value);
        if (cleaned != null && cleaned.length() > maxLength) {
            throw new IllegalArgumentException(label + " dépasse " + maxLength + " caractères.");
        }
        return cleaned;
    }

    private static List<String> copy(List<String> values) {
        return values == null ? List.of() : values.stream().filter(CareerWorkspaceService::hasText)
                .map(String::trim).toList();
    }

    private static String requireContent(String value, String label) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(label + " ne peut pas être vide.");
        }
        if (value.length() > 100_000) {
            throw new IllegalArgumentException(label + " dépasse la taille autorisée.");
        }
        return value.trim();
    }

    private String extractPdf(MultipartFile file) {
        try {
            return pdfParserService.extractText(file);
        } catch (java.io.IOException exception) {
            throw new com.hopeful117.cv_analyzer.exception.PdfParserException(
                    "Impossible de lire le document PDF.", exception);
        }
    }

    private String resolveOffer(String text, String url) {
        try {
            return resolveOffer.resolveOffer(text, url);
        } catch (java.io.IOException exception) {
            throw new com.hopeful117.cv_analyzer.exception.JobScrapperException(
                    "Impossible de récupérer l’offre depuis cette URL.", exception);
        }
    }
}
