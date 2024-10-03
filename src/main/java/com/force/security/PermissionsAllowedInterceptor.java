package com.force.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.Priorities;

import java.util.Set;
import org.jboss.logging.Logger;

import com.force.util.exception.CustomForbiddenException;

import java.util.Optional;

@PermissionsAllowed({})
@Interceptor
@Priority(Priorities.AUTHENTICATION)
public class PermissionsAllowedInterceptor {

    private static final Logger logger = Logger.getLogger(PermissionsAllowedInterceptor.class);

    @Inject
    SecurityIdentity securityIdentity;

    @AroundInvoke
    public Object checkPermissions(InvocationContext context) throws Exception {
        PermissionsAllowed permissionsAllowed = context.getMethod().getAnnotation(PermissionsAllowed.class);
        if (Optional.ofNullable(permissionsAllowed).isEmpty()) {
            permissionsAllowed = context.getTarget().getClass().getAnnotation(PermissionsAllowed.class);
        }

        if (Optional.ofNullable(permissionsAllowed).isPresent()) {
            String[] requiredPermissions = permissionsAllowed.value();

            // Retrieve permissions from attributes
            Set<String> permissions = securityIdentity.getAttribute("permissions");

            if (Optional.ofNullable(permissions).isEmpty() || permissions.isEmpty()) {
                logger.error("Permissions attribute not found in SecurityIdentity.");
                throw new ForbiddenException("Unable to determine permissions.");
            }

            for (String permission : requiredPermissions) {
                if (!permissions.contains(permission)) {
                    logger.warn("User does not have permission: " + permission);
                    throw new CustomForbiddenException("User does not have permission");
                }
            }
        }

        return context.proceed();
    }
}