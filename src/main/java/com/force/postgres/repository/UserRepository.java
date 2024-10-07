package com.force.postgres.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

import com.force.postgres.model.User;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, UUID> {
    
    
     public PanacheQuery<User> findByQueryParams(Optional<String> id, Optional<String> companyRuleId, Optional<String> email, Optional<Boolean> enabled) {
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

        if (email.isPresent() && !email.isEmpty()) {
            if (queryBuilder.length() > 0) {
                queryBuilder.append(" and ");
            }
            queryBuilder.append("email = :name");
            params.put("email", email.get());
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

    public PanacheQuery<User> findByEmail(String email) {
        return find("email", email);
    }

    public User findById(UUID userId) {
        return findById(userId);
    }

    public Optional<User> findByEmailOptional(String email) {
        return find("email", email).firstResultOptional();
    }

    public List<User> getUsersByCompanyRuleId(UUID companyRuleId) {
        return list("companyRule.id", companyRuleId);
    }
}

