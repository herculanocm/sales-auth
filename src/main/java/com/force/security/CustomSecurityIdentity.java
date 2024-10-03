package com.force.security;

import java.security.Permission;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashMap;

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
        this.attributes = Collections.emptyMap(); // Initialize as empty
    }

    // Constructor with JWTStatusFilter
    public CustomSecurityIdentity(JWTStatusFilter jwtStatusFilter) {
        String userId = jwtStatusFilter.getUserId();
        this.principal = () -> userId;
        
        this.roles = jwtStatusFilter.getRoles() != null
        ? Collections.unmodifiableSet(jwtStatusFilter.getRoles())
        : Collections.emptySet();

        Set<String> permissions = jwtStatusFilter.getPermissions() != null
                ? Collections.unmodifiableSet(jwtStatusFilter.getPermissions())
                : Collections.emptySet();

        this.attributes = new HashMap<>();
        attributes.put("permissions", permissions);
        attributes.put("roles", this.roles);
        attributes.put("userId", userId);

        this.credentials = Collections.emptySet();

        if (jwtStatusFilter.getAttributes() != null) {
            attributes.putAll(jwtStatusFilter.getAttributes());
        }
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
        // Retrieve permissions from attributes
        Set<String> permissions = getAttribute("permissions");
        if (permissions != null && permissions.contains(permission.getName())) {
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

    private boolean roleHasPermission(String role, String permission) {
        // Implement logic to check if a role grants the specified permission
        // For simplicity, we'll assume a hardcoded mapping here
        if ("ROLE_ADMIN".equals(role)) {
            return true; // Admins have all permissions
        }
        // Add additional role-permission mappings as needed
        return false;
    }
}
