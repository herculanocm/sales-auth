package com.force.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.Priorities;

import java.util.Set;
import org.jboss.logging.Logger;

import com.force.util.exception.CustomForbiddenException;

@PermissionsAllowed
@Interceptor
@Priority(Priorities.AUTHENTICATION)
public class PermissionsAllowedInterceptor {

    private static final Logger logger = Logger.getLogger(PermissionsAllowedInterceptor.class);

    @Inject
    SecurityIdentity securityIdentity;

    @AroundInvoke
    public Object checkPermissions(InvocationContext context) throws Exception {
        PermissionsAllowed permissionsAllowed = context.getMethod().getAnnotation(PermissionsAllowed.class);
        if (permissionsAllowed == null) {
            permissionsAllowed = context.getTarget().getClass().getAnnotation(PermissionsAllowed.class);
        }

        if (permissionsAllowed != null) {
            String[] requiredRoles = permissionsAllowed.roles();
            String[] requiredPermissions = permissionsAllowed.permissions();
            boolean inclusiveRole = permissionsAllowed.inclusiveRole();
            boolean inclusivePermission = permissionsAllowed.inclusivePermission();

            // Retrieve roles and permissions from SecurityIdentity
            Set<String> userRoles = securityIdentity.getRoles();
            Set<String> userPermissions = securityIdentity.getAttribute("permissions");

            if (userRoles == null || userRoles.isEmpty()) {
                logger.error("Roles not found in SecurityIdentity.");
                throw new CustomForbiddenException("Unable to determine user roles.");
            }

            // Check roles
            if (requiredRoles.length > 0) {
                if (inclusiveRole) {
                    // User must have all required roles
                    for (String role : requiredRoles) {
                        if (!userRoles.contains(role)) {
                            throw new CustomForbiddenException("User lacks required roles.");
                        }
                    }
                } else {
                    // User must have at least one required role
                    boolean hasRole = false;
                    for (String role : requiredRoles) {
                        if (userRoles.contains(role)) {
                            hasRole = true;
                            break;
                        }
                    }
                    if (!hasRole) {
                        throw new CustomForbiddenException("User lacks required roles.");
                    }
                }
            }

            // Check permissions
            if (requiredPermissions.length > 0) {
                if (userPermissions == null || userPermissions.isEmpty()) {
                    logger.error("Permissions not found in SecurityIdentity.");
                    throw new CustomForbiddenException("Unable to determine user permissions.");
                }

                if (inclusivePermission) {
                    // User must have all required permissions
                    for (String permission : requiredPermissions) {
                        if (!userPermissions.contains(permission)) {
                            throw new CustomForbiddenException("User lacks required permissions.");
                        }
                    }
                } else {
                    // User must have at least one required permission
                    boolean hasPermission = false;
                    for (String permission : requiredPermissions) {
                        if (userPermissions.contains(permission)) {
                            hasPermission = true;
                            break;
                        }
                    }
                    if (!hasPermission) {
                        throw new CustomForbiddenException("User lacks required permissions.");
                    }
                }
            }
        }

        return context.proceed();
    }
}