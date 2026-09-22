package com.hopeful117.cv_analyzer.WebInterfaceController;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class Es004TemplateContractTest {
    @Test
    void opportunityWorkspaceExposesPreparationAndApplicationActions() throws IOException {
        String template = Files.readString(Path.of("src/main/resources/templates/opportunity-workspace.html"));

        assertThat(template).contains("@{/analyze(opportunityTitle=");
        assertThat(template).contains("@{/generator(opportunityTitle=");
        assertThat(template).contains("@{/applications/new(opportunityId=");
        assertThat(template).contains("workspace.analyses");
        assertThat(template).contains("workspace.resumes");
        assertThat(template).contains("workspace.letters");
        assertThat(template).contains("workspace.applications");
    }

    @Test
    void opportunityListLinksToWorkspaceAndApplicationDetailLinksBack() throws IOException {
        String list = Files.readString(Path.of("src/main/resources/templates/opportunities.html"));
        String detail = Files.readString(Path.of("src/main/resources/templates/application-detail.html"));

        assertThat(list).contains("@{/opportunities/{id}(id=${item.id})}");
        assertThat(detail).contains("@{/opportunities/{id}(id=${applicationDetails.opportunityId})}");
    }
}
