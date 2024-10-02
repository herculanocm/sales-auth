package com.force.security.jwt;

import java.io.IOException;

import org.jboss.logging.Logger;

import com.force.security.CustomSecurityIdentity;
import com.force.security.JwtAuthenticationException;

import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import io.quarkus.security.runtime.SecurityIdentityAssociation;


@Provider
@Priority(Priorities.AUTHENTICATION)
public class JWTFilter implements ContainerRequestFilter {

    private static final Logger logger = Logger.getLogger(JWTFilter.class);

    @Inject
    IdentityProviderManager identityProviderManager;

    @Inject
    SecurityIdentityAssociation identityAssociation;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        logger.debug("Request URI: " + path);

        if ("/api/v1/authenticate".equals(path)) {
            return;
        }

        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring("Bearer".length()).trim();
            logger.debug("JWT: " + jwt);

            try {
                CustomSecurityIdentity customIdentity =  (CustomSecurityIdentity)  identityProviderManager
                        .authenticate(new TokenAuthenticationRequest(new TokenCredential(jwt, "jwt"))).await()
                        .indefinitely();

              

                    // Log the authenticated principal name
                    logger.debug("Authenticated user: " + customIdentity.getPrincipal().getName());

                    logger.debug("Authenticated roles: " + customIdentity.getRoles().toString());

                    logger.debug("Authenticated permissions: " + customIdentity.getPermissions().toString());

                    

                    // Set the SecurityIdentity globally for the current request
                    identityAssociation.setIdentity(customIdentity);

            } catch (Exception e) {
                logger.error("Failed to authenticate", e);
                handleAuthenticationFailure(requestContext, e);
            }
        } else {
            logger.error("Missing or invalid Authorization header");
            abortWithUnauthorized(requestContext, "Missing or invalid Authorization header");
        }
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                .entity(message)
                .build());
    }

    private void handleAuthenticationFailure(ContainerRequestContext requestContext, Exception failure) {
        logger.error("Failed to authenticate", failure);

        if (failure instanceof JwtAuthenticationException) {
            JwtAuthenticationException ex = (JwtAuthenticationException) failure;
            JWTStatusFilter jwtStatusFilter = ex.getJwtStatusFilter();
            logger.error("JWT authentication failed: " + ex.getMessage());
            logger.error("JWT Status: " + jwtStatusFilter);

            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                            .entity(jwtStatusFilter.getError().getErrorMessage())
                            .build());
        } else {
            logger.error("Authentication failed: " + failure.getMessage());
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                            .entity("Authentication failed")
                            .build());
        }
    }

}
