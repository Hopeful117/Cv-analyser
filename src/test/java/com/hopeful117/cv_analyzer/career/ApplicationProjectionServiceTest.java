package com.hopeful117.cv_analyzer.career;

import com.hopeful117.cv_analyzer.career.application.*;
import com.hopeful117.cv_analyzer.career.application.port.*;
import com.hopeful117.cv_analyzer.career.domain.ProjectionStatus;
import com.hopeful117.cv_analyzer.career.infrastructure.google.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.*;

class ApplicationProjectionServiceTest {
    @Test
    void googleFailureDoesNotEscapeAndIsRecorded() {
        GoogleSheetsProjectionPort port = mock(GoogleSheetsProjectionPort.class);
        ProjectionStateService state = mock(ProjectionStateService.class);
        ApplicationProjectionQueryService query = mock(ApplicationProjectionQueryService.class);
        CareerGoogleSheetsProperties properties = new CareerGoogleSheetsProperties(
                true, "sheet-id", "Candidatures", "Tableau de bord", 1);
        ApplicationSheetProjection projection = projection();
        when(query.get(eq(42L), any(Instant.class), eq(ProjectionStatus.SYNCHRONIZED)))
                .thenReturn(projection);
        when(port.upsert(projection)).thenThrow(new GoogleSheetsFunctionalException(
                "QUOTA_EXCEEDED", "Quota Google dépassé."));

        new ApplicationProjectionService(port, properties, state, query).synchronize(42L);

        verify(state).markPending(42L);
        verify(state).markFailure(42L, "QUOTA_EXCEEDED", "Quota Google dépassé.");
        verify(state, never()).markSuccess(anyLong());
    }

    @Test
    void importedLegacyApplicationUpdatesItsOriginalRow() {
        GoogleSheetsProjectionPort port = mock(GoogleSheetsProjectionPort.class);
        ProjectionStateService state = mock(ProjectionStateService.class);
        ApplicationProjectionQueryService query = mock(ApplicationProjectionQueryService.class);
        CareerGoogleSheetsProperties properties = new CareerGoogleSheetsProperties(
                true, "sheet-id", "Candidatures", "Tableau de bord", 1);
        ApplicationSheetProjection projection = projection();
        when(query.get(eq(42L), any(Instant.class), eq(ProjectionStatus.SYNCHRONIZED)))
                .thenReturn(projection);
        when(query.getLegacySheetRow(42L)).thenReturn(3);

        new ApplicationProjectionService(port, properties, state, query).synchronize(42L);

        verify(port).updateLegacyRow(3, projection);
        verify(port, never()).upsert(any());
        verify(state).markSuccess(42L);
    }

    private ApplicationSheetProjection projection() {
        return new ApplicationSheetProjection("42", "APPLICATION-42", "Test", null, null,
                null, null, null, "Java", null, null, null, null, null,
                false, false, false, null, null, null, "Non démarché",
                "NONE", "PENDING", null, null, "Faible", null, null, null,
                Instant.now(), "SYNCHRONIZED");
    }
}
