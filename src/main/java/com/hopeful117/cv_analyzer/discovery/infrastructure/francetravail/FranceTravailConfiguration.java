package com.hopeful117.cv_analyzer.discovery.infrastructure.francetravail;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(FranceTravailProperties.class)
public class FranceTravailConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "france-travail", name = "enabled", havingValue = "true")
    public RestTemplate franceTravailRestTemplate() {
        return new RestTemplate();
    }
}
