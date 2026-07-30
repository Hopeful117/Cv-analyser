package com.hopeful117.cv_analyzer.career;

import com.hopeful117.cv_analyzer.career.application.ApplicationCrmService;
import com.hopeful117.cv_analyzer.career.domain.*;
import com.hopeful117.cv_analyzer.career.persistence.ApplicationStatusHistoryRepository;
import com.hopeful117.cv_analyzer.career.web.ApplicationForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplicationCrmIntegrationTest {
    @Autowired ApplicationCrmService service;
    @Autowired ApplicationStatusHistoryRepository historyRepository;

    @Test
    void createsApplicationAndOnlyHistoriesRealStatusChanges() {
        ApplicationForm form = new ApplicationForm();
        form.setCompanyName("Entreprise test CRM");
        form.setJobTitle("Développeur Java");
        form.setStatus(ApplicationStatus.NOT_CONTACTED);
        form.setPriority(ApplicationPriority.HIGH);
        form.setRemoteMode(RemoteMode.HYBRID);
        form.setInterviewStatus(InterviewStatus.NONE);
        form.setDecision(ApplicationDecision.PENDING);

        long id = service.create(form, ChangeSource.USER);
        assertThat(service.getDetails(id).companyName()).isEqualTo("Entreprise test CRM");
        assertThat(historyRepository.findByApplicationIdOrderByChangedAtDesc(id)).hasSize(1);

        service.changeStatus(id, ApplicationStatus.NOT_CONTACTED, "Sans changement");
        assertThat(historyRepository.findByApplicationIdOrderByChangedAtDesc(id)).hasSize(1);

        service.changeStatus(id, ApplicationStatus.APPLIED, "CV envoyé");
        assertThat(historyRepository.findByApplicationIdOrderByChangedAtDesc(id))
                .hasSize(2).first().extracting(event -> event.getNewStatus())
                .isEqualTo(ApplicationStatus.APPLIED);
    }
}
