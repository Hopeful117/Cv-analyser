-- Profil professionnel persistant (Story S1).
CREATE TABLE career_professional_profile (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(200),
    professional_title VARCHAR(200),
    reference_location VARCHAR(300),
    ai_provider VARCHAR(80),
    ai_model VARCHAR(120),
    prompt_version VARCHAR(80),
    cv_assisted_at TIMESTAMP(6),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE career_profile_skill (
    id BIGINT NOT NULL AUTO_INCREMENT,
    profile_id BIGINT NOT NULL,
    item_order INT NOT NULL,
    label VARCHAR(120) NOT NULL,
    normalized_name VARCHAR(120) NOT NULL,
    origin VARCHAR(16) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_profile_skill_name UNIQUE (profile_id, normalized_name),
    CONSTRAINT fk_skill_profile FOREIGN KEY (profile_id) REFERENCES career_professional_profile (id) ON DELETE CASCADE
);

CREATE TABLE career_profile_experience (
    id BIGINT NOT NULL AUTO_INCREMENT,
    profile_id BIGINT NOT NULL,
    item_order INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    company VARCHAR(200),
    start_date DATE,
    end_date DATE,
    description VARCHAR(2000),
    PRIMARY KEY (id),
    CONSTRAINT fk_experience_profile FOREIGN KEY (profile_id) REFERENCES career_professional_profile (id) ON DELETE CASCADE
);

CREATE TABLE career_profile_education (
    id BIGINT NOT NULL AUTO_INCREMENT,
    profile_id BIGINT NOT NULL,
    item_order INT NOT NULL,
    kind VARCHAR(20) NOT NULL,
    label VARCHAR(255) NOT NULL,
    institution VARCHAR(200),
    obtained_on DATE,
    PRIMARY KEY (id),
    CONSTRAINT fk_education_profile FOREIGN KEY (profile_id) REFERENCES career_professional_profile (id) ON DELETE CASCADE
);

CREATE TABLE career_profile_language (
    id BIGINT NOT NULL AUTO_INCREMENT,
    profile_id BIGINT NOT NULL,
    item_order INT NOT NULL,
    language VARCHAR(60) NOT NULL,
    normalized_language VARCHAR(60) NOT NULL,
    level VARCHAR(40),
    PRIMARY KEY (id),
    CONSTRAINT uk_profile_language_name UNIQUE (profile_id, normalized_language),
    CONSTRAINT fk_language_profile FOREIGN KEY (profile_id) REFERENCES career_professional_profile (id) ON DELETE CASCADE
);

CREATE INDEX idx_profile_updated ON career_professional_profile (updated_at);
