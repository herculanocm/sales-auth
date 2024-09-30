package com.force.security.jwt;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import com.force.DTO.JWTTokenReturnDTO;
import com.force.controller.DefaultValuesConstants;
import com.force.service.JWTCacheService;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;

import io.jsonwebtoken.Jwts;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class JWTProvider {

    private static final Logger logger = Logger.getLogger(JWTProvider.class);

    private final JWTCacheService jwtCacheService;

    @Inject
    public JWTProvider(JWTCacheService jwtCacheService) {
        this.jwtCacheService = jwtCacheService;
    }
    
    public PrivateKey getPrivateKey(String base64PrivateKey) throws Exception {
        base64PrivateKey = base64PrivateKey
        .replaceAll("-----BEGIN (.*)-----", "")
        .replaceAll("-----END (.*)-----", "")
        .replaceAll("\\s+", "");  // Remove all line breaks and spaces

        byte[] keyBytes = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    public PublicKey getPublicKey(String base64PublicKey) throws Exception {
        base64PublicKey = base64PublicKey
        .replaceAll("-----BEGIN (.*)-----", "")
        .replaceAll("-----END (.*)-----", "")
        .replaceAll("\\s+", "");  // Remove all line breaks and spaces

        byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    public JWTTokenReturnDTO generateTokenByUsernameAndCompanyId(String userId, String companyId, Boolean rememberMe) {
        logger.info("Generating token for user: " + userId + " and company: " + companyId);
        String base64PrivateKey = jwtCacheService.getBase64PrivateKey(companyId).orElseThrow();
        String token = generateToken(userId, companyId, base64PrivateKey, DefaultValuesConstants.TOKEN_DEFAULT_EXPIRATION_MINUTES);    
        return JWTTokenReturnDTO.builder()
                .accessToken(token)
                .expiresIn(DefaultValuesConstants.TOKEN_DEFAULT_EXPIRATION_MINUTES)
                .tokenType("Bearer")
                .build();
    }

    public String generateToken(String userId, String companyId, String base64PrivateKey, long minutes) {
        Date expiration = Date.from(Instant.now().plus(minutes, ChronoUnit.MINUTES));
        Date issuedAt = Date.from(Instant.now());
        Map<String, Object> claims = new HashMap<>();
        claims.put("companyId", companyId);
        claims.put("sub", userId);
        claims.put("iss", "force.com");
        claims.put("aud", "force.com");
        claims.put("iat", issuedAt);
        claims.put("exp", expiration);

        try {
            PrivateKey privateKey = getPrivateKey(base64PrivateKey);
            return Jwts.builder()
                    .setSubject(userId)
                    .setIssuedAt(issuedAt)
                    .setHeaderParam("typ", "JWT")
                    .setClaims(claims)
                    .setExpiration(expiration)
                    .signWith(privateKey) // Updated signWith method
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Error generating JWT", e);
        }
    }
}
