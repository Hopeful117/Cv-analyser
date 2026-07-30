package com.hopeful117.cv_analyzer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedResume {
    private String content;
    private String candidateName;
    private String professionalTitle;
    private List<String> placeholders;
    private List<String> appliedCorrections;

    public GeneratedResume(
            String content,
            List<String> placeholders,
            List<String> appliedCorrections
    ) {
        this(content, null, null, placeholders, appliedCorrections);
    }
}
