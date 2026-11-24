package com.tricore.crmtoyota.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "crm")
public record CrmProperties(Security security, Ml ml) {

    public record Security(
            String jwtSecret,
            long jwtExpirationHours,
            String corsAllowedOrigins
    ) {}

    public record Ml(
            String mode,
            String baseUrl,
            String internalApiKey,
            int timeoutSeconds
    ) {}
}
