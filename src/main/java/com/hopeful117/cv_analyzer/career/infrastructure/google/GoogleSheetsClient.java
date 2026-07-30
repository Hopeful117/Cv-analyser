package com.hopeful117.cv_analyzer.career.infrastructure.google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

@Component
@ConditionalOnProperty(prefix = "career.google-sheets", name = "enabled", havingValue = "true")
public class GoogleSheetsClient {
    private volatile Sheets delegate;

    public Sheets get() {
        Sheets current = delegate;
        if (current != null) return current;
        synchronized (this) {
            if (delegate == null) delegate = create();
            return delegate;
        }
    }

    private Sheets create() {
        try {
            GoogleCredentials credentials = resolveCredentials()
                    .createScoped(SheetsScopes.SPREADSHEETS);
            return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                    .setApplicationName("Career Intelligence").build();
        } catch (IOException exception) {
            throw new GoogleSheetsFunctionalException("CREDENTIALS_UNAVAILABLE",
                    "Les identifiants Google sont absents ou invalides.", exception);
        } catch (GeneralSecurityException exception) {
            throw new GoogleSheetsFunctionalException("GOOGLE_CLIENT_INITIALIZATION_FAILED",
                    "Le client Google Sheets n’a pas pu être initialisé.", exception);
        }
    }

    private GoogleCredentials resolveCredentials() throws IOException {
        try {
            return GoogleCredentials.getApplicationDefault();
        } catch (IOException adcFailure) {
            Path configuredPath = credentialPathFromSystemProperties();
            if (configuredPath == null) {
                throw new GoogleSheetsFunctionalException("CREDENTIALS_UNAVAILABLE",
                        "La variable GOOGLE_APPLICATION_CREDENTIALS n’est pas disponible dans le processus.",
                        adcFailure);
            }
            if (!Files.isRegularFile(configuredPath) || !Files.isReadable(configuredPath)) {
                throw new GoogleSheetsFunctionalException("CREDENTIALS_FILE_UNREADABLE",
                        "Le fichier de credentials Google configuré est absent ou illisible.", adcFailure);
            }
            try (InputStream input = Files.newInputStream(configuredPath)) {
                return ServiceAccountCredentials.fromStream(input);
            } catch (IOException invalidFile) {
                throw new GoogleSheetsFunctionalException("CREDENTIALS_INVALID",
                        "Le fichier de credentials Google est invalide.", invalidFile);
            }
        }
    }

    static Path credentialPathFromSystemProperties() {
        String configured = System.getProperty("GOOGLE_APPLICATION_CREDENTIALS");
        if (configured == null || configured.isBlank()) return null;
        try {
            return Path.of(configured.trim()).toAbsolutePath().normalize();
        } catch (InvalidPathException exception) {
            throw new GoogleSheetsFunctionalException("CREDENTIALS_PATH_INVALID",
                    "Le chemin du fichier de credentials Google est invalide.", exception);
        }
    }
}
