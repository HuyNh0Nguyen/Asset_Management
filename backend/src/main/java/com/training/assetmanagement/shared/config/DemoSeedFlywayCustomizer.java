package com.training.assetmanagement.shared.config;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

// app.seed-demo=true -> nạp thm db/demo và placeholder email bí danh
// Hộp thư đã được AppPropertirs kiểm khi khởi động
@Component
@RequiredArgsConstructor
public class DemoSeedFlywayCustomizer implements FlywayConfigurationCustomizer {

    private static final String DEMO_LOCATION = "classpath:db/demo";
    private static final String MAIL_USER_PLACEHOLDER = "demoMailUser";
    private static final String MAIL_DOMAIN_PLACEHOLDER = "demoMailDomain";
    private static final char MAIL_SEPARATOR = '@';

    private final AppProperties appProperties;

    @Override
    public void customize(FluentConfiguration configuration) {
        if (!appProperties.seedDemo()) {
            return;
        }
        String[] locations = Stream.concat(
            Arrays.stream(configuration.getLocations()).map(Location::getDescriptor),
            Stream.of(DEMO_LOCATION)
        )
            .distinct()
            .toArray(String[]::new);

        String mailbox = appProperties.demo().mailbox();
        int separator = mailbox.indexOf(MAIL_SEPARATOR);
        Map<String, String> placeholders = new HashMap<>(configuration.getPlaceholders());
        placeholders.put(MAIL_USER_PLACEHOLDER, mailbox.substring(0, separator));
        placeholders.put(MAIL_DOMAIN_PLACEHOLDER, mailbox.substring(separator + 1));

        configuration.locations(locations).placeholders(placeholders);
    }
}
