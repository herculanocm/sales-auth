package com.force.postgres.repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.force.postgres.model.CompanyRule;
import com.force.postgres.model.CompanyRuleKey;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CompanyRuleKeyRepository implements PanacheRepositoryBase<CompanyRuleKey, UUID> {

    public Optional<CompanyRuleKey> findByUUID(UUID uuid) {
        return find("id", uuid).firstResultOptional();
    }

    public Optional<CompanyRuleKey> findByCompanyRuleUUID(UUID companyRuleUUID) {
        return find("companyRule.id", companyRuleUUID).firstResultOptional();
    }

        public Optional<CompanyRuleKey> existsCompanyRuleKeyByCompanyUUIDDiff(UUID companyId, UUID id) {
        return find("companyRule.id = :companyId and id != :id", Map.of("companyId", companyId, "id", id)).firstResultOptional();
    }
    
}
