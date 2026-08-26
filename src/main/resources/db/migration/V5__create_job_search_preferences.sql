-- Préférences de recherche d'emploi actives (Story S2).
CREATE TABLE career_job_search_preferences (
    id BIGINT NOT NULL AUTO_INCREMENT,
    open_to_relocation BOOLEAN NOT NULL,
    salary_min_amount INT,
    salary_currency VARCHAR(3),
    salary_period VARCHAR(10),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE career_preference_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    preferences_id BIGINT NOT NULL,
    item_order INT NOT NULL,
    label VARCHAR(200) NOT NULL,
    normalized_label VARCHAR(200) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_preference_role UNIQUE (preferences_id, normalized_label),
    CONSTRAINT fk_role_preferences FOREIGN KEY (preferences_id) REFERENCES career_job_search_preferences (id) ON DELETE CASCADE
);

CREATE TABLE career_preference_location (
    id BIGINT NOT NULL AUTO_INCREMENT,
    preferences_id BIGINT NOT NULL,
    item_order INT NOT NULL,
    label VARCHAR(300) NOT NULL,
    normalized_label VARCHAR(300) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_preference_location UNIQUE (preferences_id, normalized_label),
    CONSTRAINT fk_location_preferences FOREIGN KEY (preferences_id) REFERENCES career_job_search_preferences (id) ON DELETE CASCADE
);

CREATE TABLE career_preference_technology (
    id BIGINT NOT NULL AUTO_INCREMENT,
    preferences_id BIGINT NOT NULL,
    item_order INT NOT NULL,
    preference_kind VARCHAR(16) NOT NULL,
    label VARCHAR(120) NOT NULL,
    normalized_name VARCHAR(120) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_preference_technology UNIQUE (preferences_id, preference_kind, normalized_name),
    CONSTRAINT fk_technology_preferences FOREIGN KEY (preferences_id) REFERENCES career_job_search_preferences (id) ON DELETE CASCADE
);

CREATE TABLE career_preference_work_mode (
    preferences_id BIGINT NOT NULL,
    mode VARCHAR(16) NOT NULL,
    PRIMARY KEY (preferences_id, mode),
    CONSTRAINT fk_work_mode_preferences FOREIGN KEY (preferences_id) REFERENCES career_job_search_preferences (id) ON DELETE CASCADE
);

CREATE TABLE career_preference_contract_type (
    preferences_id BIGINT NOT NULL,
    contract_type VARCHAR(24) NOT NULL,
    PRIMARY KEY (preferences_id, contract_type),
    CONSTRAINT fk_contract_type_preferences FOREIGN KEY (preferences_id) REFERENCES career_job_search_preferences (id) ON DELETE CASCADE
);
