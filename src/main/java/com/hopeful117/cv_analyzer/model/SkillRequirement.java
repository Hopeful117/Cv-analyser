package com.hopeful117.cv_analyzer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SkillRequirement {
    private String name;
    int weight;
    boolean mandatory;


}
