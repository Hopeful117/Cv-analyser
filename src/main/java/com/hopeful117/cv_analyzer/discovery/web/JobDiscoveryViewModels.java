package com.hopeful117.cv_analyzer.discovery.web;

import com.hopeful117.cv_analyzer.discovery.application.DiscoverJobOffers;
import com.hopeful117.cv_analyzer.discovery.domain.EligibilityResult;
import com.hopeful117.cv_analyzer.discovery.domain.EligibilityStatus;
import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;
import com.hopeful117.cv_analyzer.search.persistence.PreferenceRoleEntity;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class JobDiscoveryViewModels {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JobDiscoveryViewModels() {
    }

    public static SearchForm createSearchForm(List<PreferenceRoleEntity> roles) {
        List<RoleOption> roleOptions = roles.stream()
                .map(r -> new RoleOption(r.getLabel(), r.getLabel()))
                .toList();
        return new SearchForm(roleOptions, null);
    }

    public static ResultsViewModel toResults(DiscoverJobOffers.DiscoveryResult result) {
        List<OfferViewModel> offers = result.offers().stream()
                .map(e -> toOfferViewModel(e.offer(), e.eligibility()))
                .toList();

        return new ResultsViewModel(
                result.returnedCount(),
                result.totalAvailable(),
                result.targetRole(),
                offers
        );
    }

    private static OfferViewModel toOfferViewModel(JobOffer offer, EligibilityResult eligibility) {
        return new OfferViewModel(
                offer.title(),
                offer.company(),
                offer.locationLabel(),
                offer.rawContractLabel(),
                offer.rawSalaryText(),
                offer.workDurationLabel(),
                eligibility.status().name(),
                eligibility.status().getLabel(),
                statusCssClass(eligibility.status()),
                eligibility.reasons().stream().map(r -> r.message()).toList(),
                offer.originUrl(),
                sanitizeUrl(offer.originUrl()),
                offer.providerOfferId(),
                offer.providerCreatedAt() != null ? DATE_FORMAT.format(offer.providerCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate()) : null,
                offer.description() != null ? stripHtml(truncate(offer.description(), 500)) : null
        );
    }

    private static String statusCssClass(EligibilityStatus status) {
        return switch (status) {
            case ELIGIBLE -> "os-badge-success";
            case INELIGIBLE -> "os-badge-error";
            case REVIEW_REQUIRED -> "os-badge-warning";
        };
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) return null;
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }

    private static String stripHtml(String html) {
        if (html == null) return null;
        return html
                .replaceAll("<[^>]+>", " ")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"")
                .replaceAll("&#39;", "'")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String sanitizeUrl(String url) {
        if (url == null) return null;
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if (scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                return url;
            }
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public record SearchForm(
            List<RoleOption> roles,
            String selectedRole
    ) {
    }

    public record RoleOption(
            String value,
            String label
    ) {
    }

    public record ResultsViewModel(
            int returnedCount,
            int totalAvailable,
            String targetRole,
            List<OfferViewModel> offers
    ) {
    }

    public record OfferViewModel(
            String title,
            String company,
            String location,
            String contract,
            String salary,
            String workDuration,
            String status,
            String statusLabel,
            String statusCssClass,
            List<String> reasons,
            String originUrl,
            String safeUrl,
            String providerOfferId,
            String createdAt,
            String descriptionSnippet
    ) {
    }
}
