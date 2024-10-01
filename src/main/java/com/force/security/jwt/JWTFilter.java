package com.force.security.jwt;

import java.io.IOException;

import org.jboss.logging.Logger;

import com.force.security.CustomSecurityContext;
import com.force.security.JwtAuthenticationException;


import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;


@Provider
@Priority(Priorities.AUTHENTICATION)
@RequestScoped
public class JWTFilter implements ContainerRequestFilter {

    private static final Logger logger = Logger.getLogger(JWTFilter.class);

    @Inject
    IdentityProviderManager identityProviderManager; 

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Extract the Authorization header from the request
        logger.debug("Request URI: " + requestContext.getUriInfo().getRequestUri());

        if (requestContext.getUriInfo().getPath().equals("/api/v1/authenticate")) {
            return;
        }


        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring("Bearer".length()).trim();
            logger.debug("JWT: " + jwt);

            Uni<SecurityIdentity> identityUni = identityProviderManager.authenticate(new TokenAuthenticationRequest(new TokenCredential(jwt, "jwt")));
            identityUni.subscribe().with(
                identity -> {
                logger.debug("Identity: " + identity.getPrincipal().getName());
                requestContext.setSecurityContext(new CustomSecurityContext(identity.getPrincipal(), Boolean.TRUE, "Bearer"));
                logger.debug("jwt authentication successful, jwt: " + jwt);
            },
            failure -> {
                logger.error("Failed to authenticate", failure);
                if (failure instanceof JwtAuthenticationException) {
                    JwtAuthenticationException ex = (JwtAuthenticationException) failure;
                    JWTStatusFilter jwtStatusFilter = ex.getJwtStatusFilter();
                    logger.error("Authentication failed: " + ex.getMessage());
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
            );
        } else {
            logger.error("Missing or invalid Authorization header");
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                            .entity("Missing or invalid Authorization header")
                            .build());
        }
    }
}
