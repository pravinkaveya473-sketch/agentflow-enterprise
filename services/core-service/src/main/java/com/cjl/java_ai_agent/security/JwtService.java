package com.cjl.java_ai_agent.security;

import com.cjl.java_ai_agent.config.AppProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final AppProperties properties;

    public JwtService(JwtEncoder jwtEncoder, AppProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    public TokenView issue(AppUserEntity user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.security().accessTokenTtl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.security().jwtIssuer())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getUsername())
                .claim("roles", java.util.List.of(user.getRole().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenView(token, "Bearer", expiresAt);
    }

    public record TokenView(String accessToken, String tokenType, Instant expiresAt) {}
}
