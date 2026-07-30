package com.hopeful117.cv_analyzer.career.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ApplicationProjectionListener {
    private final ApplicationProjectionService projectionService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void project(ApplicationChangedEvent event) {
        projectionService.synchronize(event.applicationId());
    }
}
