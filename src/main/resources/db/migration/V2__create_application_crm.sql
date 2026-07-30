CREATE TABLE career_company (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    city VARCHAR(160),
    address VARCHAR(500),
    phone VARCHAR(80),
    email VARCHAR(254),
    website VARCHAR(2048),
    notes LONGTEXT,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_company_name ON career_company (name);

ALTER TABLE career_opportunity ADD COLUMN company_id BIGINT;
ALTER TABLE career_opportunity ADD COLUMN contract_type VARCHAR(24);
ALTER TABLE career_opportunity ADD COLUMN contract_type_raw VARCHAR(120);
ALTER TABLE career_opportunity ADD COLUMN work_schedule VARCHAR(24);
ALTER TABLE career_opportunity ADD COLUMN work_schedule_raw VARCHAR(120);
ALTER TABLE career_opportunity ADD COLUMN remote_mode VARCHAR(24);
ALTER TABLE career_opportunity ADD COLUMN source VARCHAR(200);
ALTER TABLE career_opportunity ADD COLUMN salary_text VARCHAR(200);
ALTER TABLE career_opportunity ADD COLUMN distance_text VARCHAR(120);
ALTER TABLE career_opportunity ADD COLUMN location VARCHAR(300);

INSERT INTO career_company (name, created_at, updated_at)
SELECT DISTINCT company_name, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
FROM career_opportunity
WHERE company_name IS NOT NULL AND TRIM(company_name) <> '';

UPDATE career_opportunity o
SET company_id = (
    SELECT MIN(c.id) FROM career_company c WHERE c.name = o.company_name
)
WHERE o.company_name IS NOT NULL AND TRIM(o.company_name) <> '';

ALTER TABLE career_opportunity
    ADD CONSTRAINT fk_opportunity_company FOREIGN KEY (company_id) REFERENCES career_company (id);
CREATE INDEX idx_opportunity_company ON career_opportunity (company_id);

CREATE TABLE career_application (
    id BIGINT NOT NULL AUTO_INCREMENT,
    opportunity_id BIGINT NOT NULL,
    status VARCHAR(24) NOT NULL,
    priority VARCHAR(16) NOT NULL,
    applied_at DATE,
    follow_up_planned_at DATE,
    last_follow_up_at DATE,
    interview_status VARCHAR(20) NOT NULL,
    decision VARCHAR(20) NOT NULL,
    portfolio_sent BOOLEAN NOT NULL,
    notes LONGTEXT,
    private_notes LONGTEXT,
    resume_version_id BIGINT,
    cover_letter_id BIGINT,
    analysis_id BIGINT,
    legacy_external_id VARCHAR(120),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_application_opportunity FOREIGN KEY (opportunity_id) REFERENCES career_opportunity (id),
    CONSTRAINT fk_application_resume_version FOREIGN KEY (resume_version_id) REFERENCES career_resume_version (id) ON DELETE SET NULL,
    CONSTRAINT fk_application_cover_letter FOREIGN KEY (cover_letter_id) REFERENCES career_cover_letter (id) ON DELETE SET NULL,
    CONSTRAINT fk_application_analysis FOREIGN KEY (analysis_id) REFERENCES career_resume_analysis (id) ON DELETE SET NULL
);

CREATE INDEX idx_application_status ON career_application (status);
CREATE INDEX idx_application_priority ON career_application (priority);
CREATE INDEX idx_application_applied_at ON career_application (applied_at);
CREATE INDEX idx_application_follow_up ON career_application (follow_up_planned_at);
CREATE INDEX idx_application_opportunity ON career_application (opportunity_id);

CREATE TABLE career_application_status_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    application_id BIGINT NOT NULL,
    previous_status VARCHAR(24),
    new_status VARCHAR(24) NOT NULL,
    changed_at TIMESTAMP(6) NOT NULL,
    change_source VARCHAR(16) NOT NULL,
    comment VARCHAR(500),
    PRIMARY KEY (id),
    CONSTRAINT fk_status_history_application FOREIGN KEY (application_id)
        REFERENCES career_application (id) ON DELETE CASCADE
);

CREATE INDEX idx_status_history_application
    ON career_application_status_history (application_id, changed_at);

CREATE TABLE career_external_projection (
    id BIGINT NOT NULL AUTO_INCREMENT,
    resource_type VARCHAR(40) NOT NULL,
    resource_id BIGINT NOT NULL,
    spreadsheet_id VARCHAR(200) NOT NULL,
    sheet_name VARCHAR(200) NOT NULL,
    external_id VARCHAR(120) NOT NULL,
    status VARCHAR(20) NOT NULL,
    last_attempt_at TIMESTAMP(6),
    last_successful_sync_at TIMESTAMP(6),
    last_error_code VARCHAR(80),
    last_error_message VARCHAR(1000),
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_external_projection_resource UNIQUE
        (resource_type, resource_id, spreadsheet_id, sheet_name)
);

CREATE INDEX idx_projection_resource ON career_external_projection (resource_id);
CREATE INDEX idx_projection_external_id ON career_external_projection (external_id);
CREATE INDEX idx_projection_status ON career_external_projection (status);
