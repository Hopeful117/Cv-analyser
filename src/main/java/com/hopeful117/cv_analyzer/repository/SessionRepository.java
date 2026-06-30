package com.hopeful117.cv_analyzer.repository;



import com.hopeful117.cv_analyzer.model.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<InterviewSession,Long> {
    Optional<InterviewSession> findBySessionId(UUID sessionId);
}
