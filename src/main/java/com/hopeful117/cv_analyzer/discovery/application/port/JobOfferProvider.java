package com.hopeful117.cv_analyzer.discovery.application.port;

public interface JobOfferProvider {
    JobOfferSearchResult search(JobOfferSearchRequest request);

    boolean isAvailable();
}
