package com.force.security;

import java.security.Permission;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import com.force.security.jwt.JWTStatusFilter;

import io.quarkus.security.credential.Credential;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;

public class CustomSecurityIdentity implements SecurityIdentity {

    private Principal principal;
    private Set<String> roles;
    private Set<Credential> credentials;
    private Map<String, Object> attributes;

    // Constructor with Principal and Roles
    public CustomSecurityIdentity(Principal principal, Set<String> roles, Set<Credential> credentials) {
        this.principal = principal;
        this.roles = Optional.ofNullable(roles).orElse(Collections.emptySet());
        this.credentials = credentials; // Initialize as empty
        this.attributes = Collections.emptyMap();  // Initialize as empty
    }

    // Constructor with JWTStatusFilter
    public CustomSecurityIdentity(JWTStatusFilter jwtStatusFilter) {
        String userId = jwtStatusFilter.getUserId();

        // Set the principal using a lambda expression
        this.principal = () -> userId;

        // Initialize roles, credentials, and attributes
        //this.roles = Optional.ofNullable(jwtStatusFilter.getPermissions()).orElse(Collections.emptySet());
        this.roles = new HashSet<>(Collections.singleton("ROLE_ADMIN"));
        this.credentials = Collections.emptySet(); // No credentials by default
        this.attributes = Collections.emptyMap();  // No attributes by default
    }

    @Override
    public Principal getPrincipal() {
        return this.principal;
    }

    @Override
    public boolean isAnonymous() {
        return false; // User is authenticated
    }

    @Override
    public Set<String> getRoles() {
        return Collections.unmodifiableSet(this.roles); // Return unmodifiable set
    }

    @Override
    public boolean hasRole(String role) {
        return this.roles.contains(role);
    }

    @Override
    public <T extends Credential> T getCredential(Class<T> credentialType) {
        // Since credentials are empty, this will return null
        return credentials.stream()
                .filter(credentialType::isInstance)
                .map(credentialType::cast)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Set<Credential> getCredentials() {
        return Collections.unmodifiableSet(this.credentials); // Return unmodifiable set
    }

    @Override
    public <T> T getAttribute(String name) {
        return (T) attributes.get(name);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(this.attributes); // Return unmodifiable map
    }

    @Override
    public Uni<Boolean> checkPermission(Permission permission) {
        // Implement permission checking logic if needed
        // For now, always return true
        return Uni.createFrom().item(true);
    }
}
