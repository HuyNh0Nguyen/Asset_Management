package com.training.assetmanagement.shared.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.training.assetmanagement.shared.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtServiceImpl implements JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtServiceImpl.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final JwtEncoder encoder;
    private final NimbusJwtDecoder decoder;
    private final Clock clock;
    private final Duration ttl;

    public JwtServiceImpl(AppProperties appProperties, Clock clock) {
        SecretKey key = new SecretKeySpec(
            appProperties.jwt().secret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM
        );
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        this.decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        // Clock tiêm vào, skew = 0 -> test tiến giờ được
        JwtTimestampValidator timestampValidator = new JwtTimestampValidator(Duration.ZERO);
        timestampValidator.setClock(clock);
        this.decoder.setJwtValidator(timestampValidator);
        this.clock = clock;
        this.ttl = appProperties.jwt().ttl();
    }

    @Override
    public String issue(UUID userId) {
        // iat cắt về giây - cùng độ chính xác với token_valid_from
        Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiresAt(now.plus(ttl))
            .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public Optional<VerifiedToken> verify(String token) {
        try {
            Jwt jwt = decoder.decode(token);
            if (jwt.getSubject() == null || jwt.getIssuedAt() == null) {
                return Optional.empty();
            }
            return Optional.of(new VerifiedToken(UUID.fromString(jwt.getSubject()), jwt.getIssuedAt()));
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Rejected JWT: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}
