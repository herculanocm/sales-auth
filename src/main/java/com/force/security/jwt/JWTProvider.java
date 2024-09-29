package com.force.security.jwt;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import com.force.DTO.JWTTokenReturnDTO;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.Jwts;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JWTProvider {
    
    public PrivateKey getPrivateKey(String base64PrivateKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    public PublicKey getPublicKey(String base64PublicKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    public JWTTokenReturnDTO generateTokenByUsernameAndCompanyId(String username, String companyId, Boolean rememberMe) {
        JWTTokenReturnDTO jwtTokenReturnDTO = new JWTTokenReturnDTO();
        return jwtTokenReturnDTO;
    }

    public String generateToken(String userId, String companyId, String base64PrivateKey, long minutes) {
        Map<String, String> claims = new HashMap<>();
        claims.put("companyId", companyId);
        try {
            PrivateKey privateKey = getPrivateKey(base64PrivateKey);
            return Jwts.builder()
                    .setSubject(userId)
                    .setIssuedAt(new Date())
                    .setClaims(claims)
                    .setExpiration(Date.from(Instant.now().plus(minutes, ChronoUnit.MINUTES)))
                    .signWith(privateKey) // Updated signWith method
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Error generating JWT", e);
        }
    }
}
