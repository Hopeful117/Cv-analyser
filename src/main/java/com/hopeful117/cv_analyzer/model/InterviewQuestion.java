package com.hopeful117.cv_analyzer.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "interview_question")
@Getter
@Setter
@NoArgsConstructor
public class InterviewQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    private int questionOrder;
    private String category;
    @Lob
    private String question;
    private String expectedSkill;

        public InterviewQuestion(InterviewSession session, int order, String category, String question, String expectedSkill) {
            this.session=session;
            this.questionOrder = order;
            this.category=category;
            this.question=question;
            this.expectedSkill=expectedSkill;
    }
}
