package com.hopeful117.cv_analyzer.discovery.domain;

/**
 * Stable identity within the namespace of the acquisition source.
 * This is not a persisted identity and does not perform cross-source deduplication.
 */
public record JobOfferIdentity(
        String acquisitionSource,
        String providerOfferId
) {
    public static JobOfferIdentity from(JobOffer offer) {
        if (offer == null) return null;
        return new JobOfferIdentity(offer.providerKey(), offer.providerOfferId());
    }
}
