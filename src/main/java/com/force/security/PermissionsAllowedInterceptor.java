package com.force.security;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.ForbiddenException;
import org.jboss.logging.Logger;

@PermissionsAllowed({})
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 1)
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
            String[] requiredPermissions = permissionsAllowed.value();

            if (securityIdentity instanceof CustomSecurityIdentity) {
                CustomSecurityIdentity customIdentity = (CustomSecurityIdentity) securityIdentity;

                for (String permission : requiredPermissions) {
                    Uni<Boolean> hasPermissionUni = customIdentity.checkPermission(new CustomPermission(permission));
                    Boolean hasPermission = hasPermissionUni.await().indefinitely();

                    if (!hasPermission) {
                        logger.warn("User lacks permission: " + permission);
                        throw new ForbiddenException("You do not have permission to access this resource.");
                    }
                }
            } else {
                logger.error("SecurityIdentity is not an instance of CustomSecurityIdentity.");
                throw new ForbiddenException("Unable to determine permissions.");
            }
        }

        return context.proceed();
    }
}