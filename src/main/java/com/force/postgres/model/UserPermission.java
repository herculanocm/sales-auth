package com.force.postgres.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "UserPermission")
@Table(name = "sales_user_permission")
public class UserPermission {
    
    @EmbeddedId
    private UserPermissionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "urp_pk_user_uuid", insertable = false, updatable = false)
    @JsonIgnore
    private User user;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserPermissionId implements Serializable {
        
        @Column(name = "urp_pk_user_uuid", updatable = false, nullable = false)
        private UUID userId;

        @Column(name = "urp_pk_permission", updatable = false, nullable = false)
        private String permission;

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            UserPermissionId other = (UserPermissionId) obj;
            if (userId == null) {
                if (other.userId != null)
                    return false;
            } else if (!userId.equals(other.userId))
                return false;
            if (permission == null) {
                if (other.permission != null)
                    return false;
            } else if (!permission.equals(other.permission))
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((userId == null) ? 0 : userId.hashCode());
            result = prime * result + ((permission == null) ? 0 : permission.hashCode());
            return result;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserPermission other = (UserPermission) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }
    
}
