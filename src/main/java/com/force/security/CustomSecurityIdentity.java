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
    private final Set<String> permissions;

    // Constructor with Principal and Roles
    public CustomSecurityIdentity(Principal principal, Set<String> roles, Set<Credential> credentials) {
        this.principal = principal;
        this.roles = Optional.ofNullable(roles).orElse(Collections.emptySet());
        this.credentials = credentials; // Initialize as empty
        this.attributes = Collections.emptyMap(); // Initialize as empty
        this.permissions = new HashSet<>();
    }

    // Constructor with JWTStatusFilter
    public CustomSecurityIdentity(JWTStatusFilter jwtStatusFilter) {
        String userId = jwtStatusFilter.getUserId();

        // Set the principal using a lambda expression
        this.principal = () -> userId;

        // Initialize roles, credentials, and attributes
        // this.roles =
        // Optional.ofNullable(jwtStatusFilter.getPermissions()).orElse(Collections.emptySet());
        this.roles = jwtStatusFilter.getRoles();
        this.permissions = jwtStatusFilter.getPermissions();
        this.attributes = jwtStatusFilter.getAttributes();
        this.credentials = Collections.emptySet(); // No credentials by default
    }

    public Set<String> getPermissions() {
        return permissions;
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

    private boolean roleHasPermission(String role, String permission) {
        // Implement logic to check if a role grants the specified permission
        // This can be a lookup in a database, a configuration file, etc.
        // For simplicity, we'll assume a hardcoded mapping here
        switch (role) {
            case "ROLE_ADMIN":
                return true; // Admins have all permissions
            default:
                return false;
        }
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
        if (permissions.contains(permission.getName())) {
            return Uni.createFrom().item(true);
        }

        // Optionally, check if any role grants the permission
        for (String role : roles) {
            if (roleHasPermission(role, permission.getName())) {
                return Uni.createFrom().item(true);
            }
        }

        return Uni.createFrom().item(false);
    }
}
