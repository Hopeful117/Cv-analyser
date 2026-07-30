package com.hopeful117.cv_analyzer.career.persistence;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.time.Instant;

@Entity
@Table(name = "career_company")
@Getter
@Setter
@NoArgsConstructor
public class CompanyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String name;
    @Size(max = 160)
    @Column(length = 160)
    private String city;
    @Size(max = 500)
    @Column(length = 500)
    private String address;
    @Size(max = 80)
    @Column(length = 80)
    private String phone;
    @Email
    @Size(max = 254)
    @Column(length = 254)
    private String email;
    @URL
    @Size(max = 2048)
    @Column(length = 2048)
    private String website;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String notes;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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
}
