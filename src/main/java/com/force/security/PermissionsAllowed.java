package com.force.security;

import jakarta.interceptor.InterceptorBinding;
import jakarta.enterprise.util.Nonbinding;
import jakarta.ws.rs.NameBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@InterceptorBinding
@NameBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface PermissionsAllowed {
    @Nonbinding String[] value();
}
