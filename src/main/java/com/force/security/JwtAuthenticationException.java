package com.force.security;

import com.force.security.jwt.JWTStatusFilter;

public class JwtAuthenticationException extends RuntimeException {

    private final JWTStatusFilter jwtStatusFilter;

    public JwtAuthenticationException(String message, JWTStatusFilter jwtStatusFilter) {
        super(message);
        this.jwtStatusFilter = jwtStatusFilter;
    }

    public JwtAuthenticationException(String message, Throwable cause, JWTStatusFilter jwtStatusFilter) {
        super(message, cause);
        this.jwtStatusFilter = jwtStatusFilter;
    }

    public JWTStatusFilter getJwtStatusFilter() {
        return jwtStatusFilter;
    }
}
