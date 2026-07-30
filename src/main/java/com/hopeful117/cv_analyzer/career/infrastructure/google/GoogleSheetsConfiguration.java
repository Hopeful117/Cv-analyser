package com.hopeful117.cv_analyzer.career.infrastructure.google;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CareerGoogleSheetsProperties.class)
public class GoogleSheetsConfiguration {
}
