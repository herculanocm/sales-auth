package com.force.security;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.security.PublicKey;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.force.security.jwt.JWTProvider;
import com.force.security.jwt.JWTStatusError;
import com.force.security.jwt.JWTStatusFilter;
import com.force.service.JWTCacheService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.smallrye.mutiny.Uni;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;


@Singleton
public class JwtIdentityProvider implements IdentityProvider<TokenAuthenticationRequest> {

     private static final Logger logger = Logger.getLogger(JwtIdentityProvider.class);

    @Inject
    private JWTCacheService jwtService;

    @ConfigProperty(name = "sales.security.roles-from-jwt", defaultValue = "false") Boolean rolesFromJwt;

    @Inject
    private ObjectMapper mapper;

    @Inject
    private JWTProvider jwtProvider;

    @Override
    public Class<TokenAuthenticationRequest> getRequestType() {
        return TokenAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(TokenAuthenticationRequest request,
            AuthenticationRequestContext context) {
            String token = request.getToken().getToken();

            JWTStatusFilter jwtStatusFilter = getJWTStatusFilter(token);

            if (jwtStatusFilter.getValid()) {
                CustomSecurityIdentity identity = new CustomSecurityIdentity(jwtStatusFilter);
            return Uni.createFrom().item(identity);
            } else {
                logger.error("JWT invalid: " + jwtStatusFilter.getError().getErrorMessage());
                return Uni.createFrom().failure(new JwtAuthenticationException("Invalid JWT Token" , jwtStatusFilter));
            }
    }

    public JWTStatusFilter getJWTStatusFilter(String jwtToken) {
        // Initialize JWTStatusFilter
        JWTStatusFilter jwtStatusFilter = new JWTStatusFilter();
        jwtStatusFilter.setJwt(jwtToken);
        jwtStatusFilter.setValid(false);

        try {

            String[] parts = jwtToken.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            logger.debug(String.format("Payload: %s", payload));

            // Parse the JSON string into a JsonNode
            JsonNode rootNode = mapper.readTree(payload);
            Optional<String> optCompanyId =  Optional.ofNullable(rootNode.get("companyId").asText());


            if (optCompanyId.isEmpty()) {
                logger.error("JWT without companyId");
                jwtStatusFilter.setError(JWTStatusError.builder()
                        .errorMessage("JWT without companyId")
                        .errorDetails("The companyId claim is missing in the JWT")
                        .errorCode(1005)
                        .build());
                return jwtStatusFilter;
            }

            jwtStatusFilter.setCompanyId(optCompanyId.get());

            Optional<String> optBase64PublicKey = jwtService.getBase64PublicKey(optCompanyId.get());
            if (optBase64PublicKey.isEmpty()) {
                logger.error("Public key not found");
                jwtStatusFilter.setError(JWTStatusError.builder()
                        .errorMessage("Public key not found")
                        .errorDetails("The public key for the company is not found")
                        .errorCode(1006)
                        .build());
                return jwtStatusFilter;
            }

            // Decode and generate the public key
            PublicKey publicKey = jwtProvider.getPublicKey(optBase64PublicKey.get());

            // Build the JwtParser with the public key
            JwtParser parser = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build();

            // Parse the JWT and validate the signature and claims
            Claims claims = parser.parseClaimsJws(jwtToken).getBody();

            // Validate token expiration
            Date expiration = claims.getExpiration();
            if (expiration == null || expiration.before(new Date())) {
                logger.error("JWT expired");
                jwtStatusFilter.setValid(false);
                jwtStatusFilter.setError(JWTStatusError.builder()
                        .errorMessage("JWT expired")
                        .errorDetails("The JWT has expired")
                        .errorCode(1003)
                        .build());
                return jwtStatusFilter;
            }

            // Validate subject (userId)
            Optional<String> userId = Optional.ofNullable(claims.getSubject());
            if (!userId.isPresent() || userId.get().isEmpty()) {
                logger.error("JWT without subject");
                jwtStatusFilter.setValid(false);
                jwtStatusFilter.setError(JWTStatusError.builder()
                        .errorMessage("JWT without subject")
                        .errorDetails("The subject (userId) is missing in the JWT")
                        .errorCode(1004)
                        .build());
                return jwtStatusFilter;
            }

            // Extract permissions from the JWT
            if (rolesFromJwt) {

                @SuppressWarnings("unchecked")
                Optional<java.util.ArrayList<String>> rolesClaim = Optional.ofNullable((java.util.ArrayList<String>) claims.get("roles"));
                Set<String> roles = new HashSet<>();
                if (rolesClaim.isPresent() && !rolesClaim.get().isEmpty()) {
                    // Assuming roles are stored as a comma-separated string
                    roles = rolesClaim.get().stream()
                            .map(String::trim)
                            .collect(Collectors.toSet());
                    if (roles.size() > 0) {
                        jwtStatusFilter.setRoles(roles);
                    }
                } else {
                    jwtStatusFilter.setRoles(roles);
                }


                @SuppressWarnings("unchecked")
                Optional<java.util.ArrayList<String>> permissionsClaim = Optional.ofNullable((java.util.ArrayList<String>) claims.get("permissions"));
                Set<String> permissions = new HashSet<>();
                if (permissionsClaim.isPresent() && !permissionsClaim.get().isEmpty()) {
                    // Assuming permissions are stored as a comma-separated string
                    permissions = permissionsClaim.get().stream()
                            .map(String::trim)
                            .collect(Collectors.toSet());
                    if (permissions.size() > 0) {
                        jwtStatusFilter.setPermissions(permissions);
                    }
                } else {
                    jwtStatusFilter.setPermissions(permissions);
                }

            } else {
                Set<String> permissions = jwtService.getPermissions(optCompanyId.get(), userId.get());
                if (permissions.size() > 0) {
                    jwtStatusFilter.setPermissions(permissions);
                }

                Set<String> roles = jwtService.getRoles(optCompanyId.get(), userId.get());
                if (roles.size() > 0) {
                    jwtStatusFilter.setRoles(roles);
                }
            }

           

            // Set valid status and claims
            jwtStatusFilter.setUserId(userId.get());
            jwtStatusFilter.setValid(true);

            return jwtStatusFilter;

        } catch (JwtException e) {
            logger.error("JWT validation error", e);
            // Handle JWT parsing and validation exceptions
            return JWTStatusFilter.builder()
                    .jwt(jwtToken)
                    .valid(false)
                    .error(JWTStatusError.builder()
                            .errorMessage("JWT validation error")
                            .errorDetails(e.getMessage())
                            .errorCode(1002)
                            .build())
                    .build();
        } catch (Exception e) {
            logger.error("Public key error", e);
            // Handle other exceptions (e.g., public key errors)
            return JWTStatusFilter.builder()
                    .jwt(jwtToken)
                    .valid(false)
                    .error(JWTStatusError.builder()
                            .errorMessage("Public key error")
                            .errorDetails(e.getMessage())
                            .errorCode(1001)
                            .build())
                    .build();
        }
    }
    
}
