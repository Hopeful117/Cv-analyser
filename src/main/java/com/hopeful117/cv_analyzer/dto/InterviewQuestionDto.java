package com.hopeful117.cv_analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterviewQuestionDto {


        private int order;
        private String category;
        private String question;
        private String expectedSkill;

}
