package com.force.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthService {

    @Inject
    public AuthService() { }

    public boolean authenticate(String username, String password) {
        return "user".equals(username) && "password".equals(password);
    }
    
}
