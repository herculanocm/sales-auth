package com.force.security;

import java.security.Permission;

public class CustomPermission extends Permission {
    public CustomPermission(String name) {
        super(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CustomPermission) {
            CustomPermission other = (CustomPermission) obj;
            return this.getName().equals(other.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    @Override
    public String getActions() {
        return null;
    }

    @Override
    public boolean implies(Permission permission) {
        return this.equals(permission);
    }
    
}
