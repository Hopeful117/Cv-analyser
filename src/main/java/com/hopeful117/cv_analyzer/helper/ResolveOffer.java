package com.hopeful117.cv_analyzer.helper;

import com.hopeful117.cv_analyzer.exception.InvalidJobOfferException;
import com.hopeful117.cv_analyzer.service.JobScrapperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ResolveOffer {
    private final JobScrapperService jobScrapperService;
    public String resolveOffer(
            String jobOffer,
            String jobOfferUrl
    ) throws IOException {

        ;
        boolean hasText =
                jobOffer != null
                        && !jobOffer.isBlank();

        boolean hasUrl =
                jobOfferUrl != null
                        && !jobOfferUrl.isBlank();

        if (hasText && hasUrl) {
            throw new InvalidJobOfferException(
                    "Provide either a job offer text or a URL, not both."
            );
        }

        if (!hasText && !hasUrl) {
            throw new InvalidJobOfferException(
                    "A job offer text or URL is required."
            );
        }

        if (hasUrl) {
            return jobScrapperService
                    .extractTextFromUrl(jobOfferUrl);
        }

        return jobOffer;
    }
}
