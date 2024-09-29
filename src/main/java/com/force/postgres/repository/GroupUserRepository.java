package com.force.postgres.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.force.postgres.model.GroupUser;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class GroupUserRepository implements PanacheRepositoryBase<GroupUser, UUID> {

     public PanacheQuery<GroupUser> findByQueryParams(Optional<String> id, Optional<String> companyRuleId, Optional<String> name, Optional<Boolean> enabled) {
        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        if (id.isPresent()) {
            queryBuilder.append("id = :id");
            params.put("id", UUID.fromString(id.get()));
        }

        if (companyRuleId.isPresent()) {
            if (queryBuilder.length() > 0) {
                queryBuilder.append(" and ");
            }
            queryBuilder.append("companyRule.id = :companyRuleId");
            params.put("companyRuleId", UUID.fromString(companyRuleId.get()));
        }

        if (name.isPresent() && !name.isEmpty()) {
            if (queryBuilder.length() > 0) {
                queryBuilder.append(" and ");
            }
            queryBuilder.append("name like :name");
            params.put("name", "%" + name.get() + "%");
        }

        if (enabled.isPresent()) {
            if (queryBuilder.length() > 0) {
                queryBuilder.append(" and ");
            }
            queryBuilder.append("enabled = :enabled");
            params.put("enabled", enabled.get());
        }

        if (queryBuilder.length() > 0) {
            return find(queryBuilder.toString(), params);
        } else {
            return findAll();
        }
    }
    
}
