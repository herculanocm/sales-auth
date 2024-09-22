package com.force.postgres.repository;

import java.util.HashMap;
import java.util.Map;

import com.force.postgres.model.GroupUser;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class GroupUserReporitory implements PanacheRepositoryBase<GroupUser, UUID> {

     public PanacheQuery<GroupUser> findByQueryParams(UUID id, String name, Boolean enabled) {
        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        if (id != null) {
            queryBuilder.append("id = :id");
            params.put("id", id);
        }

        if (name != null && !name.isEmpty()) {
            if (queryBuilder.length() > 0) {
                queryBuilder.append(" and ");
            }
            queryBuilder.append("name like :name");
            params.put("name", "%" + name + "%");
        }

        if (enabled != null) {
            if (queryBuilder.length() > 0) {
                queryBuilder.append(" and ");
            }
            queryBuilder.append("enabled = :enabled");
            params.put("enabled", enabled);
        }

        if (queryBuilder.length() > 0) {
            return find(queryBuilder.toString(), params);
        } else {
            return findAll();
        }
    }
    
}
