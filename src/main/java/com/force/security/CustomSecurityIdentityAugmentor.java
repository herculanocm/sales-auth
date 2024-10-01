package com.force.security;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustomSecurityIdentityAugmentor implements SecurityIdentityAugmentor {


    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        if (identity.isAnonymous()) {
            return Uni.createFrom().item(identity);
        }

            CustomSecurityIdentity customIdentity = new CustomSecurityIdentity(identity.getPrincipal(), identity.getRoles(), identity.getCredentials());

            return Uni.createFrom().item(customIdentity);
    }
    
}
