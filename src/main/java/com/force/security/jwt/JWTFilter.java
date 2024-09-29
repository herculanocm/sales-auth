package com.force.security.jwt;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Set;
import java.util.stream.Collectors;
import java.security.Principal;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.force.service.JWTCacheService;

import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JWTFilter implements ContainerRequestFilter {

    private static final Logger logger = Logger.getLogger(JWTFilter.class);

    private final JWTCacheService jwtService;

    private final Boolean rolesFromJwt;

    private final ObjectMapper mapper;

    private final JWTProvider jwtProvider;

    @Inject
    public JWTFilter(
            JWTCacheService jwtService,
            @ConfigProperty(name = "sales.security.roles-from-jwt", defaultValue = "false") Boolean rolesFromJwt,
            ObjectMapper mapper,
            JWTProvider jwtProvider
            ) {
        this.jwtService = jwtService;
        this.rolesFromJwt = rolesFromJwt;
        this.mapper = mapper;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Extract the Authorization header from the request
        logger.info("Request URI: " + requestContext.getUriInfo().getRequestUri());
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring("Bearer".length()).trim();
            JWTStatusFilter jwtStatusFilter = getJWTStatusFilter(jwt);

            if (jwtStatusFilter.getValid()) {
                // Create a new SecurityIdentity with the extracted user and permissions
                // Create a Principal representing the authenticated user
                Principal userPrincipal = new Principal() {
                    @Override
                    public String getName() {
                        return jwtStatusFilter.getUserId();
                    }
                };

                // Create a set of roles/permissions
                Set<String> roles = jwtStatusFilter.getPermissions();

                // Get the original SecurityContext
                SecurityContext originalContext = requestContext.getSecurityContext();

                // Create a new SecurityContext
                SecurityContext newSecurityContext = new SecurityContext() {
                    @Override
                    public Principal getUserPrincipal() {
                        return userPrincipal;
                    }

                    @Override
                    public boolean isUserInRole(String role) {
                        return roles.contains(role);
                    }

                    @Override
                    public boolean isSecure() {
                        return originalContext.isSecure();
                    }

                    @Override
                    public String getAuthenticationScheme() {
                        return "Bearer";
                    }
                };

                // Set the new SecurityContext
                requestContext.setSecurityContext(newSecurityContext);

            } else {
                // If the token is invalid, abort the request with an error response
                requestContext.abortWith(
                        Response.status(Response.Status.UNAUTHORIZED)
                                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                                .entity(jwtStatusFilter.getError())
                                .build());
            }
        } else {
            // If there's no Authorization header or it doesn't start with Bearer, abort the
            // request
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                            .entity("Missing or invalid Authorization header")
                            .build());
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
                Optional<String> permissionsClaim = Optional.ofNullable(claims.get("permissions", String.class));
                Set<String> permissions = new HashSet<>();
                if (permissionsClaim.isPresent() && !permissionsClaim.get().isEmpty()) {
                    // Assuming permissions are stored as a comma-separated string
                    permissions = Arrays.stream(permissionsClaim.get().split(","))
                            .map(String::trim)
                            .collect(Collectors.toSet());
                    if (permissions.size() > 0) {
                        jwtStatusFilter.setPermissions(permissions);
                    }
                }
            } else {
                Set<String> permissions = jwtService.getPermissions(optCompanyId.get(), userId.get());
                if (permissions.size() > 0) {
                    jwtStatusFilter.setPermissions(permissions);
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
