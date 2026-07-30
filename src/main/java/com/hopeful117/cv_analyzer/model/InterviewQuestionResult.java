package com.hopeful117.cv_analyzer.model;

import com.hopeful117.cv_analyzer.dto.InterviewQuestionFeedback;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "interview_question_result")
@Getter
@Setter
@NoArgsConstructor
public class InterviewQuestionResult {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long resultId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "session_id", nullable = false)
        private InterviewSession session;

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "question_id", nullable = false)
        private InterviewQuestion question;

        @Lob
        private String userAnswer;

        @Transient
        private InterviewQuestionFeedback feedback;

        public InterviewQuestionResult(InterviewSession session, InterviewQuestion question, String answer, InterviewQuestionFeedback feedback) {
            this.session = session;
            this.question = question;
            this.userAnswer = answer;
            this.feedback = feedback;
        }
}

