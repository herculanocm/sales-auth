package com.force.security;

import java.security.Principal;

import jakarta.ws.rs.core.SecurityContext;

public class CustomSecurityContext implements SecurityContext {

    private final Principal principal;
    private final boolean isSecure;
    private final String authenticationScheme;

    public CustomSecurityContext(Principal principal, boolean isSecure, String authenticationScheme) {
        this.principal = principal;
        this.isSecure = isSecure;
        this.authenticationScheme = authenticationScheme;
    }

    @Override
    public Principal getUserPrincipal() {
       return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public boolean isSecure() {
        return isSecure;
    }

    @Override
    public String getAuthenticationScheme() {
        return authenticationScheme;
    }
    
}
