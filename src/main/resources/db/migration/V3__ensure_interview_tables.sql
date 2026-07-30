-- Some legacy installations were baselined after the experimental interview tables
-- had been removed or only partially created. V1 can therefore be recorded as applied
-- while Hibernate still expects these tables. Keep this repair additive and idempotent.
CREATE TABLE IF NOT EXISTS interview_session (
    session_id BINARY(16) NOT NULL,
    cv LONGTEXT,
    job_description LONGTEXT,
    current_index INT NOT NULL,
    PRIMARY KEY (session_id)
);

CREATE TABLE IF NOT EXISTS interview_question (
    question_id BIGINT NOT NULL AUTO_INCREMENT,
    session_id BINARY(16) NOT NULL,
    question_order INT NOT NULL,
    category VARCHAR(255),
    question LONGTEXT,
    expected_skill VARCHAR(255),
    PRIMARY KEY (question_id),
    CONSTRAINT fk_question_session FOREIGN KEY (session_id)
        REFERENCES interview_session (session_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS interview_question_result (
    result_id BIGINT NOT NULL AUTO_INCREMENT,
    session_id BINARY(16) NOT NULL,
    question_id BIGINT NOT NULL,
    user_answer LONGTEXT,
    PRIMARY KEY (result_id),
    CONSTRAINT uk_result_question UNIQUE (question_id),
    CONSTRAINT fk_result_session FOREIGN KEY (session_id)
        REFERENCES interview_session (session_id) ON DELETE CASCADE,
    CONSTRAINT fk_result_question FOREIGN KEY (question_id)
        REFERENCES interview_question (question_id)
);
