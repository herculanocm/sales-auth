package com.force.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.Set;


import java.util.Arrays;

import java.util.HashSet;

@ApplicationScoped
public class JWTCacheService {
    
    public Optional<String> getBase64PublicKey(String companyId) {
        return Optional.of("");
    }

    public Set<String> getPermissions(String companyId, String userId) {
        return new HashSet<>(Arrays.asList("read", "write"));
    }
}
