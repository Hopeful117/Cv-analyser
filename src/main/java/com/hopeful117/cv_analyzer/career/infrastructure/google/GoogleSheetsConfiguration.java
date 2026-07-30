package com.hopeful117.cv_analyzer.career.infrastructure.google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
@EnableConfigurationProperties(CareerGoogleSheetsProperties.class)
public class GoogleSheetsConfiguration {
    @Bean
    @Lazy
    @ConditionalOnProperty(prefix = "career.google-sheets", name = "enabled", havingValue = "true")
    Sheets googleSheetsClient() throws GeneralSecurityException, IOException {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                .createScoped(SheetsScopes.SPREADSHEETS);
        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                .setApplicationName("Career Intelligence").build();
    }
}
