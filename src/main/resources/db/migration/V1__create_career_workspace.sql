CREATE TABLE career_opportunity (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200),
    company_name VARCHAR(200),
    source_type VARCHAR(20) NOT NULL,
    source_url VARCHAR(2048),
    raw_description LONGTEXT NOT NULL,
    normalized_description LONGTEXT NOT NULL,
    detected_language VARCHAR(16),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE career_resume_analysis (
    id BIGINT NOT NULL AUTO_INCREMENT,
    opportunity_id BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    overall_score INT NOT NULL,
    quality_score INT NOT NULL,
    ats_score INT NOT NULL,
    match_score INT NOT NULL,
    job_offer_language VARCHAR(16),
    analysis_nature VARCHAR(24) NOT NULL,
    ai_provider VARCHAR(80),
    ai_model VARCHAR(120),
    prompt_version VARCHAR(80),
    generation_type VARCHAR(40),
    generated_at TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_analysis_opportunity FOREIGN KEY (opportunity_id) REFERENCES career_opportunity (id)
);

CREATE TABLE career_analysis_risk (
    analysis_id BIGINT NOT NULL,
    item_order INT NOT NULL,
    item_value VARCHAR(1000) NOT NULL,
    PRIMARY KEY (analysis_id, item_order),
    CONSTRAINT fk_risk_analysis FOREIGN KEY (analysis_id) REFERENCES career_resume_analysis (id) ON DELETE CASCADE
);

CREATE TABLE career_analysis_recommendation (
    analysis_id BIGINT NOT NULL,
    item_order INT NOT NULL,
    item_value VARCHAR(1000) NOT NULL,
    PRIMARY KEY (analysis_id, item_order),
    CONSTRAINT fk_recommendation_analysis FOREIGN KEY (analysis_id) REFERENCES career_resume_analysis (id) ON DELETE CASCADE
);

CREATE TABLE career_analysis_missing_keyword (
    analysis_id BIGINT NOT NULL,
    item_order INT NOT NULL,
    item_value VARCHAR(255) NOT NULL,
    PRIMARY KEY (analysis_id, item_order),
    CONSTRAINT fk_keyword_analysis FOREIGN KEY (analysis_id) REFERENCES career_resume_analysis (id) ON DELETE CASCADE
);

CREATE TABLE career_resume_document (
    id BIGINT NOT NULL AUTO_INCREMENT,
    analysis_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_resume_document_analysis UNIQUE (analysis_id),
    CONSTRAINT fk_document_analysis FOREIGN KEY (analysis_id) REFERENCES career_resume_analysis (id)
);

CREATE TABLE career_resume_version (
    id BIGINT NOT NULL AUTO_INCREMENT,
    document_id BIGINT NOT NULL,
    version_number INT NOT NULL,
    candidate_name VARCHAR(200),
    professional_title VARCHAR(200),
    content LONGTEXT NOT NULL,
    language VARCHAR(16),
    origin VARCHAR(24) NOT NULL,
    pdf_style VARCHAR(30) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    ai_provider VARCHAR(80),
    ai_model VARCHAR(120),
    prompt_version VARCHAR(80),
    generation_type VARCHAR(40),
    generated_at TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_resume_version_number UNIQUE (document_id, version_number),
    CONSTRAINT fk_version_document FOREIGN KEY (document_id) REFERENCES career_resume_document (id) ON DELETE CASCADE
);

CREATE TABLE career_resume_placeholder (
    version_id BIGINT NOT NULL,
    item_order INT NOT NULL,
    item_value VARCHAR(1000) NOT NULL,
    PRIMARY KEY (version_id, item_order),
    CONSTRAINT fk_placeholder_version FOREIGN KEY (version_id) REFERENCES career_resume_version (id) ON DELETE CASCADE
);

CREATE TABLE career_resume_correction (
    version_id BIGINT NOT NULL,
    item_order INT NOT NULL,
    item_value VARCHAR(1000) NOT NULL,
    PRIMARY KEY (version_id, item_order),
    CONSTRAINT fk_correction_version FOREIGN KEY (version_id) REFERENCES career_resume_version (id) ON DELETE CASCADE
);

CREATE TABLE career_cover_letter (
    id BIGINT NOT NULL AUTO_INCREMENT,
    opportunity_id BIGINT NOT NULL,
    analysis_id BIGINT,
    resume_version_id BIGINT,
    content LONGTEXT NOT NULL,
    language VARCHAR(16),
    status VARCHAR(20) NOT NULL,
    origin VARCHAR(24) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    ai_provider VARCHAR(80),
    ai_model VARCHAR(120),
    prompt_version VARCHAR(80),
    generation_type VARCHAR(40),
    generated_at TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_letter_opportunity FOREIGN KEY (opportunity_id) REFERENCES career_opportunity (id),
    CONSTRAINT fk_letter_analysis FOREIGN KEY (analysis_id) REFERENCES career_resume_analysis (id) ON DELETE SET NULL,
    CONSTRAINT fk_letter_version FOREIGN KEY (resume_version_id) REFERENCES career_resume_version (id) ON DELETE SET NULL
);

CREATE INDEX idx_analysis_created ON career_resume_analysis (created_at);
CREATE INDEX idx_analysis_opportunity ON career_resume_analysis (opportunity_id);
CREATE INDEX idx_resume_updated ON career_resume_document (updated_at);
CREATE INDEX idx_letter_updated ON career_cover_letter (updated_at);

-- Existing experimental interview persistence, now managed non-destructively by Flyway.
CREATE TABLE interview_session (
    session_id BINARY(16) NOT NULL,
    cv LONGTEXT,
    job_description LONGTEXT,
    current_index INT NOT NULL,
    PRIMARY KEY (session_id)
);

CREATE TABLE interview_question (
    question_id BIGINT NOT NULL AUTO_INCREMENT,
    session_id BINARY(16) NOT NULL,
    question_order INT NOT NULL,
    category VARCHAR(255),
    question LONGTEXT,
    expected_skill VARCHAR(255),
    PRIMARY KEY (question_id),
    CONSTRAINT fk_question_session FOREIGN KEY (session_id) REFERENCES interview_session (session_id) ON DELETE CASCADE
);

CREATE TABLE interview_question_result (
    result_id BIGINT NOT NULL AUTO_INCREMENT,
    session_id BINARY(16) NOT NULL,
    question_id BIGINT NOT NULL,
    user_answer LONGTEXT,
    PRIMARY KEY (result_id),
    CONSTRAINT uk_result_question UNIQUE (question_id),
    CONSTRAINT fk_result_session FOREIGN KEY (session_id) REFERENCES interview_session (session_id) ON DELETE CASCADE,
    CONSTRAINT fk_result_question FOREIGN KEY (question_id) REFERENCES interview_question (question_id)
);
