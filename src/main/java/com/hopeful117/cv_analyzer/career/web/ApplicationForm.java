package com.hopeful117.cv_analyzer.career.web;

import com.hopeful117.cv_analyzer.career.domain.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class ApplicationForm {
    @NotBlank @Size(max = 200)
    private String companyName;
    @Size(max = 160)
    private String city;
    @Size(max = 500)
    private String address;
    @Size(max = 80)
    private String phone;
    @Email @Size(max = 254)
    private String email;
    @URL @Size(max = 2048)
    private String website;
    @NotBlank @Size(max = 200)
    private String jobTitle;
    @URL @Size(max = 2048)
    private String offerUrl;
    private ContractType contractType;
    @Size(max = 120)
    private String contractTypeRaw;
    private WorkSchedule workSchedule;
    @Size(max = 120)
    private String workScheduleRaw;
    private RemoteMode remoteMode = RemoteMode.UNSPECIFIED;
    @Size(max = 200)
    private String source;
    @Size(max = 200)
    private String salaryText;
    @Size(max = 120)
    private String distanceText;
    @Size(max = 300)
    private String location;
    @Size(max = 100_000)
    private String description;
    private ApplicationStatus status = ApplicationStatus.NOT_CONTACTED;
    private ApplicationPriority priority = ApplicationPriority.MEDIUM;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate appliedAt;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate followUpPlannedAt;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate lastFollowUpAt;
    private InterviewStatus interviewStatus = InterviewStatus.NONE;
    private ApplicationDecision decision = ApplicationDecision.PENDING;
    private boolean portfolioSent;
    @Size(max = 20_000)
    private String notes;
    @Size(max = 20_000)
    private String privateNotes;
    private Long resumeVersionId;
    private Long coverLetterId;
    private Long analysisId;
}
