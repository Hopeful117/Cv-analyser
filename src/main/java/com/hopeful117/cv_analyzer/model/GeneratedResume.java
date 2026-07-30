package com.hopeful117.cv_analyzer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GeneratedResume {
    private String content;
    private List<String> placeholders;
    private List<String> appliedCorrections;
}
