package com.force.postgres.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

import com.force.postgres.model.CompanyRule;

@ApplicationScoped
public class CompanyRuleRepository implements PanacheRepositoryBase<CompanyRule, UUID> {
    
    public PanacheQuery<CompanyRule> findByQueryParams(Optional<String> id,  Optional<String> name, Optional<String> cgc, Optional<Boolean> enabled) {
        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        if (id.isPresent()) {
            queryBuilder.append("id = :id");
            params.put("id", id.get());
        }

        if (cgc.isPresent() && !cgc.get().isEmpty()) {
            if (queryBuilder.length() > 0) {
                queryBuilder.append(" and ");
            }
            queryBuilder.append("cgc = :cgc");
            params.put("cgc", cgc.get());
        }

        if (name.isPresent() && !name.get().isEmpty()) {
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

    public Optional<CompanyRule> findByCgc(String cgc) {
        return find("cgc", cgc).firstResultOptional();
    }

    public Optional<CompanyRule> existsCompanyRuleByCgcDifId(String cgc, UUID id) {
        return find("cgc = :cgc and id != :id", Map.of("cgc", cgc, "id", id)).firstResultOptional();
    }
}

